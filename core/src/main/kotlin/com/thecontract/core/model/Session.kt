package com.thecontract.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerSetup(
    val name: String = "",
    val role: Role = Role.DOMINANT,
    val analRole: AnalRole = AnalRole.VERS,
    /** "Has trouble getting or staying hard." Filters silently; never explained in content. */
    val erectionDifficulty: Boolean = false
)

/** Everything Player 1 configures before private profiles open (section 16). */
@Serializable
data class SharedSetup(
    val player1: PlayerSetup = PlayerSetup(),
    val player2: PlayerSetup = PlayerSetup(role = Role.SUBMISSIVE),
    val sessionLength: SessionLength = SessionLength.FULL,
    val explicitness: Explicitness = Explicitness.DIRECT,
    val finaleFormat: FinaleFormat = FinaleFormat.THREE_ORDERS,
    val stopWord: String = "red",
    val defaultMaybeCondition: MaybeCondition = MaybeCondition.GENTLER,
    val boundaries: Set<Boundary> = emptySet(),
    val equipment: Set<Equipment> = emptySet(),
    /**
     * Whether the television reads instructions out loud.
     *
     * Speech is public by its nature, so this only ever applies to what is already on the TV —
     * never to a private list open on one phone. See [com.thecontract.core.protocol.Narration].
     */
    val narrationEnabled: Boolean = true
) {
    fun player(slot: Slot): PlayerSetup = if (slot == Slot.PLAYER_1) player1 else player2

    fun name(slot: Slot): String = player(slot).name.ifBlank {
        if (slot == Slot.PLAYER_1) "Player 1" else "Player 2"
    }

    fun slotOfRole(role: Role): Slot =
        if (player1.role == role) Slot.PLAYER_1 else Slot.PLAYER_2

    val dominant: Slot get() = slotOfRole(Role.DOMINANT)
    val submissive: Slot get() = slotOfRole(Role.SUBMISSIVE)

    /** Basic well-formedness: two distinct names, exactly one Dominant. */
    fun validationErrors(): List<String> = buildList {
        if (player1.name.isBlank()) add("Player 1 needs a name")
        if (player2.name.isBlank()) add("Player 2 needs a name")
        if (player1.name.trim().equals(player2.name.trim(), ignoreCase = true)) {
            add("The two players need different names")
        }
        if (player1.role == player2.role) add("One player must be Dominant and the other submissive")
        if (stopWord.isBlank()) add("A stop word is required")
    }
}

/** A player's private profile. Never leaves the server except to that player's own phone. */
@Serializable
data class PrivateProfile(
    val slot: Slot,
    val answers: Map<String, Answer> = emptyMap(),
    /** Per-preference override of the session default Maybe condition. */
    val maybeConditions: Map<String, MaybeCondition> = emptyMap(),
    val complete: Boolean = false
) {
    fun answer(prefId: String): Answer = answers[prefId] ?: Answer.MAYBE
}

@Serializable
data class ConsiderationReceipt(
    val id: String,
    val actionId: String,
    val title: String,
    val instruction: String,
    val performer: Slot,
    val recipient: Slot,
    val mutual: Boolean,
    val completedAtMs: Long,
    val forTermIds: List<String>,
    val bundle: Boolean = false
)

@Serializable
data class SignedTerm(
    val index: Int,
    val act: Int,
    val term: RenderedTerm,
    /** Present when this slot was filled by a bundle trade; consumes two regular slots. */
    val bundledTerm: RenderedTerm? = null,
    val considerationReceiptId: String?,
    val signedBySlots: List<Slot>,
    val signedAtMs: Long,
    /** Non-null for the two mandatory climax terms; identifies who is guaranteed to finish. */
    val closingFor: Slot? = null
) {
    /** One bundle occupies two regular-term slots. */
    val regularSlotsConsumed: Int get() = if (bundledTerm != null) 2 else 1

    val allTerms: List<RenderedTerm> get() = listOfNotNull(term, bundledTerm)

    /** The half of this item a given execution step performs. */
    fun half(bundled: Boolean): RenderedTerm? = if (bundled) bundledTerm else term
}

/**
 * One thing performed in the final scene.
 *
 * A trade is signed as a single contract item holding two terms, and for everything up to the
 * finale that is the right shape: it is approved once, paid for with one consideration and
 * occupies two slots as a unit. Performing it is the one place where that shape is wrong. The two
 * halves are separate acts with their own instructions and their own timers, and — because the
 * binding that keeps the benefit alternating casts the owed man as giver in one and receiver in
 * the other — frequently with the giver and receiver the other way round. Run as a single step
 * they took the first term's instruction, the first term's controller and the first term's
 * completer, so the second was performed silently, timed against the wrong clock and marked
 * complete by the wrong man.
 */
