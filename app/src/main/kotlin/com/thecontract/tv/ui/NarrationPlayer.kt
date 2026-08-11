package com.thecontract.tv.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsSupertonicModelConfig
import com.thecontract.tv.ContractLog
import com.thecontract.tv.ServerHolder
import com.thecontract.tv.voice.VoiceInstaller
import com.thecontract.tv.voice.VoiceModel
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import kotlin.math.max

/**
 * Reads an instruction out loud on the television.
 *
 * The voice is SupertonicTTS, run entirely on the device by sherpa-onnx. Nothing is ever sent
 * anywhere and no text leaves the box — which is the same guarantee the rest of the app makes and
 * the reason a cloud voice was never an option for content like this. The model itself is
 * downloaded once, because bundling it made the APK too large to hand over; see [VoiceInstaller].
 * After that one fetch the game works with the internet unplugged, as it always did.
 *
 * **Latency is the whole design problem.** The Shield's Tegra X1+ is a 2015 Cortex-A57 cluster
 * and ONNX Runtime here is CPU-only, so generating a paragraph takes about as long as saying it.
 * Waiting for the whole paragraph before making a sound would put fifteen seconds of silence
 * between a term appearing and the television reading it. So generation is streamed:
 * `generateWithCallback` hands back audio as it is produced and each chunk goes straight into a
 * playing [AudioTrack], which means speech starts after the first chunk rather than the last.
 *
 * **Interruption matters as much.** A player rejects a term and the next one arrives; the
 * television has to stop mid-sentence rather than finish reading something nobody is looking at
 * any more. Every request carries a generation number, the callback returns 0 as soon as that
 * number is stale, and the native side stops producing.
 */
class NarrationPlayer(private val context: Context) {

    private companion object {
        /** Speaker 5 of the ten in the Supertonic bundle. */
        const val SPEAKER_ID = 5
        const val THREADS = 4
        /** Trailing silence so the last word is not clipped by the track draining. */
        const val TAIL_SILENCE_SAMPLES = 4000
        /** Quiet period after a failed download before another is started. */
        const val RETRY_COOLDOWN_NANOS = 60_000_000_000L
    }

    /**
     * Two locks rather than one, because the first call can now take minutes.
     *
     * [engineLock] is held for the whole of installing and loading the model; [trackLock] only
     * ever for the microseconds it takes to swap an [AudioTrack]. They were a single lock while
     * the model was in the APK and loading cost a few seconds. With a 139 MB download behind the
     * same lock, [stop] — which is called from the coroutine that collects the published view —
     * would have blocked that collector for the length of the download, freezing the screen.
     */
    private val engineLock = Any()
    private val trackLock = Any()

    // Volatile because [prepare] reads both from the collector thread without taking the lock —
    // the whole point of it being cheap to call on every published view.
    @Volatile
    private var tts: OfflineTts? = null

    @Volatile
    private var loadFailed = false

    private var track: AudioTrack? = null
    private val generation = AtomicLong(0)
    private val preparing = AtomicBoolean(false)

    /** Set after a failed download, so a television with no network is not retried every tick. */
    @Volatile
    private var retryAfterNanos = 0L

    /** Measured seconds of compute per second of speech, or null until something has been said. */
    @Volatile
    var lastRealTimeFactor: Double? = null
        private set

    /**
     * Builds the engine, fetching the model first if this television has never had it.
     *
     * A model that will not load is remembered, so a broken install does not retry on every
     * screen for the rest of the night. A model that could not be *downloaded* is deliberately
     * not remembered: that is usually a television whose network was not up yet, and the right
     * response to the next term arriving is to try again.
     */
    private fun engine(): OfflineTts? = synchronized(engineLock) {
        tts?.let { return it }
        if (loadFailed) return null

        val source = VoiceInstaller.resolve(
            context = context,
            onProgress = { p ->
                ServerHolder.publishNarration {
                    it.copy(
                        failed = false,
                        failureNote = null,
                        download = ServerHolder.NarrationStatus.Download(p.bytesDone, p.bytesTotal)
                    )
                }
            },
            onFailure = { note ->
                ServerHolder.publishNarration {
                    it.copy(failed = true, failureNote = note, speaking = false, download = null)
                }
            }
        ) ?: return null

        ServerHolder.publishNarration { it.copy(download = null, failed = false, failureNote = null) }

        val prefix = when (source) {
            is VoiceInstaller.Source.Assets -> VoiceModel.DIR_NAME
            is VoiceInstaller.Source.Files -> source.dir.absolutePath
        }
        return runCatching {
            val config = OfflineTtsConfig(
                model = OfflineTtsModelConfig(
                    supertonic = OfflineTtsSupertonicModelConfig(
                        durationPredictor = "$prefix/${VoiceModel.DURATION_PREDICTOR}",
                        textEncoder = "$prefix/${VoiceModel.TEXT_ENCODER}",
                        vectorEstimator = "$prefix/${VoiceModel.VECTOR_ESTIMATOR}",
                        vocoder = "$prefix/${VoiceModel.VOCODER}",
                        ttsJson = "$prefix/${VoiceModel.TTS_JSON}",
                        unicodeIndexer = "$prefix/${VoiceModel.UNICODE_INDEXER}",
                        voiceStyle = "$prefix/${VoiceModel.VOICE_STYLE}"
                    ),
                    numThreads = THREADS,
                    provider = "cpu"
                ),
                // One sentence at a time is what makes streaming worth having: the first full
                // stop is the point at which the television can start talking.
                maxNumSentences = 1
            )
            // Assets are opened through the asset manager; a downloaded model is opened by path.
            val manager = (source as? VoiceInstaller.Source.Assets)?.let { context.assets }
            OfflineTts(assetManager = manager, config = config).also { tts = it }
        }.onFailure {
            loadFailed = true
            ServerHolder.publishNarration { s ->
                s.copy(failed = true, failureNote = null, speaking = false, download = null)
            }
            ContractLog.w("Narration unavailable: ${it.message}")
        }.getOrNull()
    }

