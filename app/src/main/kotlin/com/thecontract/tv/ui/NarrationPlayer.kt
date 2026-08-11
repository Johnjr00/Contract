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
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import kotlin.math.max

/**
 * Reads an instruction out loud on the television.
 *
 * The voice is SupertonicTTS, run entirely on the device by sherpa-onnx: the model lives in the
 * APK, nothing is sent anywhere, and the game keeps working with the internet unplugged — which
 * is the same guarantee the rest of the app makes and the reason a cloud voice was never an
 * option for content like this.
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
        const val ASSET_DIR = "tts"
        /** Speaker 5 of the ten in the Supertonic bundle. */
        const val SPEAKER_ID = 5
        const val THREADS = 4
        /** Trailing silence so the last word is not clipped by the track draining. */
        const val TAIL_SILENCE_SAMPLES = 4000
    }

    private val lock = Any()
    private var tts: OfflineTts? = null
    private var loadFailed = false
    private var track: AudioTrack? = null
    private val generation = AtomicLong(0)

    /** Measured seconds of compute per second of speech, or null until something has been said. */
    @Volatile
    var lastRealTimeFactor: Double? = null
        private set

    /**
     * Builds the engine. Several seconds of model loading, so it happens off the caller's
     * thread and only once; a failure is remembered so a broken install does not retry on
     * every screen for the rest of the night.
     */
    private fun engine(): OfflineTts? = synchronized(lock) {
        tts?.let { return it }
        if (loadFailed) return null
        return runCatching {
            val config = OfflineTtsConfig(
                model = OfflineTtsModelConfig(
                    supertonic = OfflineTtsSupertonicModelConfig(
                        durationPredictor = "$ASSET_DIR/duration_predictor.int8.onnx",
                        textEncoder = "$ASSET_DIR/text_encoder.int8.onnx",
                        vectorEstimator = "$ASSET_DIR/vector_estimator.int8.onnx",
                        vocoder = "$ASSET_DIR/vocoder.int8.onnx",
                        ttsJson = "$ASSET_DIR/tts.json",
                        unicodeIndexer = "$ASSET_DIR/unicode_indexer.bin",
                        voiceStyle = "$ASSET_DIR/voice.bin"
                    ),
                    numThreads = THREADS,
                    provider = "cpu"
                ),
                // One sentence at a time is what makes streaming worth having: the first full
                // stop is the point at which the television can start talking.
                maxNumSentences = 1
            )
            OfflineTts(assetManager = context.assets, config = config).also { tts = it }
        }.onFailure {
            loadFailed = true
            ServerHolder.publishNarration { s -> s.copy(failed = true, speaking = false) }
            ContractLog.w("Narration unavailable: ${it.message}")
        }.getOrNull()
    }

    /** Loads the model ahead of the first line so the opening term is not the one that waits. */
    fun warmUp() {
        thread(name = "narration-warmup", isDaemon = true) { engine() }
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
        synchronized(lock) { releaseTrack() }
        ServerHolder.publishNarration { it.copy(speaking = false) }
    }

    fun release() {
        stop()
        synchronized(lock) {
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

        val out = synchronized(lock) {
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

        synchronized(lock) {
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