@Serializable
data class ExecutionStep(
    val signedIndex: Int,
    /** True for the second term of a trade. */
    val bundled: Boolean = false
)

@Serializable
data class CurrentProposal(
    val proposalId: String,
    val term: RenderedTerm,
    val bundledTerm: RenderedTerm? = null,
    val responses: Map<Slot, ProposalResponse> = emptyMap(),
    /** Round 1 amendment picks, then round 2 votes when the two picks differ. */
    val amendmentPicks: Map<Slot, Amendment> = emptyMap(),
    val amendmentBallot: List<Amendment> = emptyList(),
    val amendmentVotes: Map<Slot, Amendment> = emptyMap(),
    val amendmentApproval: Map<Slot, Boolean> = emptyMap(),
    val appliedAmendments: List<Amendment> = emptyList(),
    val bundleInitiator: Slot? = null,
    val bundleCandidateIds: List<String> = emptyList(),
    val bundleApproval: Map<Slot, Boolean> = emptyMap(),
    val beneficiary: Slot? = null,
    val considerationOptions: List<RenderedConsideration> = emptyList(),
    val consideration: RenderedConsideration? = null,
    val considerationConfirmed: Map<Slot, Boolean> = emptyMap(),
    val signatures: Map<Slot, Boolean> = emptyMap(),
    val blockedBoundary: String? = null,
    /** Guards against a double tap producing two identical signatures. */
    val handledActionIds: Set<String> = emptySet()
)

@Serializable
data class Negotiation(
    val signed: List<SignedTerm> = emptyList(),
    val seenTermIds: Set<String> = emptySet(),
    val declinedTermIds: Set<String> = emptySet(),
    val receipts: List<ConsiderationReceipt> = emptyList(),
    val current: CurrentProposal? = null,
    val recentConsiderationIds: List<String> = emptyList(),
    /** Whose closing (climax) term is being negotiated, once regular terms are done. */
    val closingTurn: Slot? = null,
    val closingOptionIds: List<String> = emptyList(),
    /** Monotonic counter seeding deterministic proposal selection; survives restarts. */
    val proposalCounter: Int = 0
) {
    val signedRegular: List<SignedTerm> get() = signed.filter { it.closingFor == null }
    val signedClosing: List<SignedTerm> get() = signed.filter { it.closingFor != null }

    /** Regular-term slots used so far. A bundle consumes two. */
    val regularSlotsUsed: Int get() = signedRegular.sumOf { it.regularSlotsConsumed }
}

@Serializable
data class GameTimer(
    val id: String,
    val label: String,
    val parentInstruction: String,
    val totalSeconds: Int,
    val remainingMs: Long,
    val state: TimerRunState = TimerRunState.IDLE,
    /** Server clock anchor for the current run; null unless [state] is RUNNING. */
    val runningSinceMs: Long? = null
) {
    fun remainingAt(nowMs: Long): Long = when (state) {
        TimerRunState.RUNNING -> (remainingMs - (nowMs - (runningSinceMs ?: nowMs))).coerceAtLeast(0)
        else -> remainingMs
    }
}

@Serializable
data class TimerBoard(
    val timers: List<GameTimer> = emptyList(),
    /** Who may start/pause/reset. null + [bothMayControl] = either phone. */
    val controllerSlot: Slot? = null,
    val bothMayControl: Boolean = false,
    /** Who may mark the action complete and advance. */
    val completerSlot: Slot? = null,
    val context: String = ""
) {
    fun timer(id: String): GameTimer? = timers.firstOrNull { it.id == id }
    val anyRunning: Boolean get() = timers.any { it.state == TimerRunState.RUNNING }
}

@Serializable
data class PauseState(
    val paused: Boolean = false,
    val resumedPhase: GamePhase? = null,
    val resumeConfirmations: Map<Slot, Boolean> = emptyMap(),
    val tvOverrideConfirmed: Boolean = false
)

