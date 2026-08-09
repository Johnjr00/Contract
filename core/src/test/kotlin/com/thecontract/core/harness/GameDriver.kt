package com.thecontract.core.harness

import com.thecontract.core.model.Amendment
import com.thecontract.core.model.Answer
import com.thecontract.core.model.FinaleOrder
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.GameState
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.ProposalResponse
import com.thecontract.core.model.SharedSetup
import com.thecontract.core.model.Slot
import com.thecontract.core.protocol.ApproveAmended
import com.thecontract.core.protocol.Back
import com.thecontract.core.protocol.ConfirmSignature
import com.thecontract.core.protocol.ConsiderationPerformed
import com.thecontract.core.protocol.ContinueFlow
import com.thecontract.core.protocol.ExecutionCommand
import com.thecontract.core.protocol.PickAmendment
import com.thecontract.core.protocol.PickBundle
import com.thecontract.core.protocol.PickClosing
import com.thecontract.core.protocol.PickConsideration
import com.thecontract.core.protocol.PickFinaleOrder
import com.thecontract.core.protocol.ProposalRespond
import com.thecontract.core.protocol.ResumeVote
import com.thecontract.core.protocol.SaveProfile
import com.thecontract.core.protocol.SubmitSetup
import com.thecontract.core.protocol.TimerCommand
import com.thecontract.core.protocol.VoteAmendment
import com.thecontract.core.protocol.VoteBundle
import com.thecontract.core.protocol.VoteClosing

/**
 * Scripted behaviour for one automated game. Indices refer to the server's proposal counter, so
 * "counteroffer on the third proposal" means exactly that regardless of what is proposed.
 */
data class Plan(
    val rejectAt: Set<Int> = emptySet(),
    val counterAt: Set<Int> = emptySet(),
    val bothCounterAt: Set<Int> = emptySet(),
    val bundleAt: Set<Int> = emptySet(),
    val backAt: Set<Int> = emptySet(),
    val backAtConsideration: Set<Int> = emptySet(),
    val repeatConsiderationAt: Set<Int> = emptySet(),
    val finaleVotes: Map<Slot, FinaleOrder> = mapOf(
        Slot.PLAYER_1 to FinaleOrder.SMOOTH_ESCALATION,
        Slot.PLAYER_2 to FinaleOrder.SMOOTH_ESCALATION
    ),
    val dominantFinale: FinaleOrder = FinaleOrder.DOMINANTS_CUT
)

/**
 * Drives a complete game from pairing to completion through the wire protocol, using two
 * simulated phones. Every observation the driver makes about "who should act next" comes from
 * the phone views, not from engine internals, so a view that fails to offer the right control
 * makes the test hang and then fail rather than silently pass.
 */
