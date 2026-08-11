package com.thecontract.core.engine

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.model.Act
import com.thecontract.core.model.Amendment
import com.thecontract.core.model.ConsiderationReceipt
import com.thecontract.core.model.CurrentProposal
import com.thecontract.core.model.FinaleOrder
import com.thecontract.core.model.FinaleState
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.GameState
import com.thecontract.core.model.MaybeCondition
import com.thecontract.core.model.Negotiation
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.PauseState
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.PrivateProfile
import com.thecontract.core.model.ProposalResponse
import com.thecontract.core.model.RenderedConsideration
import com.thecontract.core.model.RenderedTerm
import com.thecontract.core.model.SignedTerm
import com.thecontract.core.model.Slot
import com.thecontract.core.model.Term
import com.thecontract.core.model.TimerBoard
import com.thecontract.core.model.TimerRunState
import com.thecontract.core.protocol.ApproveAmended
import com.thecontract.core.protocol.Back
import com.thecontract.core.protocol.CloseDraft
import com.thecontract.core.protocol.ConfirmSignature
import com.thecontract.core.protocol.ConsiderationPerformed
import com.thecontract.core.protocol.ContinueFlow
import com.thecontract.core.protocol.EndSession
import com.thecontract.core.protocol.ExecutionCommand
import com.thecontract.core.protocol.GameAction
import com.thecontract.core.protocol.GlobalPause
import com.thecontract.core.protocol.OpenDraft
import com.thecontract.core.protocol.PickAmendment
import com.thecontract.core.protocol.PickBundle
import com.thecontract.core.protocol.PickClosing
import com.thecontract.core.protocol.PickConsideration
import com.thecontract.core.protocol.PickFinaleOrder
import com.thecontract.core.protocol.ProposalRespond
import com.thecontract.core.protocol.ResumeVote
import com.thecontract.core.protocol.SaveContract
import com.thecontract.core.protocol.SaveProfile
import com.thecontract.core.protocol.StartExecution
import com.thecontract.core.protocol.StopAllTimers
import com.thecontract.core.protocol.SubmitSetup
import com.thecontract.core.protocol.TimerCommand
import com.thecontract.core.protocol.UpdateSetup
import com.thecontract.core.protocol.VoteAmendment
import com.thecontract.core.protocol.VoteBundle
import com.thecontract.core.protocol.VoteClosing
import kotlin.random.Random

/** State plus the Back-navigation stack. Persisted together. */
data class EngineState(
    val state: GameState,
    val undoStack: List<GameState> = emptyList()
)

data class ApplyResult(
    val engine: EngineState,
    val accepted: Boolean,
    val code: String? = null,
    val message: String? = null
) {
    val state: GameState get() = engine.state
}

/**
 * The server-authoritative state machine (sections 10, 14, 20, 25–39).
 *
 * Every mutation is validated against the current phase, applied idempotently by client action
 * id, and bumps the state version. Phones never mutate anything; they send a
 * [com.thecontract.core.protocol.GameAction] and receive the resulting authoritative state.
 */
class GameEngine {

    companion object {
        private const val MAX_UNDO = 25
        private const val MAX_PROCESSED_IDS = 250

        const val CODE_STALE = "STALE_VERSION"
        const val CODE_PHASE = "INVALID_PHASE"
        const val CODE_NOT_ALLOWED = "NOT_ALLOWED"
        const val CODE_INVALID = "INVALID_ACTION"
    }

    // ------------------------------------------------------------------ construction

    fun newGame(sessionId: String, nowMs: Long): EngineState = EngineState(
        GameState(
            sessionId = sessionId,
            version = 1,
            phase = GamePhase.PAIRING,
            createdAtMs = nowMs,
            updatedAtMs = nowMs
        )
    )

    fun context(state: GameState): GameContext = GameContext.of(state)

    /**
     * Called by the session manager when a phone claims or reclaims a slot. Keeps pairing
     * progress in the authoritative state rather than in connection bookkeeping.
     */
    fun onSlotClaimed(es: EngineState, claimedSlots: Int, nowMs: Long): EngineState {
        val s = es.state
        val next = when {
            s.phase == GamePhase.PAIRING && claimedSlots >= 1 -> s.copy(phase = GamePhase.PLAYER_1_SETUP)
            s.phase == GamePhase.WAITING_FOR_PLAYER_2 && claimedSlots >= 2 && s.setupComplete ->
                startProfiles(s)
            else -> return es
        }
        return es.copy(state = next.bump(nowMs))
    }

    /** Restores a persisted session: a timer that was running comes back paused. */
    fun restore(es: EngineState, nowMs: Long): EngineState {
        val s = es.state
        return es.copy(
            state = s.copy(
                timers = TimerEngine.restoreAfterRestart(s.timers),
                updatedAtMs = nowMs
            )
        )
    }

    // ------------------------------------------------------------------ entry point

    fun apply(
        es: EngineState,
        slot: Slot?,
        actionId: String,
        expectedVersion: Long,
        action: GameAction,
        nowMs: Long
    ): ApplyResult {
        val state = es.state

        if (actionId.isNotEmpty() && actionId in state.processedActionIds) {
            // Duplicate delivery (double tap, WebSocket retry, replay). Already applied.
            return ApplyResult(es, accepted = true, code = "DUPLICATE")
        }

        // The immediate stop control must work from a stale client, always.
        val bypassesVersion = action is GlobalPause
        if (!bypassesVersion && expectedVersion >= 0 && expectedVersion != state.version) {
            return ApplyResult(
                es, accepted = false, code = CODE_STALE,
                message = "Your screen is out of date. It has been refreshed."
            )
        }

        if (state.pause.paused && action !is ResumeVote && action !is EndSession && action !is GlobalPause) {
            return ApplyResult(es, accepted = false, code = CODE_PHASE, message = "The session is paused.")
        }

        val result = dispatch(es, slot, action, nowMs)
        if (!result.accepted) return result

        // A handler that recognised the action as already-applied leaves the state completely
        // alone: no version bump, so a double tap cannot even make other clients re-render.
        if (result.code == "DUPLICATE") return ApplyResult(es, accepted = true, code = "DUPLICATE")

        val recorded = result.engine.state.copy(
            processedActionIds = (result.engine.state.processedActionIds + actionId)
                .takeLast(MAX_PROCESSED_IDS)
        ).bump(nowMs)
        return result.copy(engine = result.engine.copy(state = recorded))
    }