    /**
     * Gets the model onto the television and into memory ahead of the first line.
     *
     * Called from the view collector on every publish once narration is known to be wanted, so it
     * has to be free after the first time: [preparing] makes every call but the first a single
     * atomic read. The first one is what starts the download, which is why this is separate from
     * speaking — a fetch that takes minutes should begin while the players are still filling in
     * profiles, not when the opening term appears.
     */
    fun prepare() {
        if (loadFailed || System.nanoTime() < retryAfterNanos) return
        if (!preparing.compareAndSet(false, true)) return
        thread(name = "narration-warmup", isDaemon = true) {
            try {
                engine()
            } finally {
                if (tts == null) {
                    // Freed for another attempt, because the usual reason for getting here is a
                    // television whose network was not up yet. A minute of quiet first: views
                    // republish every tick, and without it a box with no network at all would
                    // start a fresh download attempt several times a second.
                    retryAfterNanos = System.nanoTime() + RETRY_COOLDOWN_NANOS
                    preparing.set(false)
                }
            }
        }
    }

    /** Loads the model if it is already here, but never starts a download on its own. */
    fun warmUp() {
        thread(name = "narration-precheck", isDaemon = true) {
            if (VoiceInstaller.ready(context)) engine()
        }
    }

    /**
     * Says [text], cutting off whatever was being said before it.
     *
     * Returns immediately; the work happens on its own thread.
     */
    fun speak(text: String) {
        if (text.isBlank()) return
        val mine = generation.incrementAndGet()
        thread(name = "narration", isDaemon = true) { run(text, mine) }
    }

    /** Stops mid-sentence and produces nothing further until the next [speak]. */
    fun stop() {
        generation.incrementAndGet()
        synchronized(trackLock) { releaseTrack() }
        ServerHolder.publishNarration { it.copy(speaking = false) }
    }

    fun release() {
        stop()
        synchronized(engineLock) {
            runCatching { tts?.free() }
            tts = null
        }
    }

    private fun run(text: String, mine: Long) {
        val engine = engine() ?: return
        if (generation.get() != mine) return

        val sampleRate = runCatching { engine.sampleRate() }.getOrDefault(24000)
        val startedAt = System.nanoTime()
        var samplesProduced = 0L

        val out = synchronized(trackLock) {
            if (generation.get() != mine) return
            releaseTrack()
            newTrack(sampleRate).also { track = it }
        } ?: return

        ServerHolder.publishNarration { it.copy(speaking = true) }
        runCatching {
            out.play()
            engine.generateWithCallback(text = text, sid = SPEAKER_ID, speed = 1.0f) { chunk ->
                if (generation.get() != mine) {
                    0
                } else {
                    samplesProduced += chunk.size
                    write(out, chunk)
                    1
                }
            }
            if (generation.get() == mine) {
                // Without this the track stops the instant the last chunk is queued and eats
                // the final syllable.
                write(out, FloatArray(TAIL_SILENCE_SAMPLES))
                val seconds = samplesProduced.toDouble() / sampleRate
                if (seconds > 0.5) {
                    val rtf = (System.nanoTime() - startedAt) / 1e9 / seconds
                    lastRealTimeFactor = rtf
                    ServerHolder.publishNarration { it.copy(realTimeFactor = rtf) }
                }
            }
        }.onFailure { ContractLog.w("Narration failed: ${it.message}") }

        synchronized(trackLock) {
            if (generation.get() == mine) {
                releaseTrack()
                ServerHolder.publishNarration { it.copy(speaking = false) }
            }
        }
    }

    private fun write(out: AudioTrack, samples: FloatArray) {
        var offset = 0
        while (offset < samples.size) {
            val n = out.write(samples, offset, samples.size - offset, AudioTrack.WRITE_BLOCKING)
            if (n <= 0) return
            offset += n
        }
    }

    /**
     * A speech track rather than a media one, so a television that is ducking or mixing treats
     * it as somebody talking. Buffered generously: the generator runs in bursts and an
     * underrun is audible as a click between sentences.
     */
    private fun newTrack(sampleRate: Int): AudioTrack? = runCatching {
        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT
        )
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(max(minBuffer, sampleRate * 4))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_NONE)
            .build()
    }.onFailure { ContractLog.w("No audio track for narration: ${it.message}") }.getOrNull()

    private fun releaseTrack() {
        track?.let {
            runCatching { if (it.playState == AudioTrack.PLAYSTATE_PLAYING) it.pause() }
            runCatching { it.flush() }
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        track = null
    }
}
