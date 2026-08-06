package com.thecontract.tv

import com.thecontract.core.model.GamePhase
import com.thecontract.core.protocol.ClientView
import com.thecontract.core.server.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    @Volatile
    var boundPort: Int = 0
        internal set

    @Volatile
    var serverRunning: Boolean = false
        internal set

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