    private fun GameState.bump(nowMs: Long): GameState = copy(version = version + 1, updatedAtMs = nowMs)

    private fun reject(es: EngineState, code: String, message: String) =
        ApplyResult(es, accepted = false, code = code, message = message)

    private fun ok(es: EngineState, state: GameState, undo: List<GameState>? = null) =
        ApplyResult(es.copy(state = state, undoStack = undo ?: es.undoStack), accepted = true)

    private fun pushUndo(es: EngineState): List<GameState> =
        (es.undoStack + es.state).takeLast(MAX_UNDO)

    // ------------------------------------------------------------------ dispatch

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    private fun dispatch(es: EngineState, slot: Slot?, action: GameAction, now: Long): ApplyResult {
        val s = es.state
        return when (action) {
            is GlobalPause -> handleGlobalPause(es, now)
            is ResumeVote -> handleResumeVote(es, slot, action.confirm, now)
            is EndSession -> ok(es, s.copy(phase = GamePhase.COMPLETED, completedAtMs = now))
            is OpenDraft -> ok(es, s.copy(draftReviewOpen = true))
            is CloseDraft -> ok(es, s.copy(draftReviewOpen = false))
            is SaveContract -> ok(es, s) // persistence handled by the session manager
            is Back -> handleBack(es, now)

            is UpdateSetup -> requireP1(es, slot) {
                if (s.phase != GamePhase.PLAYER_1_SETUP) {
                    reject(es, CODE_PHASE, "Setup is not open.")
                } else {
                    ok(es, s.copy(setup = action.setup))
                }
            }

            is SubmitSetup -> requireP1(es, slot) { handleSubmitSetup(es, action, now) }

            is SaveProfile -> handleSaveProfile(es, slot, action, now)

            is ProposalRespond -> handleProposalResponse(es, slot, action.response, now)
            is PickAmendment -> handlePickAmendment(es, slot, action.amendment, now)
            is VoteAmendment -> handleVoteAmendment(es, slot, action.amendment, now)
            is ApproveAmended -> handleApproveAmended(es, slot, action.approve, now)
            is PickBundle -> handlePickBundle(es, slot, action.termId, now)
            is VoteBundle -> handleVoteBundle(es, slot, action.approve, now)
            is PickConsideration -> handlePickConsideration(es, slot, action.actionId, now)
            is ConsiderationPerformed -> handleConsiderationPerformed(es, slot, now)
            is ConfirmSignature -> handleConfirmSignature(es, slot, action.earned, now)
            is ContinueFlow -> handleContinue(es, now)
            is PickClosing -> handlePickClosing(es, slot, action.termId, now)
            is VoteClosing -> handleVoteClosing(es, slot, action.approve, now)
            is PickFinaleOrder -> handlePickFinaleOrder(es, slot, action.order, now)
            is StartExecution -> handleStartExecution(es, now)
            is ExecutionCommand -> handleExecutionCommand(es, slot, action.command, now)
            is TimerCommand -> handleTimerCommand(es, slot, action.timerId, action.command, now)
            is StopAllTimers -> handleStopAll(es, slot, now)
        }
    }

    private inline fun requireP1(es: EngineState, slot: Slot?, body: () -> ApplyResult): ApplyResult =
        if (slot != null && slot != Slot.PLAYER_1) {
            reject(es, CODE_NOT_ALLOWED, "Only Player 1 runs the shared setup.")
        } else {
            body()
        }

    // ------------------------------------------------------------------ setup & profiles

    private fun handleSubmitSetup(es: EngineState, action: SubmitSetup, now: Long): ApplyResult {
        val s = es.state
        if (s.phase != GamePhase.PLAYER_1_SETUP) return reject(es, CODE_PHASE, "Setup is not open.")
        val errors = action.setup.validationErrors()
        if (errors.isNotEmpty()) return reject(es, CODE_INVALID, errors.joinToString(" "))
        val withSetup = s.copy(setup = action.setup, setupComplete = true, phase = GamePhase.WAITING_FOR_PLAYER_2)
        return ok(es, withSetup)
    }

    private fun startProfiles(s: GameState): GameState = s.copy(
        phase = GamePhase.PRIVATE_PROFILES,
        profiles = Slot.entries.associateWith { slot ->
            s.profiles[slot] ?: PrivateProfile(slot, PreferenceLibrary.blankAnswers())
        }
    )

    private fun handleSaveProfile(es: EngineState, slot: Slot?, action: SaveProfile, now: Long): ApplyResult {
        val s = es.state
        if (slot == null) return reject(es, CODE_NOT_ALLOWED, "Private profiles are filled in on a phone.")
        if (s.phase != GamePhase.PRIVATE_PROFILES && s.phase != GamePhase.WAITING_FOR_PROFILES) {
            return reject(es, CODE_PHASE, "Profiles are not open.")
        }
        val existing = s.profile(slot)
        val merged = PreferenceLibrary.blankAnswers().toMutableMap().apply {
            putAll(existing.answers)
            action.answers.forEach { (k, v) -> if (PreferenceLibrary.byId.containsKey(k)) put(k, v) }
        }
        val conditions = existing.maybeConditions.toMutableMap().apply {
            action.conditions.forEach { (prefId, condId) ->
                MaybeCondition.byId(condId)?.let { put(prefId, it) }
            }
        }
        val updated = existing.copy(answers = merged, maybeConditions = conditions, complete = action.complete)
        var next = s.copy(profiles = s.profiles + (slot to updated))

        next = when {
            Slot.entries.all { next.profile(it).complete } -> startNegotiation(next, now)
            next.profile(slot).complete -> next.copy(phase = GamePhase.WAITING_FOR_PROFILES)
            else -> next.copy(phase = GamePhase.PRIVATE_PROFILES)
        }
        return ok(es, next)
    }