@Serializable
data class FinaleState(
    val orderVotes: Map<Slot, FinaleOrder> = emptyMap(),
    val revealedBallot: List<FinaleOrder> = emptyList(),
    val chosenOrder: FinaleOrder? = null,
    /**
     * What is performed, in order. Climax terms always last, and the two halves of a trade always
     * next to each other, because they were agreed as one thing.
     *
     * Named apart from the `executionOrder` list of plain indices this replaced, so a session
     * saved by an older build decodes rather than being thrown away: the old key is now unknown
     * and skipped, this one falls back to its default, and [com.thecontract.core.engine.FinaleOrdering]
     * rebuilds the list from the signed contract.
     */
    val executionSteps: List<ExecutionStep> = emptyList(),
    val stepIndex: Int = 0,
    val stepsCompleted: Set<Int> = emptySet(),
    val stepStarted: Boolean = false
)

/**
 * The complete authoritative game state. Every mutation bumps [version]; phones send their
 * expected version with each action and are rejected if stale.
 */
@Serializable
data class GameState(
    val sessionId: String,
    val version: Long = 0,
    val phase: GamePhase = GamePhase.PAIRING,
    val createdAtMs: Long = 0,
    val updatedAtMs: Long = 0,
    val setup: SharedSetup = SharedSetup(),
    val setupComplete: Boolean = false,
    val profiles: Map<Slot, PrivateProfile> = emptyMap(),
    val negotiation: Negotiation = Negotiation(),
    val finale: FinaleState = FinaleState(),
    val timers: TimerBoard = TimerBoard(),
    val pause: PauseState = PauseState(),
    /** Set while the draft is open so it can be closed back into the exact prior state. */
    val draftReviewOpen: Boolean = false,
    /**
     * Bumped by the "Read it again" button. The television narrates whenever the text it would
     * speak changes, so raising this counter is all a replay needs to be: the key changes, and
     * the same words are spoken again.
     */
    val narrationNonce: Int = 0,
    /**
     * Bounded ring of client action ids already applied. Makes every mutation idempotent, so a
     * double tap, a retry after a dropped WebSocket or a replayed message cannot double-sign a
     * term, duplicate a receipt or skip two screens at once.
     */
    val processedActionIds: List<String> = emptyList(),
    val completedAtMs: Long? = null
) {
    fun profile(slot: Slot): PrivateProfile = profiles[slot] ?: PrivateProfile(slot)

    val bothProfilesComplete: Boolean
        get() = Slot.entries.all { profile(it).complete }

    val requiredRegularTerms: Int get() = setup.sessionLength.regularTerms

    val regularTermsRemaining: Int
        get() = (requiredRegularTerms - negotiation.regularSlotsUsed).coerceAtLeast(0)

    val allRegularTermsSigned: Boolean get() = regularTermsRemaining == 0

    val allClosingTermsSigned: Boolean get() = negotiation.signedClosing.size >= 2
}

@Serializable
data class PlayerSlotState(
    val slot: Slot,
    val claimed: Boolean = false,
    /** Opaque secret held by the phone's local storage; never displayed. */
    val reconnectToken: String? = null,
    /** Stable-ish browser identity used to spot the same device returning. */
    val deviceId: String? = null,
    val connection: ConnectionState = ConnectionState.NEVER_CONNECTED,
    val lastSeenMs: Long = 0
)

/** An unknown browser asking to take over an occupied saved slot; needs TV-remote confirmation. */
@Serializable
data class PendingReclaim(
    val requestId: String,
    val slot: Slot,
    val deviceId: String,
    val requestedAtMs: Long
)

/**
 * Everything persisted for one session. The undo stack backs Back navigation; it is truncated
 * at every signature so a signed term can never be reopened.
 */
@Serializable
data class SessionRecord(
    val sessionId: String,
    val joinToken: String,
    val slots: Map<Slot, PlayerSlotState> = Slot.entries.associateWith { PlayerSlotState(it) },
    val state: GameState,
    val undoStack: List<GameState> = emptyList(),
    val pendingReclaim: PendingReclaim? = null,
    val savedAtMs: Long = 0,
    val finished: Boolean = false
) {
    fun slot(s: Slot): PlayerSlotState = slots[s] ?: PlayerSlotState(s)
}

/** A completed contract, saved on the TV only. */
@Serializable
data class SavedContract(
    val id: String,
    val title: String,
    val completedAtMs: Long,
    val player1Name: String,
    val player2Name: String,
    val sessionLength: SessionLength,
    val explicitness: Explicitness,
    val signedTerms: List<SignedTerm>,
    val receipts: List<ConsiderationReceipt>,
    val finaleOrder: FinaleOrder?
)
