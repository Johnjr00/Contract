package com.thecontract.core

import com.thecontract.core.engine.EngineState
import com.thecontract.core.engine.FinaleOrdering
import com.thecontract.core.engine.GameEngine
import com.thecontract.core.model.FinaleOrder
import com.thecontract.core.model.FinaleState
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.GameState
import com.thecontract.core.model.Negotiation
import com.thecontract.core.model.RenderedTerm
import com.thecontract.core.model.RenderedTimer
import com.thecontract.core.model.SignedTerm
import com.thecontract.core.model.Slot
import com.thecontract.core.protocol.ExecutionCommand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A trade is signed as one contract item carrying two terms, and performed as two steps.
 *
 * The halves are separate acts. The binding that keeps the benefit alternating casts the owed man
 * as the giver of a giver-benefit term and as the receiver of a receiver-benefit term, so the two
 * halves of a trade routinely have the giver and receiver the other way round from each other.
 * Performed as a single step they inherited the first term's instruction, the first term's
 * controller and the first term's completer — which meant the second term was never read out, ran
 * against the first one's clock, and was marked complete by the man who had just performed it
 * rather than the man it was performed on.
 */
class BundledTermExecutionTest {

    @Test
    fun `a trade becomes two steps and an ordinary term stays one`() {
        val steps = FinaleOrdering.build(state(), FinaleOrder.SIGNED_ORDER)

        assertEquals(3, steps.size, "two contract items, one of them a trade, is three steps")
        assertEquals(listOf(false, true, false), steps.map { it.bundled })
        assertEquals(listOf(0, 0, 1), steps.map { it.signedIndex }, "a trade's halves stay adjacent")
    }

    @Test
    fun `each step performs its own term`() {
        val s = started()
        val signed = s.negotiation.signed
        val performed = FinaleOrdering.resolve(s).map { signed[it.signedIndex].half(it.bundled)?.title }

        assertEquals(listOf("First half", "Second half", "Ordinary"), performed)
    }

    @Test
    fun `the second half is run and signed off by its own men, not the first half's`() {
        val s = started()
        val engine = GameEngine()

        // Step 0: the first half. Given by player one, so he holds the clock and player two,
        // who it is done to, is the one who says it is finished.
        val first = engine.apply(
            EngineState(s), null, "a1", -1, ExecutionCommand("begin"), NOW
        ).state
        assertEquals(Slot.PLAYER_1, first.timers.controllerSlot)
        assertEquals(Slot.PLAYER_2, first.timers.completerSlot)
        assertEquals(listOf(60), first.timers.timers.map { it.totalSeconds }, "the first half's clock only")

        // Step 1: the second half, which is the other man's to give.
        val onSecond = s.copy(finale = s.finale.copy(stepIndex = 1, stepStarted = false))
        val second = engine.apply(
            EngineState(onSecond), null, "a2", -1, ExecutionCommand("begin"), NOW
        ).state
        assertEquals(Slot.PLAYER_2, second.timers.controllerSlot, "the second half is player two's to run")
        assertEquals(Slot.PLAYER_1, second.timers.completerSlot, "and player one's to mark complete")
        assertEquals(listOf(90), second.timers.timers.map { it.totalSeconds }, "the second half's clock only")
    }

    /** The clocks used to be pooled, so the first step of a trade started both terms' timers. */
    @Test
    fun `a step never starts the other half's clock`() {
        val s = started()
        for (index in 0..2) {
            val at = s.copy(finale = s.finale.copy(stepIndex = index, stepStarted = false))
            val after = GameEngine().apply(
                EngineState(at), null, "a$index", -1, ExecutionCommand("begin"), NOW
            ).state
            assertEquals(1, after.timers.timers.size, "step $index started more than its own clock")
        }
    }

    /** A session saved before the finale was stepped by half still has somewhere to go. */
    @Test
    fun `an order missing from a saved session is rebuilt`() {
        val s = started().let { it.copy(finale = it.finale.copy(executionSteps = emptyList())) }
        val rebuilt = FinaleOrdering.resolve(s)

        assertEquals(3, rebuilt.size)
        assertTrue(rebuilt.any { it.bundled }, "the trade's second half was lost on rebuild")
    }

    @Test
    fun `the last step is the end of the contract, not the last contract item`() {
        val s = started()
        // Standing on the trade's second half, there is still an ordinary term to come.
        val midway = s.copy(finale = s.finale.copy(stepIndex = 1, stepStarted = true))
        val afterNext = GameEngine().apply(
            EngineState(midway), null, "n1", -1, ExecutionCommand("next"), NOW
        ).state
        assertEquals(GamePhase.FINAL_EXECUTION, afterNext.phase, "the contract ended an item early")
        assertEquals(2, afterNext.finale.stepIndex)

        val onLast = s.copy(finale = s.finale.copy(stepIndex = 2, stepStarted = true))
        val ended = GameEngine().apply(
            EngineState(onLast), null, "n2", -1, ExecutionCommand("next"), NOW
        ).state
        assertEquals(GamePhase.COMPLETED, ended.phase)
    }

    // ------------------------------------------------------------------ fixtures

    private fun started(): GameState {
        val s = state()
        return s.copy(
            finale = s.finale.copy(executionSteps = FinaleOrdering.build(s, FinaleOrder.SIGNED_ORDER))
        )
    }

    private fun state(): GameState = GameState(
        sessionId = "bundle-test",
        phase = GamePhase.FINAL_EXECUTION,
        negotiation = Negotiation(
            signed = listOf(
                SignedTerm(
                    index = 0,
                    act = 1,
                    // Player one gives; player two receives and signs it off.
                    term = term("t1", "First half", Slot.PLAYER_1, Slot.PLAYER_2, 60),
                    // The other way round, which is what makes it a trade.
                    bundledTerm = term("t2", "Second half", Slot.PLAYER_2, Slot.PLAYER_1, 90),
                    considerationReceiptId = null,
                    signedBySlots = listOf(Slot.PLAYER_1, Slot.PLAYER_2),
                    signedAtMs = NOW
                ),
                SignedTerm(
                    index = 1,
                    act = 1,
                    term = term("t3", "Ordinary", Slot.PLAYER_1, Slot.PLAYER_2, 30),
                    considerationReceiptId = null,
                    signedBySlots = listOf(Slot.PLAYER_1, Slot.PLAYER_2),
                    signedAtMs = NOW
                )
            )
        ),
        finale = FinaleState(chosenOrder = FinaleOrder.SIGNED_ORDER)
    )

    private fun term(id: String, title: String, giver: Slot, receiver: Slot, seconds: Int) = RenderedTerm(
        termId = id,
        level = 1,
        title = title,
        instruction = "$title instruction.",
        timers = listOf(RenderedTimer("$id-timer", title, seconds)),
        giver = giver,
        receiver = receiver,
        beneficiary = receiver,
        benefitExplanation = "",
        equipmentUsed = emptyList()
    )

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
