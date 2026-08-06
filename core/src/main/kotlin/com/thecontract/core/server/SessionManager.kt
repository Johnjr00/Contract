package com.thecontract.core.server

import com.thecontract.core.engine.EngineState
import com.thecontract.core.engine.GameEngine
import com.thecontract.core.engine.TimerEngine
import com.thecontract.core.engine.ViewBuilder
import com.thecontract.core.model.ConnectionState
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.PendingReclaim
import com.thecontract.core.model.PlayerSlotState
import com.thecontract.core.model.SavedContract
import com.thecontract.core.model.SessionRecord
import com.thecontract.core.model.Slot
import com.thecontract.core.net.LocalInterface
import com.thecontract.core.net.NetworkScanner
import com.thecontract.core.net.QrCode
import com.thecontract.core.persistence.StateStore
import com.thecontract.core.protocol.Choice
import com.thecontract.core.protocol.ClaimSlot
import com.thecontract.core.protocol.ClientMessage
import com.thecontract.core.protocol.ClientView
import com.thecontract.core.protocol.ErrorMessage
import com.thecontract.core.protocol.GameAction
import com.thecontract.core.protocol.Hello
import com.thecontract.core.protocol.HelloOk
import com.thecontract.core.protocol.JoinInfo
import com.thecontract.core.protocol.Ping
import com.thecontract.core.protocol.PlayerActionMessage
import com.thecontract.core.protocol.Pong
import com.thecontract.core.protocol.ProtocolJson
import com.thecontract.core.protocol.Reconnect
import com.thecontract.core.protocol.ReclaimPending
import com.thecontract.core.protocol.SavedContractSummary
import com.thecontract.core.protocol.ServerMessage
import com.thecontract.core.protocol.SessionFull
import com.thecontract.core.protocol.StateChanged
import com.thecontract.core.protocol.StateSnapshot
import com.thecontract.core.protocol.TimerUpdate
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Owns the session: slots, secure tokens, reconnection, persistence and broadcasting.
 *
 * The transport is abstracted so the same manager drives the real NanoWSD server on the TV and
 * the in-process simulated clients in the automated multiplayer tests.
 */