class GameDriver(
    private val h: Harness,
    var p1: SimulatedPhone,
    var p2: SimulatedPhone,
    private val plan: Plan = Plan()
) {

    /** Swaps in a phone that has reconnected as a new browser session. */
    fun replacePhone(slot: Slot, phone: SimulatedPhone) {
        if (slot == Slot.PLAYER_1) p1 = phone else p2 = phone
    }

    /** Performs exactly one driver step; used by tests that interleave their own events. */
    fun runOneStep() {
        val s = state()
        if (s.phase != GamePhase.COMPLETED) step(s)
    }

    val timerStarts = mutableListOf<String>()
    val considerationTitles = mutableListOf<String>()
    val backsTaken = mutableListOf<Int>()
    val backsFromConsideration = mutableListOf<Int>()
    var repeatsRequested = 0
        private set

    private fun state(): GameState = h.state()

    private fun phone(slot: Slot) = if (slot == Slot.PLAYER_1) p1 else p2

    fun submitSetup(setup: SharedSetup): Boolean = p1.act(SubmitSetup(setup))

    fun fillProfile(slot: Slot, answers: Map<String, Answer>, conditions: Map<String, String> = emptyMap()) {
        val full = PreferenceLibrary.all.associate { it.id to (answers[it.id] ?: Answer.MAYBE) }
        phone(slot).act(SaveProfile(full, conditions, complete = true))
    }

    /** Runs until the game completes or [maxSteps] passes with no progress. */
    fun run(maxSteps: Int = 4000): GameState {
        var steps = 0
        var lastVersion = -1L
        var idle = 0
        while (steps++ < maxSteps) {
            val s = state()
            if (s.phase == GamePhase.COMPLETED) return s
            step(s)
            val v = state().version
            if (v == lastVersion) {
                if (++idle > 3) {
                    error("Game stalled in ${state().phase} at version $v after $steps steps")
                }
            } else {
                idle = 0
            }
            lastVersion = v
        }
        error("Game did not complete within $maxSteps steps; stuck in ${state().phase}")
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    private fun step(s: GameState) {
        val n = s.negotiation
        val current = n.current
        val counter = n.proposalCounter

        when (s.phase) {
            GamePhase.PAUSED -> {
                p1.act(ResumeVote(true))
                p2.act(ResumeVote(true))
            }

            GamePhase.PRIVATE_PROFILES, GamePhase.WAITING_FOR_PROFILES -> {
                Slot.entries.filter { !s.profile(it).complete }.forEach { slot ->
                    fillProfile(slot, s.profile(slot).answers)
                }
            }

            GamePhase.PROPOSAL, GamePhase.WAITING_FOR_PROPOSAL_RESPONSES -> {
                if (current == null) return
                if (counter in plan.backAt && counter !in backsTaken) {
                    // One player answers, then backs out before the other has answered. Nothing
                    // may be duplicated or consumed by this.
                    backsTaken += counter
                    p1.act(ProposalRespond(ProposalResponse.SIGN))
                    check(p1.act(Back)) { "Back was refused during a proposal" }
                    return
                }
                Slot.entries.filter { it !in current.responses }.forEach { slot ->
                    phone(slot).act(ProposalRespond(responseFor(slot, counter, s)))
                }
            }

            GamePhase.COUNTEROFFER_PRIVATE_SELECTION -> {
                if (current == null) return
                if (current.amendmentBallot.isNotEmpty()) {
                    Slot.entries.filter { it !in current.amendmentVotes }.forEach { slot ->
                        phone(slot).act(VoteAmendment(current.amendmentBallot.first()))
                    }
                } else {
                    Slot.entries
                        .filter { current.responses[it] == ProposalResponse.COUNTEROFFER && it !in current.amendmentPicks }
                        .forEach { slot ->
                            val offered = phone(slot).choiceIdsStartingWith("pick_amendment:")
                            val amendment = offered.firstOrNull()
                                ?.removePrefix("pick_amendment:")
                                ?.let { name -> Amendment.entries.first { it.name == name } }
                                ?: Amendment.GENTLER
                            phone(slot).act(PickAmendment(amendment))
                        }
                }
            }

            GamePhase.COUNTEROFFER_APPROVAL -> {
                if (current == null) return
                Slot.entries.filter { it !in current.amendmentApproval }.forEach { slot ->
                    phone(slot).act(ApproveAmended(true))
                }
            }

            GamePhase.BUNDLE_PRIVATE_SELECTION -> {
                val initiator = current?.bundleInitiator ?: return
                val termId = current.bundleCandidateIds.firstOrNull() ?: return
                phone(initiator).act(PickBundle(termId))
            }

            GamePhase.BUNDLE_APPROVAL -> {
                if (current == null) return
                Slot.entries.filter { it !in current.bundleApproval }.forEach { slot ->
                    phone(slot).act(VoteBundle(true))
                }
            }

            GamePhase.CONSIDERATION_PRIVATE_SELECTION, GamePhase.CLOSING_TERM_CONSIDERATION -> {
                if (current == null) return
                // The player who gained less from the term is the one who names the payment.
                val chooser = current.beneficiary?.other ?: Slot.PLAYER_1
                if (counter in plan.backAtConsideration && counter !in backsFromConsideration) {
                    backsFromConsideration += counter
                    check(phone(chooser).act(Back)) { "Back was refused during consideration choice" }
                    return
                }
                val options = phone(chooser).choiceIdsStartingWith("pick_consideration:")
                check(options.isNotEmpty()) {
                    "no consideration offered to $chooser in ${s.phase} (options on server: " +
                        "${current.considerationOptions.size})"
                }
                val actionId = options[counter % options.size].removePrefix("pick_consideration:")
                considerationTitles += actionId
                phone(chooser).act(PickConsideration(actionId))
            }

            GamePhase.CONSIDERATION_PUBLIC_EXECUTION -> {
                runTimers(s)
                val consideration = current?.consideration ?: return
                val performers = if (consideration.mutual) {
                    listOf(consideration.performer, consideration.recipient)
                } else {
                    listOf(consideration.performer)
                }
                performers.filter { it !in current.considerationConfirmed }
                    .forEach { phone(it).act(ConsiderationPerformed) }
            }

            GamePhase.WAITING_FOR_SIGNATURE_CONFIRMATION -> {
                val consideration = current?.consideration ?: return
                val confirmers = if (consideration.mutual) {
                    listOf(consideration.performer, consideration.recipient)
                } else {
                    listOf(consideration.recipient)
                }
                val repeat = counter in plan.repeatConsiderationAt && repeatsRequested == 0
                if (repeat) {
                    repeatsRequested++
                    phone(confirmers.first()).act(ConfirmSignature(false))
                    return
                }
                confirmers.filter { it !in current.signatures }.forEach {
                    phone(it).act(ConfirmSignature(true))
                }
            }

            GamePhase.TERM_SIGNED -> p1.act(ContinueFlow)

            GamePhase.CLOSING_TERM_SELECTION -> {
                if (current == null) {
                    val turn = n.closingTurn ?: return
                    val options = phone(turn).choiceIdsStartingWith("pick_closing:")
                    check(options.isNotEmpty()) { "no closing term offered to $turn" }
                    phone(turn).act(PickClosing(options.first().removePrefix("pick_closing:")))
                } else {
                    Slot.entries.filter { it !in current.signatures }.forEach {
                        phone(it).act(VoteClosing(true))
                    }
                }
            }

            GamePhase.FINAL_CONTRACT_REVIEW -> p1.act(ContinueFlow)

            GamePhase.FINALE_ORDER_SELECTION -> {
                if (s.finale.revealedBallot.isNotEmpty()) {
                    phone(s.setup.dominant).act(PickFinaleOrder(s.finale.revealedBallot.first()))
                } else {
                    Slot.entries.filter { it !in s.finale.orderVotes }.forEach { slot ->
                        phone(slot).act(PickFinaleOrder(plan.finaleVotes.getValue(slot)))
                    }
                }
            }

            GamePhase.PRIVATE_DOMINANT_FINALE_SELECTION ->
                phone(s.setup.dominant).act(PickFinaleOrder(plan.dominantFinale))

            GamePhase.FINAL_EXECUTION -> {
                if (!s.finale.stepStarted) {
                    p1.act(ExecutionCommand("begin"))
                    return
                }
                runTimers(s)
                if (s.finale.stepIndex !in s.finale.stepsCompleted) {
                    val completer = s.timers.completerSlot ?: Slot.PLAYER_1
                    phone(completer).act(ExecutionCommand("complete"))
                } else {
                    p1.act(ExecutionCommand("next"))
                }
            }

            GamePhase.PLAYER_1_SETUP, GamePhase.WAITING_FOR_PLAYER_2,
            GamePhase.PAIRING, GamePhase.NO_SESSION, GamePhase.COMPLETED -> Unit
        }
    }

    private fun responseFor(slot: Slot, counter: Int, s: GameState): ProposalResponse = when {
        counter in plan.rejectAt && slot == Slot.PLAYER_2 -> ProposalResponse.REJECT
        counter in plan.bothCounterAt -> ProposalResponse.COUNTEROFFER
        counter in plan.counterAt && slot == Slot.PLAYER_1 -> ProposalResponse.COUNTEROFFER
        counter in plan.bundleAt && slot == Slot.PLAYER_1 && s.regularTermsRemaining >= 2 ->
            ProposalResponse.BUNDLE
        else -> ProposalResponse.SIGN
    }

    /** Starts, runs down and settles every timer on the current board. */
    private fun runTimers(s: GameState) {
        val board = s.timers
        if (board.timers.isEmpty()) return
        val controller = board.controllerSlot ?: Slot.PLAYER_1
        for (timer in board.timers) {
            val fresh = h.state().timers.timer(timer.id) ?: continue
            if (fresh.state == com.thecontract.core.model.TimerRunState.COMPLETED) continue
            timerStarts += fresh.id
            phone(controller).act(TimerCommand(fresh.id, "start"))
            h.advance(fresh.remainingMs + 250)
        }
    }
}
