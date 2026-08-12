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

/**
 * A trade is one contract item holding two terms, performed as two steps.
 *
 * The binding that keeps the benefit alternating casts the owed man as the giver of a
 * giver-benefit term and the receiver of a receiver-benefit term, so a trade's halves routinely
 * have giver and receiver the other way round. Run as a single step, the second half inherited the
 * first one's instruction, controller and completer — never shown, timed against the wrong clock,
 * and signed off by the man who had just performed it.
 */
class BundledTermExecutionTest {

    @Test
    fun `a trade is two steps, and each half is run by its own men on its own clock`() {
        val s = started()
        val steps = FinaleOrdering.resolve(s)
        assertEquals(3, steps.size, "two items, one a trade, is three steps")
        assertEquals(listOf(0, 0, 1), steps.map { it.signedIndex }, "the halves must stay adjacent")

        // Player one gives the first half, so he holds the clock and player two signs it off.
        val first = begin(s, 0)
        assertEquals(Slot.PLAYER_1, first.timers.controllerSlot)
        assertEquals(Slot.PLAYER_2, first.timers.completerSlot)
        assertEquals(listOf(60), first.timers.timers.map { it.totalSeconds }, "its own clock only")

        // The second half is the other man's to give, and the other man's to mark complete.
        val second = begin(s, 1)
        assertEquals(Slot.PLAYER_2, second.timers.controllerSlot)
        assertEquals(Slot.PLAYER_1, second.timers.completerSlot)
        assertEquals(listOf(90), second.timers.timers.map { it.totalSeconds }, "its own clock only")
    }

    @Test
    fun `the contract ends on the last term, not the last contract item`() {
        val s = started()
        val onTradesSecondHalf = GameEngine().apply(
            EngineState(s.copy(finale = s.finale.copy(stepIndex = 1, stepStarted = true))),
            null, "n1", -1, ExecutionCommand("next"), NOW
        ).state
        assertEquals(GamePhase.FINAL_EXECUTION, onTradesSecondHalf.phase, "ended an item early")

        val onLast = GameEngine().apply(
            EngineState(s.copy(finale = s.finale.copy(stepIndex = 2, stepStarted = true))),
            null, "n2", -1, ExecutionCommand("next"), NOW
        ).state
        assertEquals(GamePhase.COMPLETED, onLast.phase)
    }

    // ------------------------------------------------------------------ fixtures

    private fun begin(s: GameState, step: Int): GameState = GameEngine().apply(
        EngineState(s.copy(finale = s.finale.copy(stepIndex = step, stepStarted = false))),
        null, "a$step", -1, ExecutionCommand("begin"), NOW
    ).state

    private fun started(): GameState = state().let {
        it.copy(finale = it.finale.copy(executionSteps = FinaleOrdering.build(it, FinaleOrder.SIGNED_ORDER)))
    }

    private fun state(): GameState = GameState(
        sessionId = "bundle-test",
        phase = GamePhase.FINAL_EXECUTION,
        negotiation = Negotiation(
            signed = listOf(
                SignedTerm(
                    index = 0, act = 1,
                    term = term("t1", "First half", Slot.PLAYER_1, Slot.PLAYER_2, 60),
                    // The other way round, which is what makes it a trade.
                    bundledTerm = term("t2", "Second half", Slot.PLAYER_2, Slot.PLAYER_1, 90),
                    considerationReceiptId = null,
                    signedBySlots = listOf(Slot.PLAYER_1, Slot.PLAYER_2), signedAtMs = NOW
                ),
                SignedTerm(
                    index = 1, act = 1,
                    term = term("t3", "Ordinary", Slot.PLAYER_1, Slot.PLAYER_2, 30),
                    considerationReceiptId = null,
                    signedBySlots = listOf(Slot.PLAYER_1, Slot.PLAYER_2), signedAtMs = NOW
                )
            )
        ),
        finale = FinaleState(chosenOrder = FinaleOrder.SIGNED_ORDER)
    )

    private fun term(id: String, title: String, giver: Slot, receiver: Slot, seconds: Int) = RenderedTerm(
        termId = id, level = 1, title = title, instruction = "$title instruction.",
        timers = listOf(RenderedTimer("$id-timer", title, seconds)),
        giver = giver, receiver = receiver, beneficiary = receiver,
        benefitExplanation = "", equipmentUsed = emptyList()
    )

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