class SessionManager(
    private val store: StateStore,
    private val engine: GameEngine = GameEngine(),
    private val clock: () -> Long = System::currentTimeMillis
) {

    interface Transport {
        fun send(clientId: String, message: ServerMessage)
        fun close(clientId: String, reason: String)
    }

    /** The TV surface. Rebuilt and pushed on every authoritative change. */
    fun interface TvListener {
        fun onTvView(view: ClientView)
    }

    private data class ClientConn(val id: String, var slot: Slot? = null, var deviceId: String? = null)

    private val lock = ReentrantLock()
    private val secureRandom = SecureRandom()
    private val clients = LinkedHashMap<String, ClientConn>()
    private val joinFailures = HashMap<String, MutableList<Long>>()

    private var record: SessionRecord? = null
    private var engineState: EngineState? = null

    var transport: Transport? = null
    var tvListener: TvListener? = null

    var port: Int = DEFAULT_PORT
        private set
    var selectedInterface: LocalInterface? = null
        private set
    private var interfaces: List<LocalInterface> = emptyList()

    companion object {
        const val DEFAULT_PORT = 8765
        val FALLBACK_PORTS = listOf(8765, 8766, 8767, 8768, 8769)
        private const val MAX_JOIN_FAILURES = 10
        private const val JOIN_FAILURE_WINDOW_MS = 60_000L
        private const val TOKEN_BYTES = 32
    }

    // ------------------------------------------------------------------ tokens

    private fun secureToken(): String {
        val bytes = ByteArray(TOKEN_BYTES)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    // ------------------------------------------------------------------ lifecycle

    fun hasActiveSession(): Boolean = lock.withLock { record != null }

    fun hasSavedUnfinishedSession(): Boolean = lock.withLock {
        store.loadActiveSession()?.let { !it.finished } == true
    }

    val sessionRecord: SessionRecord? get() = lock.withLock { record }

    val joinToken: String? get() = lock.withLock { record?.joinToken }

    fun startNewSession(): SessionRecord = lock.withLock {
        val now = clock()
        val sessionId = "s-" + secureToken().take(12)
        val es = engine.newGame(sessionId, now)
        val rec = SessionRecord(
            sessionId = sessionId,
            joinToken = secureToken(),
            state = es.state,
            savedAtMs = now
        )
        engineState = es
        record = rec
        clients.values.forEach { it.slot = null }
        persistLocked()
        broadcastLocked()
        rec
    }

    /** Restores the unfinished session from disk. Any running timer comes back paused. */
    fun resumeSavedSession(): Boolean = lock.withLock {
        val saved = store.loadActiveSession() ?: return false
        if (saved.finished) return false
        val restored = engine.restore(EngineState(saved.state, saved.undoStack), clock())
        engineState = restored
        record = saved.copy(
            state = restored.state,
            slots = saved.slots.mapValues { (_, v) -> v.copy(connection = ConnectionState.DISCONNECTED) }
        )
        clients.values.forEach { it.slot = null }
        persistLocked()
        broadcastLocked()
        true
    }

    fun abandonSession() = lock.withLock {
        record = null
        engineState = null
        clients.values.forEach { it.slot = null }
        store.deleteActiveSession()
        broadcastLocked()
    }

    /** Keeps the saved contract and clears the active session. */
    fun finishSession() = lock.withLock {
        record?.let { store.saveSession(it.copy(finished = true, savedAtMs = clock())) }
        record = null
        engineState = null
        store.deleteActiveSession()
        broadcastLocked()
    }

    // ------------------------------------------------------------------ network

    fun refreshInterfaces(scan: List<LocalInterface> = NetworkScanner.scan()) = lock.withLock {
        interfaces = scan
        val current = selectedInterface
        val stillPresent = current != null && scan.any { it.id == current.id }
        if (!stillPresent) {
            selectedInterface = NetworkScanner.best(scan)
        }
        broadcastLocked()
    }

    fun selectInterface(id: String): Boolean = lock.withLock {
        val chosen = interfaces.firstOrNull { it.id == id } ?: return false
        selectedInterface = chosen
        broadcastLocked()
        true
    }

    fun setPort(value: Int) = lock.withLock {
        port = value
        broadcastLocked()
    }

    fun joinUrl(): String? {
        val addr = selectedInterface?.address ?: return null
        val token = record?.joinToken ?: return null
        return "http://$addr:$port/join/$token"
    }

    private fun joinInfoLocked(): JoinInfo? {
        val url = joinUrl() ?: return null
        val iface = selectedInterface ?: return null
        return JoinInfo(
            url = url,
            qrMatrix = runCatching { QrCode.matrix(url) }.getOrDefault(emptyList()),
            interfaceName = iface.displayName,
            address = iface.address,
            port = port,
            availableInterfaces = interfaces.map {
                Choice(it.id, "${it.displayName} — ${it.address}", kind = "option")
            }
        )
    }

    // ------------------------------------------------------------------ join authorisation

    /** Rate-limited join-token check. Used for both the HTTP page and the WebSocket upgrade. */
    fun authorizeJoin(token: String?, remoteAddress: String): Boolean = lock.withLock {
        val expected = record?.joinToken
        val ok = expected != null && token != null && constantTimeEquals(expected, token)
        if (!ok) noteJoinFailureLocked(remoteAddress)
        ok && !isRateLimitedLocked(remoteAddress)
    }

    fun isRateLimited(remoteAddress: String): Boolean = lock.withLock { isRateLimitedLocked(remoteAddress) }

    private fun isRateLimitedLocked(remoteAddress: String): Boolean {
        val now = clock()
        val list = joinFailures[remoteAddress] ?: return false
        list.removeAll { now - it > JOIN_FAILURE_WINDOW_MS }
        return list.size >= MAX_JOIN_FAILURES
    }

    private fun noteJoinFailureLocked(remoteAddress: String) {
        val now = clock()
        val list = joinFailures.getOrPut(remoteAddress) { mutableListOf() }
        list.removeAll { now - it > JOIN_FAILURE_WINDOW_MS }
        list += now
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
        return diff == 0
    }

    // ------------------------------------------------------------------ connections

    fun onClientConnected(clientId: String) = lock.withLock {
        clients[clientId] = ClientConn(clientId)
    }

    fun onClientDisconnected(clientId: String) = lock.withLock {
        val conn = clients.remove(clientId) ?: return
        val slot = conn.slot ?: return
        val rec = record ?: return
        // The slot is reserved, not released: a disconnect never hands it to another device.
        record = rec.copy(
            slots = rec.slots + (slot to rec.slot(slot).copy(
                connection = ConnectionState.DISCONNECTED,
                lastSeenMs = clock()
            ))
        )
        persistLocked()
        broadcastLocked()
    }

    fun onMessage(clientId: String, text: String) {
        val message = runCatching { ProtocolJson.decodeFromString<ClientMessage>(text) }.getOrNull()
        if (message == null) {
            transport?.send(clientId, ErrorMessage("BAD_MESSAGE", "Could not read that message."))
            return
        }
        onMessage(clientId, message)
    }

    fun onMessage(clientId: String, message: ClientMessage) {
        when (message) {
            is Ping -> transport?.send(clientId, Pong(message.t))
            is Hello -> handleHello(clientId, message.deviceId, message.resumeToken)
            is ClaimSlot -> handleHello(clientId, message.deviceId, null)
            is Reconnect -> handleHello(clientId, message.deviceId, message.resumeToken)
            is PlayerActionMessage -> handlePlayerAction(clientId, message)
        }
    }

    private fun handleHello(clientId: String, deviceId: String, resumeToken: String?) {
        val outbound = mutableListOf<Pair<String, ServerMessage>>()
        lock.withLock {
            val rec = record ?: run {
                outbound += clientId to ErrorMessage("NO_SESSION", "No game is running on the TV.")
                return@withLock
            }
            val conn = clients.getOrPut(clientId) { ClientConn(clientId) }
            conn.deviceId = deviceId

            val liveSlots = clients.values.mapNotNull { if (it.id == clientId) null else it.slot }.toSet()

            // 1. A valid resume token always returns the phone to its own slot.
            val byToken = Slot.entries.firstOrNull { slot ->
                val st = rec.slot(slot)
                st.claimed && st.reconnectToken != null && resumeToken != null &&
                    constantTimeEquals(st.reconnectToken, resumeToken)
            }
            // 2. Same browser returning without its token.
            val byDevice = Slot.entries.firstOrNull { slot ->
                val st = rec.slot(slot)
                st.claimed && st.deviceId == deviceId && slot !in liveSlots
            }
            val existing = byToken ?: byDevice
            if (existing != null && (existing !in liveSlots || byToken != null)) {
                attachLocked(conn, existing, deviceId, rotateToken = false)
                outbound += clientId to HelloOk(
                    rec.sessionId, existing, record!!.slot(existing).reconnectToken!!, engineState!!.state.version
                )
                return@withLock
            }

            // 3. A free slot: first phone becomes Player 1, second becomes Player 2.
            val free = Slot.entries.firstOrNull { !rec.slot(it).claimed }
            if (free != null) {
                attachLocked(conn, free, deviceId, rotateToken = true)
                val claimed = Slot.entries.count { record!!.slot(it).claimed }
                engineState = engine.onSlotClaimed(engineState!!, claimed, clock())
                record = record!!.copy(state = engineState!!.state)
                outbound += clientId to HelloOk(
                    rec.sessionId, free, record!!.slot(free).reconnectToken!!, engineState!!.state.version
                )
                persistLocked()
                return@withLock
            }

            // 4. Both slots are taken. An unknown browser may ask to reclaim a disconnected
            //    slot, but only the TV remote can approve it. No PIN is involved.
            val reclaimable = Slot.entries.firstOrNull { it !in liveSlots && rec.slot(it).claimed }
            if (reclaimable != null) {
                val requestId = "rc-" + secureToken().take(10)
                record = rec.copy(
                    pendingReclaim = PendingReclaim(requestId, reclaimable, deviceId, clock())
                )
                outbound += clientId to ReclaimPending(
                    requestId, reclaimable,
                    "This slot already belongs to another phone. Confirm the takeover with the TV remote."
                )
                persistLocked()
                return@withLock
            }

            outbound += clientId to SessionFull(
                "Session full. This game already has its two players."
            )
        }
        outbound.forEach { (id, msg) -> transport?.send(id, msg) }
        lock.withLock { broadcastLocked() }
        sendSnapshot(clientId)
    }

    private fun attachLocked(conn: ClientConn, slot: Slot, deviceId: String, rotateToken: Boolean) {
        val rec = record ?: return
        val existing = rec.slot(slot)
        val token = if (rotateToken || existing.reconnectToken == null) secureToken() else existing.reconnectToken
        conn.slot = slot
        record = rec.copy(
            slots = rec.slots + (slot to PlayerSlotState(
                slot = slot,
                claimed = true,
                reconnectToken = token,
                deviceId = deviceId,
                connection = ConnectionState.CONNECTED,
                lastSeenMs = clock()
            ))
        )
    }

    private fun sendSnapshot(clientId: String) {
        val (slot, view) = lock.withLock {
            val conn = clients[clientId] ?: return
            val slot = conn.slot ?: return
            val es = engineState ?: return
            slot to ViewBuilder.phoneView(es.state, slot, connectionsLocked(), clock(), es.undoStack.isNotEmpty())
        }
        if (slot != null) transport?.send(clientId, StateSnapshot(view))
    }

    // ------------------------------------------------------------------ actions

    private fun handlePlayerAction(clientId: String, message: PlayerActionMessage) {
        val slot = lock.withLock { clients[clientId]?.slot }
        if (slot == null) {
            transport?.send(clientId, ErrorMessage("NOT_JOINED", "This device has not joined the session."))
            return
        }
        applyAction(slot, message.action, message.actionId, message.expectedVersion, clientId)
    }

    /**
     * Applies an action. `slot == null` means it came from the TV remote acting as backup.
     */
    fun applyAction(
        slot: Slot?,
        action: GameAction,
        actionId: String = "tv-" + secureToken().take(10),
        expectedVersion: Long = -1,
        replyTo: String? = null
    ): Boolean {
        var accepted: Boolean
        var reply: ServerMessage? = null
        lock.withLock {
            val es = engineState ?: return false
            val result = engine.apply(es, slot, actionId, expectedVersion, action, clock())
            accepted = result.accepted
            if (result.accepted) {
                // Setup can finish before or after the second phone joins; re-evaluate pairing
                // progress after every accepted action so neither ordering stalls the game.
                val claimed = Slot.entries.count { record?.slot(it)?.claimed == true }
                engineState = engine.onSlotClaimed(result.engine, claimed, clock())
                record = record?.copy(state = engineState!!.state, undoStack = engineState!!.undoStack)
                if (action is com.thecontract.core.protocol.SaveContract) saveCompletedContractLocked()
                persistLocked()
                reply = com.thecontract.core.protocol.ActionAccepted(actionId, engineState!!.state.version)
            } else {
                reply = com.thecontract.core.protocol.ActionRejected(
                    actionId,
                    result.code ?: GameEngine.CODE_INVALID,
                    result.message ?: "That action is not available right now.",
                    es.state.version
                )
            }
        }
        replyTo?.let { id -> reply?.let { transport?.send(id, it) } }
        lock.withLock { broadcastLocked() }
        return accepted
    }

    // ------------------------------------------------------------------ TV remote backup

    fun confirmReclaim(requestId: String, approve: Boolean): Boolean {
        var targetClient: String? = null
        var slot: Slot? = null
        lock.withLock {
            val rec = record ?: return false
            val pending = rec.pendingReclaim ?: return false
            if (pending.requestId != requestId) return false
            record = rec.copy(pendingReclaim = null)
            if (!approve) return true
            slot = pending.slot
            targetClient = clients.values.firstOrNull { it.deviceId == pending.deviceId && it.slot == null }?.id
            targetClient?.let { id ->
                clients[id]?.let { conn -> attachLocked(conn, pending.slot, pending.deviceId, rotateToken = true) }
            }
            persistLocked()
        }
        targetClient?.let { id ->
            val rec = lock.withLock { record }
            val s = slot
            if (rec != null && s != null) {
                transport?.send(
                    id,
                    HelloOk(rec.sessionId, s, rec.slot(s).reconnectToken!!, rec.state.version, reclaimed = true)
                )
            }
            sendSnapshot(id)
        }
        lock.withLock { broadcastLocked() }
        return true
    }

    /** Frees a disconnected slot so a different phone can take it (section 8). */
    fun releaseSlot(slot: Slot) = lock.withLock {
        val rec = record ?: return
        clients.values.filter { it.slot == slot }.forEach { it.slot = null }
        record = rec.copy(
            slots = rec.slots + (slot to PlayerSlotState(slot)),
            pendingReclaim = null
        )
        persistLocked()
        broadcastLocked()
    }

    /** Restarts pairing without touching the contract negotiated so far. */
    fun restartPairing() = lock.withLock {
        val rec = record ?: return
        clients.values.forEach { it.slot = null }
        record = rec.copy(
            joinToken = secureToken(),
            slots = Slot.entries.associateWith { PlayerSlotState(it) },
            pendingReclaim = null
        )
        persistLocked()
        broadcastLocked()
    }

    val pendingReclaim: PendingReclaim? get() = lock.withLock { record?.pendingReclaim }

    // ------------------------------------------------------------------ contracts

    private fun saveCompletedContractLocked() {
        val es = engineState ?: return
        val s = es.state
        if (s.phase != GamePhase.COMPLETED) return
        val contract = SavedContract(
            id = "c-" + s.sessionId.removePrefix("s-"),
            title = "${s.setup.name(Slot.PLAYER_1)} and ${s.setup.name(Slot.PLAYER_2)}",
            completedAtMs = s.completedAtMs ?: clock(),
            player1Name = s.setup.name(Slot.PLAYER_1),
            player2Name = s.setup.name(Slot.PLAYER_2),
            sessionLength = s.setup.sessionLength,
            explicitness = s.setup.explicitness,
            signedTerms = s.negotiation.signed,
            receipts = s.negotiation.receipts,
            finaleOrder = s.finale.chosenOrder
        )
        store.saveContract(contract)
    }

    fun listContracts(): List<SavedContract> = store.listContracts()

    fun deleteContract(id: String) {
        store.deleteContract(id)
        lock.withLock { broadcastLocked() }
    }

    // ------------------------------------------------------------------ ticking & broadcast

    /** Server tick. Advances timer completion and pushes timer updates; never persists. */
    fun tick() {
        val updates: List<Pair<String, ServerMessage>>
        lock.withLock {
            val es = engineState ?: return
            val ticked = engine.tick(es, clock())
            val changed = ticked !== es
            if (changed) {
                engineState = ticked
                record = record?.copy(state = ticked.state)
            }
            if (!ticked.state.timers.anyRunning && !changed) return
            val views = ticked.state.timers.timers.map { ViewBuilder.timerView(it, clock()) }
            updates = clients.values.filter { it.slot != null }
                .map { it.id to TimerUpdate(ticked.state.version, views) as ServerMessage }
            if (changed) {
                // A timer that has just completed is a real state transition: persist it.
                persistLocked()
            }
            tvListener?.onTvView(tvViewLocked())
        }
        updates.forEach { (id, msg) -> transport?.send(id, msg) }
    }

    /** Persists, freezing any running timer at its current value first. */
    fun persistNow() = lock.withLock { persistLocked() }

    private fun persistLocked() {
        val rec = record ?: return
        val es = engineState ?: return
        val frozen = es.state.copy(timers = TimerEngine.freeze(es.state.timers, clock()))
        val toSave = rec.copy(state = frozen, undoStack = es.undoStack, savedAtMs = clock())
        record = rec.copy(state = es.state, undoStack = es.undoStack)
        store.saveSession(toSave)
    }

    private fun connectionsLocked(): Map<Slot, ConnectionState> {
        val rec = record ?: return Slot.entries.associateWith { ConnectionState.NEVER_CONNECTED }
        val live = clients.values.mapNotNull { it.slot }.toSet()
        return Slot.entries.associateWith { slot ->
            when {
                slot in live -> ConnectionState.CONNECTED
                rec.slot(slot).claimed -> ConnectionState.DISCONNECTED
                else -> ConnectionState.NEVER_CONNECTED
            }
        }
    }

    fun tvView(): ClientView = lock.withLock { tvViewLocked() }

    private fun tvViewLocked(): ClientView {
        val es = engineState
        if (es == null) {
            return ClientView(
                audience = "tv",
                sessionId = "",
                version = 0,
                phase = GamePhase.NO_SESSION,
                heading = "The Contract",
                body = if (hasSavedUnfinishedSessionLocked()) {
                    "An unfinished session is saved on this device. Resume it, or start a new game."
                } else {
                    "Start a new game to begin."
                },
                choices = buildList {
                    if (hasSavedUnfinishedSessionLocked()) add(Choice("resume_session", "Resume the unfinished session"))
                    add(Choice("new_game", "Start a new game"))
                },
                savedContracts = store.listContracts().map {
                    SavedContractSummary(it.id, it.title, it.completedAtMs, it.signedTerms.size)
                }
            )
        }
        return ViewBuilder.tvView(
            es.state,
            connectionsLocked(),
            joinInfoLocked(),
            clock(),
            store.listContracts().map { SavedContractSummary(it.id, it.title, it.completedAtMs, it.signedTerms.size) }
        )
    }

    private fun hasSavedUnfinishedSessionLocked(): Boolean =
        store.loadActiveSession()?.let { !it.finished } == true

    fun phoneView(slot: Slot): ClientView? = lock.withLock {
        val es = engineState ?: return null
        ViewBuilder.phoneView(es.state, slot, connectionsLocked(), clock(), es.undoStack.isNotEmpty())
    }

    private fun broadcastLocked() {
        val es = engineState
        val outbound = mutableListOf<Pair<String, ServerMessage>>()
        if (es != null) {
            val connections = connectionsLocked()
            for (conn in clients.values) {
                val slot = conn.slot ?: continue
                val view = ViewBuilder.phoneView(es.state, slot, connections, clock(), es.undoStack.isNotEmpty())
                outbound += conn.id to StateChanged(view)
            }
        }
        val tv = tvViewLocked()
        // Send outside the client loop but inside the lock: transports are non-blocking queues.
        outbound.forEach { (id, msg) -> transport?.send(id, msg) }
        tvListener?.onTvView(tv)
    }
}