    // ------------------------------------------------------------------ negotiation

    private fun startNegotiation(s: GameState, now: Long): GameState = startProposal(s, now)

    /** Builds the next proposal, or moves to closing terms if the pool is exhausted. */
    private fun startProposal(s: GameState, now: Long): GameState {
        if (s.allRegularTermsSigned) return startClosing(s, now)
        val ctx = GameContext.of(s)
        var selection = ProposalSelector.nextProposal(s, ctx)
        var working = s
        if (selection == null) {
            // Give previously declined terms one more chance rather than stalling the contract.
            working = s.copy(negotiation = s.negotiation.copy(declinedTermIds = emptySet()))
            selection = ProposalSelector.nextProposal(working, ctx)
        }
        if (selection == null) return startClosing(working, now)

        val rendered = Renderer.renderTerm(selection.term, selection.binding, ctx, selection.conditions)
        val counter = working.negotiation.proposalCounter + 1
        return working.copy(
            phase = GamePhase.PROPOSAL,
            timers = TimerEngine.empty,
            negotiation = working.negotiation.copy(
                proposalCounter = counter,
                seenTermIds = working.negotiation.seenTermIds + selection.term.id,
                current = CurrentProposal(
                    proposalId = "p$counter",
                    term = rendered,
                    blockedBoundary = selection.blockedBoundary?.label
                )
            )
        )
    }

    private fun handleProposalResponse(
        es: EngineState,
        slot: Slot?,
        response: ProposalResponse,
        now: Long
    ): ApplyResult {
        val s = es.state
        if (slot == null) return reject(es, CODE_NOT_ALLOWED, "Responses come from the phones.")
        if (s.phase != GamePhase.PROPOSAL && s.phase != GamePhase.WAITING_FOR_PROPOSAL_RESPONSES) {
            return reject(es, CODE_PHASE, "There is no proposal open.")
        }
        val current = s.negotiation.current ?: return reject(es, CODE_PHASE, "There is no proposal open.")
        if (slot in current.responses) {
            return ApplyResult(es, accepted = true, code = "DUPLICATE")
        }
        if (response == ProposalResponse.BUNDLE && s.regularTermsRemaining < 2) {
            return reject(es, CODE_INVALID, "A trade needs two free term slots and there is only one left.")
        }

        val undo = pushUndo(es)
        val responses = current.responses + (slot to response)
        var next = s.copy(
            phase = GamePhase.WAITING_FOR_PROPOSAL_RESPONSES,
            negotiation = s.negotiation.copy(current = current.copy(responses = responses))
        )
        if (responses.size == 2) next = resolveResponses(next, now)
        return ok(es, next, undo)
    }

    private fun resolveResponses(s: GameState, now: Long): GameState {
        val current = s.negotiation.current ?: return s
        val values = current.responses.values
        return when {
            ProposalResponse.REJECT in values -> declineAndReplace(s, now)
            ProposalResponse.COUNTEROFFER in values -> s.copy(phase = GamePhase.COUNTEROFFER_PRIVATE_SELECTION)
            ProposalResponse.BUNDLE in values -> beginBundleSelection(s)
            else -> beginConsideration(s, stronger = false, now = now)
        }
    }

    /** A rejection removes the term from the pool. It never consumes a contract slot. */
    private fun declineAndReplace(s: GameState, now: Long): GameState {
        val current = s.negotiation.current
        val declined = buildSet {
            current?.term?.termId?.let { add(it) }
            current?.bundledTerm?.termId?.let { add(it) }
        }
        val cleared = s.copy(
            negotiation = s.negotiation.copy(
                current = null,
                declinedTermIds = s.negotiation.declinedTermIds + declined
            ),
            timers = TimerEngine.empty
        )
        return if (cleared.negotiation.closingTurn != null) startClosing(cleared, now) else startProposal(cleared, now)
    }

    // ------------------------------------------------------------------ counteroffers

    private fun mayCounteroffer(current: CurrentProposal, slot: Slot): Boolean =
        current.responses[slot] == ProposalResponse.COUNTEROFFER

    private fun handlePickAmendment(es: EngineState, slot: Slot?, amendment: Amendment, now: Long): ApplyResult {
        val s = es.state
        if (slot == null) return reject(es, CODE_NOT_ALLOWED, "Amendments are chosen on a phone.")
        if (s.phase != GamePhase.COUNTEROFFER_PRIVATE_SELECTION) {
            return reject(es, CODE_PHASE, "No counteroffer is open.")
        }
        val current = s.negotiation.current ?: return reject(es, CODE_PHASE, "No proposal is open.")
        if (!mayCounteroffer(current, slot)) {
            return reject(es, CODE_NOT_ALLOWED, "Only the player who countered picks the amendment.")
        }
        if (current.amendmentBallot.isNotEmpty()) {
            return reject(es, CODE_PHASE, "Vote on the two amendments instead.")
        }
        if (slot in current.amendmentPicks) return ApplyResult(es, accepted = true, code = "DUPLICATE")

        val undo = pushUndo(es)
        val picks = current.amendmentPicks + (slot to amendment)
        val expected = current.responses.count { it.value == ProposalResponse.COUNTEROFFER }
        var updated = current.copy(amendmentPicks = picks)
        var next = s.copy(negotiation = s.negotiation.copy(current = updated))

        if (picks.size == expected) {
            val distinct = picks.values.distinct()
            next = if (distinct.size == 1) {
                applyChosenAmendment(next, distinct.first(), now)
                    ?: return reject(es, CODE_INVALID, "That amendment does not work for this term.")
            } else {
                updated = updated.copy(amendmentBallot = distinct)
                next.copy(negotiation = next.negotiation.copy(current = updated))
            }
        }
        return ok(es, next, undo)
    }

