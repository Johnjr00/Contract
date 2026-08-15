package com.thecontract.core.server

import com.thecontract.core.engine.EngineState
import com.thecontract.core.engine.GameEngine
import com.thecontract.core.engine.TimerEngine
import com.thecontract.core.engine.ViewBuilder
import com.thecontract.core.model.ConnectionState
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.GameState
import com.thecontract.core.model.PendingReclaim
import com.thecontract.core.model.PlayerSlotState
import com.thecontract.core.model.SavedContract
import com.thecontract.core.model.SessionRecord
import com.thecontract.core.model.SetupPreset
import com.thecontract.core.model.Slot
import com.thecontract.core.net.LocalInterface
import com.thecontract.core.net.NetworkScanner
import com.thecontract.core.net.QrCode
import com.thecontract.core.persistence.StateStore
import com.thecontract.core.protocol.Choice
import com.thecontract.core.protocol.ClaimSlot
import com.thecontract.core.protocol.ClientMessage
import com.thecontract.core.protocol.ClientView
import com.thecontract.core.protocol.DeleteSetupPreset
import com.thecontract.core.protocol.ErrorMessage
import com.thecontract.core.protocol.GameAction
import com.thecontract.core.protocol.Hello
import com.thecontract.core.protocol.HelloOk
import com.thecontract.core.protocol.JoinInfo
import com.thecontract.core.protocol.LoadSetupPreset
import com.thecontract.core.protocol.Ping
import com.thecontract.core.protocol.PlayerActionMessage
import com.thecontract.core.protocol.Pong
import com.thecontract.core.protocol.ProtocolJson
import com.thecontract.core.protocol.Reconnect
import com.thecontract.core.protocol.ReclaimPending
import com.thecontract.core.protocol.SaveSetupPreset
import com.thecontract.core.protocol.SavedContractSummary
import com.thecontract.core.protocol.ServerMessage
import com.thecontract.core.protocol.SessionFull
import com.thecontract.core.protocol.SetupPresetSummary
import com.thecontract.core.protocol.StateChanged
import com.thecontract.core.protocol.StateSnapshot
import com.thecontract.core.protocol.TimerUpdate
import com.thecontract.core.protocol.UpdateSetup
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

    /**
     * [resumeToken] is remembered so a phone that says hello before there is a session can be
     * answered properly once there is one, without it having to ask again. It cannot ask again:
     * its socket is open and the pings keep it that way, so nothing on the phone would ever
     * prompt a second hello.
     */
    private data class ClientConn(
        val id: String,
        var slot: Slot? = null,
        var deviceId: String? = null,
        var resumeToken: String? = null
    )

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
        greetWaitingClientsLocked()
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
        greetWaitingClientsLocked()
        broadcastLocked()
        true
    }

    /**
     * Hands slots back to phones that were already connected when the session appeared.
     *
     * A phone reconnects on its own the moment the server is listening, which on a relaunch is
     * several seconds before anybody has picked "resume" on the television. It says hello, is
     * told there is no session, and then has nothing left to do: its socket is open and the
     * pings keep it open, so it will never say hello again, and the broadcast that follows the
     * resume skips it because it has no slot. It would sit there, connected and useless, while
     * the game ran on the television without it.
     *
     * So the server asks on its behalf, with what the phone already told it. Each one lands in
     * its own slot by way of the resume token it presented, exactly as if it had reconnected a
     * moment later.
     */
    private fun greetWaitingClientsLocked() {
        val waiting = clients.values.filter { it.slot == null && it.deviceId != null }
        if (waiting.isEmpty()) return
        val outbound = mutableListOf<Pair<String, ServerMessage>>()
        waiting.forEach { conn ->
            helloLocked(conn.id, conn.deviceId!!, conn.resumeToken, outbound)
        }
        outbound.forEach { (id, msg) -> transport?.send(id, msg) }
        // The same full snapshot a phone gets when it says hello itself. The lock is reentrant,
        // so this is safe to do from inside the caller's critical section.
        waiting.forEach { if (it.slot != null) sendSnapshot(it.id) }
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

    /**
     * Handles one message from one phone.
     *
     * Wrapped, because of where this runs. It is called on the web socket's own reader thread,
     * and on Android an exception that reaches the top of any thread kills the whole process —
     * so without this, a single unforeseen failure while handling a tap on somebody's phone
     * takes the television down mid-game and loses the evening. Nothing a phone can send is
     * worth that. The phone is told its action did not land; the game carries on.
     */
    fun onMessage(clientId: String, message: ClientMessage) {
        runCatching {
            when (message) {
                is Ping -> transport?.send(clientId, Pong(message.t))
                is Hello -> handleHello(clientId, message.deviceId, message.resumeToken)
                is ClaimSlot -> handleHello(clientId, message.deviceId, null)
                is Reconnect -> handleHello(clientId, message.deviceId, message.resumeToken)
                is PlayerActionMessage -> handlePlayerAction(clientId, message)
            }
        }.onFailure { failure ->
            runCatching {
                transport?.send(
                    clientId,
                    ErrorMessage("INTERNAL", "The TV could not carry out that action. Try again.")
                )
            }
            // Android routes this to logcat, which is where anyone diagnosing it will look.
            System.err.println("Failed to handle $message from $clientId")
            failure.printStackTrace()
        }
    }

    private fun handleHello(clientId: String, deviceId: String, resumeToken: String?) {
        val outbound = mutableListOf<Pair<String, ServerMessage>>()
        lock.withLock { helloLocked(clientId, deviceId, resumeToken, outbound) }
        outbound.forEach { (id, msg) -> transport?.send(id, msg) }
        lock.withLock { broadcastLocked() }
        sendSnapshot(clientId)
    }

    /**
     * Answers a hello, or replays one on the phone's behalf when a session appears later.
     *
     * Everything the phone told us is recorded before the session is checked, because the
     * no-session answer is not the end of the conversation: the television is usually still on
     * its start screen when the phones reconnect, and the phone has no way to ask a second time.
     */
    private fun helloLocked(
        clientId: String,
        deviceId: String,
        resumeToken: String?,
        outbound: MutableList<Pair<String, ServerMessage>>
    ) {
        val conn = clients.getOrPut(clientId) { ClientConn(clientId) }
        conn.deviceId = deviceId
        if (resumeToken != null) conn.resumeToken = resumeToken

        val rec = record ?: run {
            outbound += clientId to ErrorMessage("NO_SESSION", "No game is running on the TV.")
            return
        }

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
            return
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
            return
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
            return
        }

        outbound += clientId to SessionFull(
            "Session full. This game already has its two players."
        )
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
            slot to ViewBuilder.phoneView(
                es.state, slot, connectionsLocked(), clock(), es.undoStack.isNotEmpty(),
                setupPresetsLocked(slot, es.state)
            )
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
            // Saved settings live on the television beside the saved contracts, so they are
            // resolved here rather than in the engine, which only ever sees game state. Loading
            // one reaches the engine as the ordinary whole-setup replacement it is.
            val loaded = (action as? LoadSetupPreset)?.let { store.loadSetupPreset(it.id) }
            val problem = presetProblemLocked(action, loaded)
            if (problem != null) {
                replyTo?.let { id ->
                    transport?.send(
                        id,
                        com.thecontract.core.protocol.ActionRejected(
                            actionId, GameEngine.CODE_INVALID, problem, es.state.version
                        )
                    )
                }
                return false
            }
            val engineAction = if (loaded != null) UpdateSetup(loaded.setup) else action
            val result = engine.apply(es, slot, actionId, expectedVersion, engineAction, clock())
            accepted = result.accepted
            if (result.accepted) {
                // Setup can finish before or after the second phone joins; re-evaluate pairing
                // progress after every accepted action so neither ordering stalls the game.
                val claimed = Slot.entries.count { record?.slot(it)?.claimed == true }
                engineState = engine.onSlotClaimed(result.engine, claimed, clock())
                record = record?.copy(state = engineState!!.state, undoStack = engineState!!.undoStack)
                if (action is com.thecontract.core.protocol.SaveContract) saveCompletedContractLocked()
                // A replayed tap must not restamp a preset and reshuffle the list under him.
                if (result.code != "DUPLICATE") applyPresetActionLocked(action)
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

    // ------------------------------------------------------------------ saved settings

    /**
     * Why a saved-settings action cannot be carried out, or null if it can.
     *
     * Checked before the engine so the phone is told plainly rather than being handed a rejection
     * about game state that would not explain anything. Deleting something already gone is not a
     * problem: the list ends up as he asked, and a double tap on a slow socket must not raise an
     * error. A full list is refused rather than quietly dropping his oldest saved setup.
     */
    private fun presetProblemLocked(action: GameAction, loaded: SetupPreset?): String? = when (action) {
        is SaveSetupPreset -> {
            val name = action.name.trim()
            val existing = store.listSetupPresets()
            when {
                name.isEmpty() -> "Give these settings a name before saving them."
                existing.size >= SetupPreset.MAX_PRESETS &&
                    existing.none { it.id == SetupPreset.idFor(name) } ->
                    "There is room for ${SetupPreset.MAX_PRESETS} saved settings. Delete one first."
                else -> null
            }
        }
        is LoadSetupPreset -> if (loaded == null) "Those saved settings are no longer on the TV." else null
        else -> null
    }

    private fun applyPresetActionLocked(action: GameAction) {
        when (action) {
            is SaveSetupPreset -> {
                val name = action.name.trim().take(SetupPreset.MAX_NAME_LENGTH)
                store.saveSetupPreset(
                    SetupPreset(
                        id = SetupPreset.idFor(name),
                        name = name,
                        savedAtMs = clock(),
                        setup = action.setup
                    )
                )
            }
            is DeleteSetupPreset -> store.deleteSetupPreset(action.id)
            else -> Unit
        }
    }

    /**
     * The saved-settings list for one phone's view.
     *
     * Read from the store only for the man and the screen that can act on it, so a whole evening
     * of broadcasts does not decrypt a list nobody is looking at.
     */
    private fun setupPresetsLocked(slot: Slot, state: GameState): List<SetupPresetSummary> =
        if (slot == Slot.PLAYER_1 && state.phase == GamePhase.PLAYER_1_SETUP) {
            store.listSetupPresets().map { SetupPresetSummary(it.id, it.name, it.savedAtMs) }
        } else {
            emptyList()
        }

    // ------------------------------------------------------------------ contracts

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
        val view = ViewBuilder.tvView(
            es.state,
            connectionsLocked(),
            joinInfoLocked(),
            clock(),
            store.listContracts().map { SavedContractSummary(it.id, it.title, it.completedAtMs, it.signedTerms.size) }
        )
        val pending = record?.pendingReclaim ?: return view
        return view.copy(
            reclaimRequest = com.thecontract.core.protocol.ReclaimRequestView(
                requestId = pending.requestId,
                slotName = pending.slot.name,
                playerName = es.state.setup.name(pending.slot)
            )
        )
    }

    private fun hasSavedUnfinishedSessionLocked(): Boolean =
        store.loadActiveSession()?.let { !it.finished } == true

    fun phoneView(slot: Slot): ClientView? = lock.withLock {
        val es = engineState ?: return null
        ViewBuilder.phoneView(
            es.state, slot, connectionsLocked(), clock(), es.undoStack.isNotEmpty(),
            setupPresetsLocked(slot, es.state)
        )
    }

    private fun broadcastLocked() {
        val es = engineState
        val outbound = mutableListOf<Pair<String, ServerMessage>>()
        if (es != null) {
            val connections = connectionsLocked()
            for (conn in clients.values) {
                val slot = conn.slot ?: continue
                val view = ViewBuilder.phoneView(
                    es.state, slot, connections, clock(), es.undoStack.isNotEmpty(),
                    setupPresetsLocked(slot, es.state)
                )
                outbound += conn.id to StateChanged(view)
            }
        }
        val tv = tvViewLocked()
        // Send outside the client loop but inside the lock: transports are non-blocking queues.
        outbound.forEach { (id, msg) -> transport?.send(id, msg) }
        tvListener?.onTvView(tv)
    }
}
