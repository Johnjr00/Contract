package com.thecontract.tv

import com.thecontract.core.model.GamePhase
import com.thecontract.core.protocol.ClientView
import com.thecontract.core.server.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The single point of contact between the foreground service that owns the server and the
 * activity that draws the television screen.
 *
 * The activity never reads the database or the session lock directly: the service publishes a
 * finished [ClientView] here on every authoritative change, and the activity renders it. That
 * keeps all storage work off the main thread and means an activity recreation costs nothing —
 * the new activity simply collects the current value.
 */
object ServerHolder {

    private val _tvView = MutableStateFlow(
        ClientView(
            audience = "tv",
            sessionId = "",
            version = 0,
            phase = GamePhase.NO_SESSION,
            heading = "The Contract",
            body = "Starting the local server…"
        )
    )

    /** Always holds the latest TV projection; safe to collect from Compose. */
    val tvView: StateFlow<ClientView> = _tvView.asStateFlow()

    @Volatile
    var manager: SessionManager? = null
        private set

    /**
     * What the narrator is doing, for the small line on the television.
     *
     * [realTimeFactor] is seconds of computation per second of speech, measured on this actual
     * box rather than estimated from somebody else's benchmark — above 1.0 means the machine
     * cannot generate speech as fast as it is spoken, which is the number that decides whether
     * a heavier voice is usable here.
     *
     * [download] is present only during the one-time fetch of the voice, and [failureNote] says
     * what went wrong in a sentence a player can act on — "the television could not reach the
     * network" is a different problem from "this device cannot run the voice", and the line on
     * screen is the only place either of them surfaces.
     */
    data class NarrationStatus(
        val speaking: Boolean = false,
        val realTimeFactor: Double? = null,
        val failed: Boolean = false,
        val failureNote: String? = null,
        val download: Download? = null
    ) {
        data class Download(val bytesDone: Long, val bytesTotal: Long) {
            val percent: Int
                get() = if (bytesTotal <= 0) 0 else ((bytesDone * 100) / bytesTotal).toInt()
        }
    }

    private val _narration = MutableStateFlow(NarrationStatus())
    val narration: StateFlow<NarrationStatus> = _narration.asStateFlow()

    /**
     * Atomic, because these arrive from several threads at once — the download reports progress
     * while the player reports that it has started speaking. Read-modify-write on `.value` loses
     * one of them, which is how a finished download can leave a progress line on screen forever.
     */
    internal fun publishNarration(update: (NarrationStatus) -> NarrationStatus) {
        _narration.update(update)
    }

    /** Whether the local server is listening, and on which port. */
    data class ServerStatus(val running: Boolean, val port: Int)

    private val _server = MutableStateFlow(ServerStatus(running = false, port = 0))

    /**
     * Observable, because the server binds its port a second or two after the screen first
     * draws. Reading a plain field during composition left the header saying "Server offline"
     * for as long as nothing else happened to trigger a recomposition, which on the idle start
     * screen is forever.
     */
    val server: StateFlow<ServerStatus> = _server.asStateFlow()

    var boundPort: Int
        get() = _server.value.port
        internal set(value) {
            _server.value = _server.value.copy(port = value)
        }

    var serverRunning: Boolean
        get() = _server.value.running
        internal set(value) {
            _server.value = _server.value.copy(running = value)
        }

    internal fun attach(sessionManager: SessionManager) {
        manager = sessionManager
    }

    internal fun detach() {
        manager = null
        serverRunning = false
    }

    internal fun publish(view: ClientView) {
        _tvView.value = view
    }
}