    private fun handleVoteAmendment(es: EngineState, slot: Slot?, amendment: Amendment, now: Long): ApplyResult {
        val s = es.state
        if (slot == null) return reject(es, CODE_NOT_ALLOWED, "Votes come from the phones.")
        val current = s.negotiation.current ?: return reject(es, CODE_PHASE, "No proposal is open.")
        if (s.phase != GamePhase.COUNTEROFFER_PRIVATE_SELECTION || current.amendmentBallot.isEmpty()) {
            return reject(es, CODE_PHASE, "There is no amendment ballot open.")
        }
        if (amendment !in current.amendmentBallot) return reject(es, CODE_INVALID, "That option is not on the ballot.")
        if (slot in current.amendmentVotes) return ApplyResult(es, accepted = true, code = "DUPLICATE")

        val undo = pushUndo(es)
        val votes = current.amendmentVotes + (slot to amendment)
        var next = s.copy(negotiation = s.negotiation.copy(current = current.copy(amendmentVotes = votes)))
        if (votes.size == 2) {
            next = if (votes.values.distinct().size == 1) {
                applyChosenAmendment(next, votes.values.first(), now)
                    ?: return reject(es, CODE_INVALID, "That amendment does not work for this term.")
            } else {
                // No agreement on the amendment: the original deal has no mutual agreement.
                declineAndReplace(next, now)
            }
        }
        return ok(es, next, undo)
    }

    /** Re-renders the term with the amendment applied. Returns null if it cannot be applied. */
    private fun applyChosenAmendment(s: GameState, amendment: Amendment, now: Long): GameState? {
        val current = s.negotiation.current ?: return null
        val ctx = GameContext.of(s)
        val term = ContentLibrary.termsById[current.term.termId] ?: return null
        val amendments = current.appliedAmendments + amendment
        val reversed = Amendment.ROLES_REVERSED in amendments
        val binding = PartyBinding(current.term.giver, current.term.receiver).let { if (reversed) it.reversed() else it }
        if (reversed) {
            val e = EligibilityEngine.evaluate(term, ctx, preferredBinding = binding)
            if (e !is Eligibility.Ok || e.binding != binding) return null
        }
        val conditions = EligibilityEngine.maybeConditions(term, binding, ctx)
        val rendered = Renderer.renderTerm(
            term = term,
            binding = binding,
            ctx = ctx,
            conditions = conditions,
            amendments = amendments.filter { it != Amendment.ROLES_REVERSED },
            extraAmendmentLabels = if (reversed) listOf(Amendment.ROLES_REVERSED.label) else emptyList()
        )
        return s.copy(
            phase = GamePhase.COUNTEROFFER_APPROVAL,
            negotiation = s.negotiation.copy(
                current = current.copy(
                    term = rendered,
                    appliedAmendments = amendments,
                    amendmentApproval = emptyMap()
                )
            )
        )
    }

    private fun handleApproveAmended(es: EngineState, slot: Slot?, approve: Boolean, now: Long): ApplyResult {
        val s = es.state
        if (slot == null) return reject(es, CODE_NOT_ALLOWED, "Votes come from the phones.")
        if (s.phase != GamePhase.COUNTEROFFER_APPROVAL) return reject(es, CODE_PHASE, "Nothing to approve.")
        val current = s.negotiation.current ?: return reject(es, CODE_PHASE, "No proposal is open.")
        if (slot in current.amendmentApproval) return ApplyResult(es, accepted = true, code = "DUPLICATE")

        val undo = pushUndo(es)
        val votes = current.amendmentApproval + (slot to approve)
        var next = s.copy(negotiation = s.negotiation.copy(current = current.copy(amendmentApproval = votes)))
        if (votes.size == 2) {
            next = if (votes.values.all { it }) {
                beginConsideration(next, stronger = false, now = now)
            } else {
                declineAndReplace(next, now)
            }
        }
        return ok(es, next, undo)
    }

    // ------------------------------------------------------------------ bundle trades

    private fun beginBundleSelection(s: GameState): GameState {
        val current = s.negotiation.current ?: return s
        val initiator = current.responses.entries.firstOrNull { it.value == ProposalResponse.BUNDLE }?.key
            ?: return s
        val ctx = GameContext.of(s)
        val candidates = ProposalSelector.bundleCandidates(s, ctx, current.term.termId)
        if (candidates.isEmpty()) {
            // Nothing compatible to trade with: fall back to signing the single term.
            return beginConsideration(s, stronger = false, now = s.updatedAtMs)
        }
        return s.copy(
            phase = GamePhase.BUNDLE_PRIVATE_SELECTION,
            negotiation = s.negotiation.copy(
                current = current.copy(
                    bundleInitiator = initiator,
                    bundleCandidateIds = candidates.map { it.term.id }
                )
            )
        )
    }

    private fun handlePickBundle(es: EngineState, slot: Slot?, termId: String, now: Long): ApplyResult {
        val s = es.state
        if (s.phase != GamePhase.BUNDLE_PRIVATE_SELECTION) return reject(es, CODE_PHASE, "No trade is open.")
        val current = s.negotiation.current ?: return reject(es, CODE_PHASE, "No proposal is open.")
        if (slot != null && slot != current.bundleInitiator) {
            return reject(es, CODE_NOT_ALLOWED, "Only the player who proposed the trade picks the second term.")
        }
        if (termId !in current.bundleCandidateIds) return reject(es, CODE_INVALID, "That term is not on offer.")
        if (s.regularTermsRemaining < 2) return reject(es, CODE_INVALID, "There is not enough room left for a trade.")

        val ctx = GameContext.of(s)
        val term = ContentLibrary.termsById[termId] ?: return reject(es, CODE_INVALID, "Unknown term.")
        // Same binding preference the candidate list was filtered on, or the trade could be
        // signed the other way round from the one that kept the benefit alternating.
        val e = EligibilityEngine.evaluate(term, ctx, ProposalSelector.preferredBinding(s, term))
        if (e !is Eligibility.Ok) return reject(es, CODE_INVALID, "That term is no longer compatible.")

        val undo = pushUndo(es)
        val rendered = Renderer.renderTerm(
            term, e.binding, ctx, EligibilityEngine.maybeConditions(term, e.binding, ctx)
        )
        val next = s.copy(
            phase = GamePhase.BUNDLE_APPROVAL,
            negotiation = s.negotiation.copy(
                seenTermIds = s.negotiation.seenTermIds + termId,
                current = current.copy(bundledTerm = rendered, bundleApproval = emptyMap())
            )
        )
        return ok(es, next, undo)
    }

