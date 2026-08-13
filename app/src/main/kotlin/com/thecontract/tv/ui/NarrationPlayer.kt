package com.thecontract.tv.ui

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.thecontract.tv.ContractLog
import com.thecontract.tv.ServerHolder
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

/**
 * Reads an instruction out loud on the television, using the set's own speech engine.
 *
 * This replaced an on-device neural voice — SupertonicTTS run through sherpa-onnx — which was the
 * right idea on the wrong hardware. A Tegra X1 is four 2015 Cortex-A57 cores with no dot-product
 * or half-precision instructions, and the model produced about a second of speech per second of
 * compute, so a term took as long to prepare as to say. Measured on the same sentence, that model
 * runs at 0.22x on a modern desktop core and the Shield is roughly five times slower again. No
 * amount of buffering, threading or sampling-step tuning moved it, because the arithmetic was the
 * arithmetic; three attempts to trade around it each fixed one symptom and produced another.
 *
 * The platform engine starts speaking almost at once, sounds better than anything that would fit
 * in the APK, and takes 223 MB of model and native library out of the build with it.
 *
 * **This one may use the network.** The engine is asked for the best voice it has rather than being
 * restricted to on-device synthesis, so the text of a term can travel to whoever supplies that
 * voice. It is a deliberate exception to the rule that nothing about a session leaves the box,
 * because the alternative was a voice nobody would wait for. Everything else — the profiles, the
 * answers, the contract itself — is still local and always was.
 */
class NarrationPlayer(private val context: Context) {

    private companion object {
        /** Longer than any instruction in the library; engines cap their own input below this. */
        const val MAX_CHARS = 3000

        /** Preference weights when picking among the voices an engine offers. */
        const val PREFER_MALE = 100
        const val PREFER_COUNTRY = 50
    }

    @Volatile
    private var tts: TextToSpeech? = null

    @Volatile
    private var ready = false

    @Volatile
    private var failed = false

    /** Handed over while the engine was still starting; said as soon as it is up. */
    @Volatile
    private var pending: String? = null

    /** Identifies a request, so a callback from one already cancelled is ignored. */
    private val generation = AtomicLong(0)

    /**
     * Starts the engine. Safe to call on every published view: the first call does the work and
     * every later one sees it already built.
     */
    fun prepare() {
        if (tts != null || failed) return
        synchronized(this) {
            if (tts != null || failed) return
            tts = runCatching { TextToSpeech(context, ::onInit) }
                .onFailure {
                    failed = true
                    ContractLog.w("No speech engine on this device: ${it.message}")
                    publishFailure("This television has no speech engine.")
                }
                .getOrNull()
        }
    }

    /** Nothing to download or warm up any more — the engine is a system service. */
    fun warmUp() = prepare()

    private fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            failed = true
            ContractLog.w("Speech engine would not start: $status")
            publishFailure("The speech engine on this television would not start.")
            return
        }
        val engine = tts ?: return
        runCatching {
            engine.setLanguage(Locale.US)
            bestVoice(engine)?.let { engine.voice = it }
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    if (utteranceId == generation.get().toString()) {
                        ServerHolder.publishNarration { it.copy(speaking = true, failed = false) }
                    }
                }

                override fun onDone(utteranceId: String?) = finished(utteranceId)

                @Deprecated("The two-argument form is not delivered on every engine.")
                override fun onError(utteranceId: String?) = finished(utteranceId)

                override fun onError(utteranceId: String?, errorCode: Int) {
                    ContractLog.w("Narration failed: $errorCode")
                    finished(utteranceId)
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) = finished(utteranceId)
            })
        }.onFailure { ContractLog.w("Speech engine setup failed: ${it.message}") }

        ready = true
        ServerHolder.publishNarration { it.copy(failed = false, failureNote = null) }
        pending?.let {
            pending = null
            speak(it)
        }
    }

    /**
     * The best voice the engine offers for US English.
     *
     * Quality first, network or not — waiting for a local voice is the thing this change exists to
     * stop. Among equals a male voice wins, because both players are men and the television is
     * describing what one of them does to the other.
     */
    private fun bestVoice(engine: TextToSpeech): Voice? = runCatching {
        engine.voices
            ?.filter { it.locale.language == Locale.US.language }
            ?.filterNot { it.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) == true }
            ?.maxByOrNull { voice ->
                var score = voice.quality
                val name = voice.name.orEmpty()
                if (name.contains("male", true) && !name.contains("female", true)) score += PREFER_MALE
                if (voice.locale.country == Locale.US.country) score += PREFER_COUNTRY
                score
            }
    }.getOrNull()

    private fun finished(utteranceId: String?) {
        if (utteranceId == generation.get().toString()) {
            ServerHolder.publishNarration { it.copy(speaking = false) }
        }
    }

    /**
     * Says [text], cutting off whatever was being said.
     *
     * `QUEUE_FLUSH` rather than `QUEUE_ADD`: a player rejects a term and the next one arrives, and
     * the television has to stop mid-sentence rather than finish reading something nobody is
     * looking at any more.
     */
    fun speak(text: String) {
        if (text.isBlank() || failed) return
        val engine = tts
        if (engine == null || !ready) {
            pending = text
            prepare()
            return
        }
        val id = generation.incrementAndGet().toString()
        val accepted = runCatching {
            engine.speak(text.take(MAX_CHARS), TextToSpeech.QUEUE_FLUSH, Bundle(), id)
        }.getOrElse {
            ContractLog.w("Narration rejected: ${it.message}")
            TextToSpeech.ERROR
        }
        if (accepted == TextToSpeech.ERROR) {
            ServerHolder.publishNarration { it.copy(speaking = false) }
        }
    }

    /** Stops mid-sentence and says nothing further until the next [speak]. */
    fun stop() {
        pending = null
        generation.incrementAndGet()
        runCatching { tts?.stop() }
        ServerHolder.publishNarration { it.copy(speaking = false) }
    }

    fun release() {
        pending = null
        generation.incrementAndGet()
        synchronized(this) {
            runCatching { tts?.stop() }
            runCatching { tts?.shutdown() }
            tts = null
            ready = false
        }
        ServerHolder.publishNarration { it.copy(speaking = false) }
    }

    private fun publishFailure(note: String) {
        ServerHolder.publishNarration { it.copy(failed = true, failureNote = note, speaking = false) }
    }
}
