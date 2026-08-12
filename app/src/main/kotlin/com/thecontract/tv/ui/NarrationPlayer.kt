package com.thecontract.tv.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
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

        /**
         * Two of the Shield's four cores, not all four.
         *
         * A term takes ten or twenty seconds to generate, and at four threads that is ten or
         * twenty seconds with every core of a 2015 Tegra X1 saturated while Compose is drawing
         * and the server is answering two phones. Starving the main thread that long does not
         * look like slow speech, it looks like the app dying: Android's watchdog declares it
         * unresponsive and kills it. Speech that takes somewhat longer to start is a far better
         * trade than a television that stops.
         */
        const val THREADS = 2
        /** Trailing silence so the last word is not clipped by the track draining. */
        const val TAIL_SILENCE_SAMPLES = 4000
        /** Quiet period after a failed download before another is started. */
        const val RETRY_COOLDOWN_NANOS = 60_000_000_000L
        /** How long shutdown waits for a running generation before freeing the engine. */
        const val SHUTDOWN_JOIN_MS = 2000L
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

    /** The thread generating right now, so shutdown can wait for it rather than free under it. */
    @Volatile
    private var speaking: Thread? = null

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

        // Guarded rather than trusted: this reaches the filesystem and the network, and it is
        // called from a bare thread, where anything thrown reaches the top of that thread and
        // takes the whole process down with it.
        val source = runCatching {
            VoiceInstaller.resolve(
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
            )
        }.onFailure {
            ServerHolder.publishNarration { s ->
                s.copy(failed = true, failureNote = "The voice could not be installed.", download = null)
            }
            ContractLog.w("Voice install failed: ${it.message}")
        }.getOrNull() ?: return null

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
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            try {
                runCatching { engine() }
                    .onFailure { ContractLog.w("Voice preparation failed: ${it.message}") }
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
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            runCatching { if (VoiceInstaller.ready(context)) engine() }
                .onFailure { ContractLog.w("Narration warm-up failed: ${it.message}") }
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
        // Let whoever is speaking notice they are stale and wind up before a second voice starts.
        unblockCurrentTrack()
        val t = thread(name = "narration", isDaemon = true, start = false) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            runCatching { run(text, mine) }
                .onFailure { ContractLog.w("Narration aborted: ${it.message}") }
        }
        speaking = t
        t.start()
    }

    /** Stops mid-sentence and produces nothing further until the next [speak]. */
    fun stop() {
        generation.incrementAndGet()
        unblockCurrentTrack()
        ServerHolder.publishNarration { it.copy(speaking = false) }
    }

    /**
     * Cuts a sentence short without destroying anything.
     *
     * Pausing and flushing are safe to do to a track another thread is writing to; releasing it
     * is not, and that distinction is the whole reason this method exists. Flushing discards what
     * is queued, which frees buffer space and lets a blocked write return, at which point the
     * thread that owns the track notices its generation is stale and tears it down itself.
     */
    private fun unblockCurrentTrack() {
        synchronized(trackLock) {
            track?.let {
                runCatching { if (it.playState == AudioTrack.PLAYSTATE_PLAYING) it.pause() }
                runCatching { it.flush() }
            }
        }
    }

    fun release() {
        stop()
        // The native engine cannot be freed underneath a generation that is still running inside
        // it. Waiting is bounded: the flush above has already unblocked the writer.
        runCatching { speaking?.join(SHUTDOWN_JOIN_MS) }
        synchronized(engineLock) {
            runCatching { tts?.free() }
            tts = null
        }
    }

    /**
     * Generates and plays one passage.
     *
     * **A track belongs to the thread that made it, and only that thread ever releases it.** The
     * audio is written from here while [stop] and the next [speak] run on other threads, and
     * `AudioTrack.release` frees the native object immediately — so releasing another thread's
     * track pulls it out from under a blocked `write`, which is a use-after-free and takes the
     * whole app down. Other threads may only ask this one to stop, by way of [unblockCurrentTrack]
     * and the generation counter.
     */
    private fun run(text: String, mine: Long) {
        val engine = engine() ?: return
        if (generation.get() != mine) return

        val sampleRate = runCatching { engine.sampleRate() }.getOrDefault(24000)
        val startedAt = System.nanoTime()
        var samplesProduced = 0L

        val out = newTrack(sampleRate) ?: return
        synchronized(trackLock) {
            if (generation.get() != mine) {
                runCatching { out.release() }
                return
            }
            // Published so others can pause and flush it. Its predecessor, if any, is left for
            // its own thread to release.
            track = out
        }

        ServerHolder.publishNarration { it.copy(speaking = true) }
        runCatching {
            out.play()
            // Nothing may be thrown out of this callback: it is invoked from C++ across JNI,
            // where an exception unwinding out of it is not caught, it is undefined. Any failure
            // becomes a 0 instead, which is the documented way to ask the generator to stop.
            engine.generateWithCallback(
                text = text,
                sid = SPEAKER_ID,
                speed = 1.0f,
                callback = ChunkSink { chunk ->
                    runCatching {
                        if (generation.get() != mine) {
                            0
                        } else {
                            samplesProduced += chunk.size
                            write(out, chunk, mine)
                            1
                        }
                    }.getOrElse {
                        ContractLog.w("Narration chunk dropped: ${it.message}")
                        0
                    }
                }
            )
            if (generation.get() == mine) {
                // Without this the track stops the instant the last chunk is queued and eats
                // the final syllable.
                write(out, FloatArray(TAIL_SILENCE_SAMPLES), mine)
                val seconds = samplesProduced.toDouble() / sampleRate
                if (seconds > 0.5) {
                    val rtf = (System.nanoTime() - startedAt) / 1e9 / seconds
                    lastRealTimeFactor = rtf
                    ServerHolder.publishNarration { it.copy(realTimeFactor = rtf) }
                }
            }
        }.onFailure { ContractLog.w("Narration failed: ${it.message}") }

        val current = synchronized(trackLock) {
            // Only clear the shared reference if it is still this track; a newer passage may
            // already have published its own, and that one is not ours to disturb.
            if (track === out) track = null
            generation.get() == mine
        }
        // Outside the lock, and only ever by the thread that made it.
        runCatching { if (out.playState == AudioTrack.PLAYSTATE_PLAYING) out.pause() }
        runCatching { out.flush() }
        runCatching { out.stop() }
        runCatching { out.release() }
        if (current) ServerHolder.publishNarration { it.copy(speaking = false) }
    }

    /**
     * Blocking write, in a loop, checking on every pass whether this passage still matters — so a
     * flush from another thread ends it promptly instead of grinding through the whole buffer.
     */
    private fun write(out: AudioTrack, samples: FloatArray, mine: Long) {
        var offset = 0
        while (offset < samples.size) {
            if (generation.get() != mine) return
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

}

/**
 * The streaming callback, as a declared class rather than a lambda.
 *
 * `generateWithCallbackImpl` is handed this object and calls `invoke([F)Ljava/lang/Integer;` on it
 * from C++, once per chunk of audio. Written as a lambda it never survived a release build: Kotlin
 * compiles lambdas through `invokedynamic`, the desugared class carries only the erased
 * `invoke(Object)Object`, and the specialised method the native side asks for by name is never
 * emitted at all — so no ProGuard rule could keep it, because there was nothing there to keep. The
 * first chunk of the first term aborted the process with
 *
 *   NoSuchMethodError: no non-static method "Ll1/k;.invoke([F)Ljava/lang/Integer;"
 *
 * which is a runtime abort, not an exception the `runCatching` inside the callback could ever see.
 *
 * Declaring the class puts the method in the APK where the native lookup can find it. The
 * signature is checked by NarrationCallbackTest against the compiled output, since nothing about
 * this is visible in the source.
 */
internal class ChunkSink(private val onChunk: (FloatArray) -> Int) : (FloatArray) -> Int {
    override fun invoke(p1: FloatArray): Int = onChunk(p1)
}