    private fun handleVoteBundle(es: EngineState, slot: Slot?, approve: Boolean, now: Long): ApplyResult {
        val s = es.state
        if (s.phase != GamePhase.BUNDLE_APPROVAL) return reject(es, CODE_PHASE, "No trade to vote on.")
        if (slot == null) return reject(es, CODE_NOT_ALLOWED, "Votes come from the phones.")
        val current = s.negotiation.current ?: return reject(es, CODE_PHASE, "No proposal is open.")
        if (slot in current.bundleApproval) return ApplyResult(es, accepted = true, code = "DUPLICATE")

        val undo = pushUndo(es)
        val votes = current.bundleApproval + (slot to approve)
        var next = s.copy(negotiation = s.negotiation.copy(current = current.copy(bundleApproval = votes)))
        if (votes.size == 2) {
            next = if (votes.values.all { it }) {
                beginConsideration(next, stronger = true, now = now)
            } else {
                declineAndReplace(next, now)
            }
        }
        return ok(es, next, undo)
    }

    // ------------------------------------------------------------------ consideration

    private fun beginConsideration(s: GameState, stronger: Boolean, now: Long): GameState {
        val current = s.negotiation.current ?: return s
        val ctx = GameContext.of(s)
        val parts = buildList {
            ContentLibrary.termsById[current.term.termId]?.let {
                add(it to PartyBinding(current.term.giver, current.term.receiver))
            }
            current.bundledTerm?.let { b ->
                ContentLibrary.termsById[b.termId]?.let { add(it to PartyBinding(b.giver, b.receiver)) }
            }
        }
        val beneficiary = if (parts.size > 1) {
            BenefitAnalysis.combinedBeneficiary(parts)
        } else {
            current.term.beneficiary
        }
        val mutual = beneficiary == null
        val performer = beneficiary ?: current.term.giver
        val recipient = beneficiary?.other ?: current.term.receiver

        // The closing step is the one players deliberate over most, so it offers a wider list.
        val options = ConsiderationSelector
            .options(
                s, ctx, performer, recipient, mutualRequired = mutual, stronger = stronger,
                limit = if (current.term.climax) 12 else 10
            )
            .map { Renderer.renderConsideration(it, performer, recipient, ctx) }

        val phase = if (current.term.climax) {
            GamePhase.CLOSING_TERM_CONSIDERATION
        } else {
            GamePhase.CONSIDERATION_PRIVATE_SELECTION
        }
        return s.copy(
            phase = phase,
            timers = TimerEngine.empty,
            negotiation = s.negotiation.copy(
                current = current.copy(
                    beneficiary = beneficiary,
                    considerationOptions = options,
                    consideration = null,
                    considerationConfirmed = emptyMap(),
                    signatures = emptyMap()
                )
            )
        )
    }

    private fun handlePickConsideration(es: EngineState, slot: Slot?, actionId: String, now: Long): ApplyResult {
        val s = es.state
        if (s.phase != GamePhase.CONSIDERATION_PRIVATE_SELECTION && s.phase != GamePhase.CLOSING_TERM_CONSIDERATION) {
            return reject(es, CODE_PHASE, "Consideration is not being chosen right now.")
        }
        val current = s.negotiation.current ?: return reject(es, CODE_PHASE, "No proposal is open.")
        val mutual = current.beneficiary == null
        // The one who is owed says what he is owed. The beneficiary performs it, but choosing it
        // as well would let him pay himself.
        if (!mutual && slot != null && slot != current.beneficiary?.other) {
            return reject(es, CODE_NOT_ALLOWED, "Only the player who is owed consideration chooses it.")
        }
        val chosen = current.considerationOptions.firstOrNull { it.actionId == actionId }
            ?: return reject(es, CODE_INVALID, "That consideration is not on offer.")

        val undo = pushUndo(es)
        val board = TimerEngine.board(
            context = chosen.instruction,
            timers = chosen.timers,
            controller = if (mutual) null else chosen.performer,
            bothMayControl = mutual,
            completer = if (mutual) null else chosen.recipient
        )
        val next = s.copy(
            phase = GamePhase.CONSIDERATION_PUBLIC_EXECUTION,
            timers = board,
            negotiation = s.negotiation.copy(
                current = current.copy(
                    consideration = chosen,
                    considerationConfirmed = emptyMap(),
                    signatures = emptyMap()
                )
            )
        )
        return ok(es, next, undo)
    }

    private fun handleConsiderationPerformed(es: EngineState, slot: Slot?, now: Long): ApplyResult {
        val s = es.state
        if (s.phase != GamePhase.CONSIDERATION_PUBLIC_EXECUTION) {
            return reject(es, CODE_PHASE, "No consideration is being performed.")
        }
        val current = s.negotiation.current ?: return reject(es, CODE_PHASE, "No proposal is open.")
        val consideration = current.consideration ?: return reject(es, CODE_PHASE, "No consideration chosen.")
        val performers = if (consideration.mutual) {
            setOf(consideration.performer, consideration.recipient)
        } else {
            setOf(consideration.performer)
        }
        val actor = slot ?: performers.firstOrNull { it !in current.considerationConfirmed }
        if (actor == null || actor !in performers) {
            return reject(es, CODE_NOT_ALLOWED, "Only the performer marks the action done.")
        }
        if (actor in current.considerationConfirmed) return ApplyResult(es, accepted = true, code = "DUPLICATE")

        val confirmed = current.considerationConfirmed + (actor to true)
        var next = s.copy(negotiation = s.negotiation.copy(current = current.copy(considerationConfirmed = confirmed)))
        if (performers.all { confirmed[it] == true }) {
            next = next.copy(
                phase = GamePhase.WAITING_FOR_SIGNATURE_CONFIRMATION,
                timers = TimerEngine.pauseAll(next.timers, now)
            )
        }
        return ok(es, next)
    }

