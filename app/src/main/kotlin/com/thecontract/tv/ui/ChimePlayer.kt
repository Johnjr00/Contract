package com.thecontract.tv.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.thecontract.tv.R

/**
 * The end-of-timer chime.
 *
 * Deliberately quiet: the sample itself is mixed low and is played quieter still, because this
 * sounds in a room where two people are mid-scene and being startled is the one thing it must
 * never do. It is a slow-attack, long-decay tone rather than a beep for the same reason.
 *
 * It does **not** request audio focus. Taking focus would duck or pause whatever music is
 * playing — the equipment list has "music or speaker" in it, so something usually is — and a
 * one-second chime is not worth interrupting a track for. Layering over the top is correct.
 */
class ChimePlayer(context: Context) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                // MEDIA so it rides the volume the room has already set for music, rather than
                // the notification stream, which on a TV is often much louder.
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var soundId: Int = 0
    @Volatile private var loaded = false

    init {
        pool.setOnLoadCompleteListener { _, _, status -> loaded = status == 0 }
        soundId = pool.load(context, R.raw.timer_chime, 1)
    }

    /** No-op until the sample is decoded, which takes a moment after the service starts. */
    fun play() {
        if (!loaded) return
        pool.play(soundId, VOLUME, VOLUME, 1, 0, 1f)
    }

    fun release() {
        loaded = false
        pool.release()
    }

    private companion object {
        /** On top of the sample already being mixed at a low peak. */
        const val VOLUME = 0.45f
    }
}
