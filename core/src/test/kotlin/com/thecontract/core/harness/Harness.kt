package com.thecontract.core.harness

import com.thecontract.core.engine.GameEngine
import com.thecontract.core.model.Slot
import com.thecontract.core.persistence.InMemoryStateStore
import com.thecontract.core.persistence.StateStore
import com.thecontract.core.protocol.ClientView
import com.thecontract.core.protocol.GameAction
import com.thecontract.core.protocol.Hello
import com.thecontract.core.protocol.HelloOk
import com.thecontract.core.protocol.PlayerActionMessage
import com.thecontract.core.protocol.ProtocolJson
import com.thecontract.core.protocol.ReclaimPending
import com.thecontract.core.protocol.ServerMessage
import com.thecontract.core.protocol.SessionFull
import com.thecontract.core.protocol.StateChanged
import com.thecontract.core.protocol.StateSnapshot
import com.thecontract.core.server.SessionManager
import java.util.concurrent.atomic.AtomicLong

/** Deterministic clock so timer behaviour can be exercised without sleeping. */
class TestClock(private var nowMs: Long = 1_700_000_000_000L) {
    fun now(): Long = nowMs
    fun advance(ms: Long) {
        nowMs += ms
    }
}

/**
 * A simulated phone controller.
 *
 * It speaks exactly the wire protocol the real browser client speaks — JSON in, JSON out — and
 * keeps the same client-side state (device id, resume token, last known state version), so the
 * tests exercise the real join, action and reconnection paths rather than calling the engine
 * directly.
 */
class SimulatedPhone(
    val harness: Harness,
    val clientId: String,
    val deviceId: String
) {
    var slot: Slot? = null
    var resumeToken: String? = null
    var version: Long = 0
    var view: ClientView? = null
    var sessionFull: String? = null
    var reclaimRequestId: String? = null
    val received = mutableListOf<ServerMessage>()
    val rejections = mutableListOf<Pair<String, String>>()

    fun receive(message: ServerMessage) {
        received += message
        when (message) {
            is HelloOk -> {
                slot = message.slot
                resumeToken = message.resumeToken
                version = message.version
            }

            is SessionFull -> sessionFull = message.message
            is ReclaimPending -> reclaimRequestId = message.requestId
            is StateSnapshot -> {
                view = message.view
                version = message.view.version
            }

            is StateChanged -> {
                view = message.view
                version = message.view.version
            }

            is com.thecontract.core.protocol.ActionAccepted -> version = message.version
            is com.thecontract.core.protocol.ActionRejected -> {
                version = message.version
                rejections += message.code to message.message
            }

            is com.thecontract.core.protocol.TimerUpdate -> {
                version = message.version
                view = view?.copy(timers = message.timers)
            }

            else -> Unit
        }
    }

    fun connect() {
        harness.manager.onClientConnected(clientId)
        harness.phones[clientId] = this
    }

    fun disconnect() {
        harness.manager.onClientDisconnected(clientId)
        harness.phones.remove(clientId)
    }

    fun hello(withResumeToken: Boolean = true) {
        // Serialise and deserialise so the tests cover the real wire encoding.
        val json = ProtocolJson.encodeToString<com.thecontract.core.protocol.ClientMessage>(
            Hello(deviceId, if (withResumeToken) resumeToken else null)
        )
        harness.manager.onMessage(clientId, json)
    }

    /** Full join: connect, say hello, receive a snapshot. */
    fun join(withResumeToken: Boolean = true): SimulatedPhone {
        connect()
        hello(withResumeToken)
        return this
    }

    fun act(action: GameAction, expectedVersion: Long = version): Boolean {
        val before = rejections.size
        val json = ProtocolJson.encodeToString<com.thecontract.core.protocol.ClientMessage>(
            PlayerActionMessage(harness.nextActionId(), expectedVersion, action)
        )
        harness.manager.onMessage(clientId, json)
        return rejections.size == before
    }

    /** Replays a previously sent action id verbatim, as a dropped-connection retry would. */
    fun actWithId(actionId: String, action: GameAction, expectedVersion: Long = version): Boolean {
        val before = rejections.size
        harness.manager.onMessage(
            clientId,
            ProtocolJson.encodeToString<com.thecontract.core.protocol.ClientMessage>(
                PlayerActionMessage(actionId, expectedVersion, action)
            )
        )
        return rejections.size == before
    }

    fun hasChoice(id: String): Boolean = view?.choices?.any { it.id == id } == true

    fun choiceIdsStartingWith(prefix: String): List<String> =
        view?.choices?.map { it.id }?.filter { it.startsWith(prefix) } ?: emptyList()
}

/**
 * One TV server plus however many simulated phones a test needs, wired through the real
 * [SessionManager] transport interface.
 */
class Harness(
    val store: StateStore = InMemoryStateStore(),
    val clock: TestClock = TestClock()
) {
    val phones = LinkedHashMap<String, SimulatedPhone>()
    private val actionIds = AtomicLong(0)
    var lastTvView: ClientView? = null
        private set

    /** Every TV view produced during a run, for privacy assertions. */
    val tvViews = mutableListOf<ClientView>()

    val manager: SessionManager = SessionManager(store, GameEngine(), clock::now)

    init {
        manager.transport = object : SessionManager.Transport {
            override fun send(clientId: String, message: ServerMessage) {
                phones[clientId]?.receive(message)
            }

            override fun close(clientId: String, reason: String) {
                phones.remove(clientId)
            }
        }
        manager.tvListener = SessionManager.TvListener { lastTvView = it; tvViews += it }
        manager.refreshInterfaces(
            listOf(
                com.thecontract.core.net.LocalInterface(
                    id = "eth0@192.168.1.50",
                    displayName = "Ethernet (eth0)",
                    address = "192.168.1.50",
                    kind = com.thecontract.core.net.InterfaceKind.ETHERNET,
                    privateAddress = true,
                    linkLocal = false,
                    priority = 100
                ),
                com.thecontract.core.net.LocalInterface(
                    id = "wlan0@192.168.1.51",
                    displayName = "Wi-Fi (wlan0)",
                    address = "192.168.1.51",
                    kind = com.thecontract.core.net.InterfaceKind.WIFI,
                    privateAddress = true,
                    linkLocal = false,
                    priority = 90
                )
            )
        )
    }

    /**
     * Client action ids must be globally unique, exactly as the browser's `crypto.randomUUID()`
     * makes them. Reusing an id across a restart would be swallowed by the server's idempotency
     * ring — which is the desired behaviour, and is asserted directly in ProtocolTest.
     */
    private val instanceId: String = java.util.UUID.randomUUID().toString().take(8)

    fun nextActionId(): String = "a-$instanceId-${actionIds.incrementAndGet()}"

    fun newPhone(name: String, resumeToken: String? = null): SimulatedPhone {
        val phone = SimulatedPhone(this, "client-$name-$instanceId-${phones.size}", "device-$name")
        phone.resumeToken = resumeToken
        return phone
    }

    fun startGame(): Harness {
        manager.startNewSession()
        return this
    }

    fun tvView(): ClientView = manager.tvView()

    fun phoneView(slot: Slot): ClientView = manager.phoneView(slot) ?: error("no view for $slot")

    fun state() = manager.sessionRecord?.state ?: error("no session")

    /** Advances the clock and lets the server settle any expired timers. */
    fun advance(ms: Long) {
        clock.advance(ms)
        manager.tick()
    }
}