    private fun handleConfirmSignature(es: EngineState, slot: Slot?, earned: Boolean, now: Long): ApplyResult {
        val s = es.state
        if (s.phase != GamePhase.WAITING_FOR_SIGNATURE_CONFIRMATION) {
            return reject(es, CODE_PHASE, "There is no signature to confirm.")
        }
        val current = s.negotiation.current ?: return reject(es, CODE_PHASE, "No proposal is open.")
        val consideration = current.consideration ?: return reject(es, CODE_PHASE, "No consideration performed.")
        val confirmers = if (consideration.mutual) {
            setOf(consideration.performer, consideration.recipient)
        } else {
            setOf(consideration.recipient)
        }
        val actor = slot ?: confirmers.firstOrNull { it !in current.signatures }
        if (actor == null || actor !in confirmers) {
            return reject(es, CODE_NOT_ALLOWED, "Only the player receiving consideration confirms it.")
        }
        if (actor in current.signatures) return ApplyResult(es, accepted = true, code = "DUPLICATE")

        if (!earned) {
            // Repeat or choose another consideration; nothing is consumed.
            val reset = s.copy(
                phase = if (current.term.climax) {
                    GamePhase.CLOSING_TERM_CONSIDERATION
                } else {
                    GamePhase.CONSIDERATION_PRIVATE_SELECTION
                },
                timers = TimerEngine.empty,
                negotiation = s.negotiation.copy(
                    current = current.copy(
                        consideration = null,
                        considerationConfirmed = emptyMap(),
                        signatures = emptyMap()
                    )
                )
            )
            return ok(es, reset)
        }

        val signatures = current.signatures + (actor to true)
        var next = s.copy(negotiation = s.negotiation.copy(current = current.copy(signatures = signatures)))
        if (confirmers.all { signatures[it] == true }) {
            next = signTerm(next, now)
        }
        return ok(es, next, undo = es.undoStack)
    }

    private fun signTerm(s: GameState, now: Long): GameState {
        val current = s.negotiation.current ?: return s
        val consideration = current.consideration
        val receipt = consideration?.let {
            ConsiderationReceipt(
                id = "r${s.negotiation.receipts.size + 1}",
                actionId = it.actionId,
                title = it.title,
                instruction = it.instruction,
                performer = it.performer,
                recipient = it.recipient,
                mutual = it.mutual,
                completedAtMs = now,
                forTermIds = listOfNotNull(current.term.termId, current.bundledTerm?.termId),
                bundle = current.bundledTerm != null
            )
        }
        val closingFor = if (current.term.climax) current.term.receiver else null
        val signed = SignedTerm(
            index = s.negotiation.signed.size,
            act = Act.ofLevel(current.term.level).level,
            term = current.term,
            bundledTerm = current.bundledTerm,
            considerationReceiptId = receipt?.id,
            signedBySlots = listOf(Slot.PLAYER_1, Slot.PLAYER_2),
            signedAtMs = now,
            closingFor = closingFor
        )
        val negotiation = s.negotiation.copy(
            signed = s.negotiation.signed + signed,
            receipts = s.negotiation.receipts + listOfNotNull(receipt),
            recentConsiderationIds = (s.negotiation.recentConsiderationIds + listOfNotNull(consideration?.actionId))
                .takeLast(12),
            current = null
        )
        return s.copy(phase = GamePhase.TERM_SIGNED, negotiation = negotiation, timers = TimerEngine.empty)
    }

    private fun handleContinue(es: EngineState, now: Long): ApplyResult {
        val s = es.state
        return when (s.phase) {
            GamePhase.TERM_SIGNED -> {
                val next = when {
                    s.negotiation.closingTurn != null -> startClosing(s, now)
                    s.allRegularTermsSigned -> startClosing(s, now)
                    else -> startProposal(s, now)
                }
                // A signed term can never be reopened: the Back stack is truncated here.
                ok(es, next, undo = emptyList())
            }

            GamePhase.FINAL_CONTRACT_REVIEW -> ok(es, beginFinaleSelection(s))
            else -> reject(es, CODE_PHASE, "Nothing to continue.")
        }
    }

    // ------------------------------------------------------------------ closing terms

    private fun startClosing(s: GameState, now: Long): GameState {
        val done = s.negotiation.signedClosing.mapNotNull { it.closingFor }.toSet()
        val next = Slot.entries.firstOrNull { it !in done }
            ?: return s.copy(phase = GamePhase.FINAL_CONTRACT_REVIEW, negotiation = s.negotiation.copy(closingTurn = null))
        val ctx = GameContext.of(s)
        val candidates = ProposalSelector.closingCandidates(s, ctx, next)
        return s.copy(
            phase = GamePhase.CLOSING_TERM_SELECTION,
            timers = TimerEngine.empty,
            negotiation = s.negotiation.copy(
                closingTurn = next,
                closingOptionIds = candidates.map { it.term.id },
                current = null
            )
        )
    }

    private fun handlePickClosing(es: EngineState, slot: Slot?, termId: String, now: Long): ApplyResult {
        val s = es.state
        if (s.phase != GamePhase.CLOSING_TERM_SELECTION) return reject(es, CODE_PHASE, "No closing term is being chosen.")
        val turn = s.negotiation.closingTurn ?: return reject(es, CODE_PHASE, "No closing term is being chosen.")
        if (slot != null && slot != turn) {
            return reject(es, CODE_NOT_ALLOWED, "This closing term belongs to the other player.")
        }
        if (s.negotiation.current != null) return reject(es, CODE_PHASE, "The closing term is already chosen.")
        if (termId !in s.negotiation.closingOptionIds) return reject(es, CODE_INVALID, "That term is not on offer.")

        val ctx = GameContext.of(s)
        val term = ContentLibrary.termsById[termId] ?: return reject(es, CODE_INVALID, "Unknown term.")
        val binding = PartyBinding(turn.other, turn)
        val e = EligibilityEngine.evaluate(term, ctx, preferredBinding = binding)
        if (e !is Eligibility.Ok || e.binding != binding) {
            return reject(es, CODE_INVALID, "That closing term is no longer compatible.")
        }
        val undo = pushUndo(es)
        val rendered = Renderer.renderTerm(term, binding, ctx, EligibilityEngine.maybeConditions(term, binding, ctx))
        val next = s.copy(
            negotiation = s.negotiation.copy(
                seenTermIds = s.negotiation.seenTermIds + termId,
                current = CurrentProposal(proposalId = "closing-${turn.name}", term = rendered)
            )
        )
        return ok(es, next, undo)
    }

    private fun handleVoteClosing(es: EngineState, slot: Slot?, approve: Boolean, now: Long): ApplyResult {
        val s = es.state
        if (s.phase != GamePhase.CLOSING_TERM_SELECTION) return reject(es, CODE_PHASE, "Nothing to vote on.")
        if (slot == null) return reject(es, CODE_NOT_ALLOWED, "Votes come from the phones.")
        val current = s.negotiation.current ?: return reject(es, CODE_PHASE, "No closing term chosen yet.")
        if (slot in current.signatures) return ApplyResult(es, accepted = true, code = "DUPLICATE")

        val undo = pushUndo(es)
        val votes = current.signatures + (slot to approve)
        var next = s.copy(negotiation = s.negotiation.copy(current = current.copy(signatures = votes)))
        if (votes.size == 2) {
            next = if (votes.values.all { it }) {
                beginConsideration(next.copy(negotiation = next.negotiation.copy(current = current.copy(signatures = emptyMap()))), stronger = true, now = now)
            } else {
                val declined = s.negotiation.declinedTermIds + current.term.termId
                startClosing(
                    s.copy(negotiation = s.negotiation.copy(current = null, declinedTermIds = declined)),
                    now
                )
            }
        }
        return ok(es, next, undo)
    }

    // ------------------------------------------------------------------ finale

    private fun beginFinaleSelection(s: GameState): GameState = when (s.setup.finaleFormat) {
        com.thecontract.core.model.FinaleFormat.UNINTERRUPTED ->
            startExecution(s, FinaleOrder.SMOOTH_ESCALATION)
        com.thecontract.core.model.FinaleFormat.DOMINANT_PRIVATE ->
            s.copy(phase = GamePhase.PRIVATE_DOMINANT_FINALE_SELECTION, finale = FinaleState())
        com.thecontract.core.model.FinaleFormat.THREE_ORDERS ->
            s.copy(phase = GamePhase.FINALE_ORDER_SELECTION, finale = FinaleState())
    }

    private fun handlePickFinaleOrder(es: EngineState, slot: Slot?, order: FinaleOrder, now: Long): ApplyResult {
        val s = es.state
        return when (s.phase) {
            GamePhase.PRIVATE_DOMINANT_FINALE_SELECTION -> {
                if (slot != null && slot != s.setup.dominant) {
                    reject(es, CODE_NOT_ALLOWED, "Only the Dominant chooses the order.")
                } else {
                    ok(es, startExecution(s, order))
                }
            }

            GamePhase.FINALE_ORDER_SELECTION -> {
                if (slot == null) return reject(es, CODE_NOT_ALLOWED, "Votes come from the phones.")
                val finale = s.finale
                if (finale.revealedBallot.isNotEmpty()) {
                    // Tie-break: only the Dominant decides between the two revealed options.
                    if (slot != s.setup.dominant) {
                        return reject(es, CODE_NOT_ALLOWED, "The Dominant makes the final choice.")
                    }
                    if (order !in finale.revealedBallot) {
                        return reject(es, CODE_INVALID, "That order is not one of the two on the table.")
                    }
                    return ok(es, startExecution(s, order))
                }
                if (slot in finale.orderVotes) return ApplyResult(es, accepted = true, code = "DUPLICATE")
                val votes = finale.orderVotes + (slot to order)
                var next = s.copy(finale = finale.copy(orderVotes = votes))
                if (votes.size == 2) {
                    val distinct = votes.values.distinct()
                    next = if (distinct.size == 1) {
                        startExecution(next, distinct.first())
                    } else {
                        next.copy(finale = next.finale.copy(revealedBallot = distinct))
                    }
                }
                ok(es, next)
            }

            else -> reject(es, CODE_PHASE, "The finale order is not being chosen.")
        }
    }

    /**
     * Builds the execution order (section 39). Terms held back by a "Save for finale"
     * amendment move to the end of the regular block, and the two climax terms are always
     * last — ordinary reordering can never move them earlier.
     */
    private fun startExecution(s: GameState, order: FinaleOrder): GameState {
        val signed = s.negotiation.signed
        val regularIdx = signed.indices.filter { signed[it].closingFor == null }
        val closingIdx = signed.indices.filter { signed[it].closingFor != null }

        val (deferred, normal) = regularIdx.partition { signed[it].term.deferToEnd }
        val orderedNormal = when (order) {
            FinaleOrder.SIGNED_ORDER -> normal
            FinaleOrder.SMOOTH_ESCALATION -> normal.sortedBy { signed[it].term.level }
            FinaleOrder.DOMINANTS_CUT -> normal.shuffled(Random(s.sessionId.hashCode().toLong() * 31 + 7))
        }
        val orderedDeferred = deferred.sortedBy { signed[it].term.level }

        // Section 37: with anal penetration in the contract, the top's climax term comes first.
        val orderedClosing = closingIdx.sortedByDescending { signed[it].term.analPenetration }

        return s.copy(
            phase = GamePhase.FINAL_EXECUTION,
            timers = TimerEngine.empty,
            finale = s.finale.copy(
                chosenOrder = order,
                executionOrder = orderedNormal + orderedDeferred + orderedClosing,
                stepIndex = 0,
                stepsCompleted = emptySet(),
                stepStarted = false
            )
        )
    }

    private fun handleStartExecution(es: EngineState, now: Long): ApplyResult {
        val s = es.state
        if (s.phase != GamePhase.FINAL_EXECUTION) return reject(es, CODE_PHASE, "Not in final execution.")
        return handleExecutionCommand(es, null, "begin", now)
    }

    private fun currentExecutionTerm(s: GameState): SignedTerm? {
        val idx = s.finale.executionOrder.getOrNull(s.finale.stepIndex) ?: return null
        return s.negotiation.signed.getOrNull(idx)
    }

    private fun handleExecutionCommand(es: EngineState, slot: Slot?, command: String, now: Long): ApplyResult {
        val s = es.state
        if (s.phase != GamePhase.FINAL_EXECUTION) return reject(es, CODE_PHASE, "Not in final execution.")
        val signedTerm = currentExecutionTerm(s) ?: return reject(es, CODE_INVALID, "No term at this step.")
        val term = signedTerm.term

        return when (command) {
            "begin" -> {
                val board = TimerEngine.board(
                    context = term.instruction,
                    timers = term.timers + (signedTerm.bundledTerm?.timers ?: emptyList()),
                    controller = if (term.mutual) null else term.giver,
                    bothMayControl = term.mutual,
                    completer = if (term.mutual) null else term.receiver
                )
                ok(es, s.copy(timers = board, finale = s.finale.copy(stepStarted = true)))
            }

            "complete" -> {
                if (!TimerEngine.mayComplete(s.timers, slot)) {
                    reject(es, CODE_NOT_ALLOWED, "The other player marks this one complete.")
                } else {
                    ok(
                        es,
                        s.copy(
                            timers = TimerEngine.pauseAll(s.timers, now),
                            finale = s.finale.copy(stepsCompleted = s.finale.stepsCompleted + s.finale.stepIndex)
                        )
                    )
                }
            }

            "next" -> {
                val last = s.finale.stepIndex >= s.finale.executionOrder.lastIndex
                if (last) {
                    ok(es, s.copy(phase = GamePhase.COMPLETED, completedAtMs = now, timers = TimerEngine.empty))
                } else {
                    ok(
                        es,
                        s.copy(
                            timers = TimerEngine.empty,
                            finale = s.finale.copy(stepIndex = s.finale.stepIndex + 1, stepStarted = false)
                        )
                    )
                }
            }

            "prev" -> {
                if (s.finale.stepIndex == 0) {
                    reject(es, CODE_INVALID, "This is the first term.")
                } else {
                    ok(
                        es,
                        s.copy(
                            timers = TimerEngine.empty,
                            finale = s.finale.copy(stepIndex = s.finale.stepIndex - 1, stepStarted = false)
                        )
                    )
                }
            }

            else -> reject(es, CODE_INVALID, "Unknown command.")
        }
    }

    // ------------------------------------------------------------------ timers

    private fun handleTimerCommand(
        es: EngineState,
        slot: Slot?,
        timerId: String,
        command: String,
        now: Long
    ): ApplyResult {
        val s = es.state
        if (s.timers.timer(timerId) == null) return reject(es, CODE_INVALID, "Unknown timer.")
        if (!TimerEngine.mayControl(s.timers, slot)) {
            return reject(es, CODE_NOT_ALLOWED, "The other player controls this timer.")
        }
        val board = when (command) {
            "start", "resume" -> TimerEngine.start(s.timers, timerId, now)
            "pause" -> TimerEngine.pause(s.timers, timerId, now)
            "reset" -> TimerEngine.reset(s.timers, timerId)
            else -> return reject(es, CODE_INVALID, "Unknown timer command.")
        }
        return ok(es, s.copy(timers = board))
    }

    private fun handleStopAll(es: EngineState, slot: Slot?, now: Long): ApplyResult {
        val s = es.state
        if (!TimerEngine.mayControl(s.timers, slot)) {
            return reject(es, CODE_NOT_ALLOWED, "The other player controls these timers.")
        }
        return ok(es, s.copy(timers = TimerEngine.pauseAll(s.timers, now)))
    }

    /** Server tick: flips expired timers to completed. Never persisted per second. */
    fun tick(es: EngineState, now: Long): EngineState {
        if (!es.state.timers.anyRunning) return es
        val settled = TimerEngine.settle(es.state.timers, now)
        if (settled == es.state.timers) return es
        return es.copy(state = es.state.copy(timers = settled))
    }

    // ------------------------------------------------------------------ pause & back

    private fun handleGlobalPause(es: EngineState, now: Long): ApplyResult {
        val s = es.state
        if (s.pause.paused) return ApplyResult(es, accepted = true, code = "DUPLICATE")
        return ok(
            es,
            s.copy(
                phase = GamePhase.PAUSED,
                timers = TimerEngine.pauseAll(s.timers, now),
                pause = PauseState(paused = true, resumedPhase = s.phase)
            )
        )
    }

    private fun handleResumeVote(es: EngineState, slot: Slot?, confirm: Boolean, now: Long): ApplyResult {
        val s = es.state
        if (!s.pause.paused) return reject(es, CODE_PHASE, "The session is not paused.")
        if (!confirm) return ApplyResult(es, accepted = true)
        val pause = if (slot == null) {
            // TV remote override: used when a phone is disconnected.
            s.pause.copy(tvOverrideConfirmed = true)
        } else {
            s.pause.copy(resumeConfirmations = s.pause.resumeConfirmations + (slot to true))
        }
        val bothConfirmed = Slot.entries.all { pause.resumeConfirmations[it] == true }
        return if (bothConfirmed || pause.tvOverrideConfirmed) {
            ok(
                es,
                s.copy(
                    phase = pause.resumedPhase ?: GamePhase.PROPOSAL,
                    pause = PauseState()
                )
            )
        } else {
            ok(es, s.copy(pause = pause))
        }
    }

    /**
     * Back navigation (section 32). Implemented as a snapshot stack, which makes it impossible
     * for Back to duplicate a term, a vote or a receipt, or to consume progress. The stack is
     * truncated at every signature, so a signed term can never be reopened.
     */
    private fun handleBack(es: EngineState, now: Long): ApplyResult {
        val previous = es.undoStack.lastOrNull()
            ?: return reject(es, CODE_INVALID, "There is nothing to go back to.")
        if (previous.negotiation.signed.size != es.state.negotiation.signed.size) {
            return reject(es, CODE_INVALID, "Signed terms cannot be reopened.")
        }
        // Leaving a timed screen always stops its timers.
        val restored = previous.copy(
            version = es.state.version,
            timers = TimerEngine.resetAll(previous.timers),
            processedActionIds = es.state.processedActionIds
        )
        return ok(es, restored, undo = es.undoStack.dropLast(1))
    }
}
