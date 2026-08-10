package com.thecontract.core

import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Plan
import com.thecontract.core.harness.Setups
import com.thecontract.core.model.Answer
import com.thecontract.core.model.FinaleFormat
import com.thecontract.core.model.FinaleOrder
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.SessionLength
import com.thecontract.core.model.Slot
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Game 1 (section 45) — broad profile.
 *
 * Both players Yes to everything, every piece of equipment, Vers and Vers, Extremely filthy,
 * with direct signatures, counteroffers, bundle trades, rejections, Back navigation, both
 * closing terms, all three finale orders, timer synchronisation and a phone reconnect.
 */
class Game1BroadProfileTest {

    private fun playBroadGame(
        length: SessionLength = SessionLength.QUICK,
        finale: FinaleFormat = FinaleFormat.THREE_ORDERS,
        plan: Plan = Plan()
    ): Pair<Harness, GameDriver> {
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        assertEquals(Slot.PLAYER_1, p1.slot)
        assertEquals(Slot.PLAYER_2, p2.slot)

        val driver = GameDriver(h, p1, p2, plan)
        driver.submitSetup(Setups.broad(length, finale))
        assertEquals(GamePhase.PRIVATE_PROFILES, h.state().phase)

        driver.fillProfile(Slot.PLAYER_1, Setups.allAnswers(Answer.YES))
        driver.fillProfile(Slot.PLAYER_2, Setups.allAnswers(Answer.YES))
        assertEquals(GamePhase.PROPOSAL, h.state().phase)
        return h to driver
    }

    @Test
    fun `a full broad game completes with the exact contract length and both climax terms`() {
        val plan = Plan(
            rejectAt = setOf(2),
            counterAt = setOf(3),
            bothCounterAt = setOf(4),
            bundleAt = setOf(6),
            backAt = setOf(5),
            backAtConsideration = setOf(8),
            repeatConsiderationAt = setOf(7)
        )
        val (h, driver) = playBroadGame(plan = plan)
        val final = driver.run()

        assertEquals(GamePhase.COMPLETED, final.phase)

        // Exact contract length: 10 regular slots plus exactly two closing terms.
        assertEquals(10, final.negotiation.regularSlotsUsed, "regular term slots consumed")
        assertEquals(2, final.negotiation.signedClosing.size, "closing terms")

        // No duplicates anywhere in the signed contract.
        val termIds = final.negotiation.signed.flatMap { s -> s.allTerms.map { it.termId } }
        assertEquals(termIds.size, termIds.distinct().size, "duplicate terms in the contract")

        // Every regular term has a consideration receipt, and receipts are never contract terms.
        val receiptIds = final.negotiation.receipts.map { it.id }
        assertEquals(receiptIds.size, receiptIds.distinct().size, "duplicate consideration receipts")
        assertTrue(
            final.negotiation.signed.all { it.considerationReceiptId != null },
            "every signed term must carry a consideration receipt"
        )
        assertTrue(
            final.negotiation.receipts.none { r -> r.actionId in termIds },
            "a consideration action leaked into the contract"
        )

        // Both players are guaranteed to finish.
        val closingFor = final.negotiation.signedClosing.mapNotNull { it.closingFor }.toSet()
        assertEquals(setOf(Slot.PLAYER_1, Slot.PLAYER_2), closingFor, "both players must climax")

        // A term both men perform has no beneficiary by design. A term only one man performs may
        // still have none — an order obeyed or a rule kept is done by one of them and benefits
        // neither, because being the one in charge of it is not a benefit.
        final.negotiation.signed.forEach { signed ->
            if (signed.term.mutual) {
                assertNull(signed.term.beneficiary, "mutual term ${signed.term.termId} named a beneficiary")
            }
        }
        assertTrue(
            final.negotiation.signed.any { it.term.beneficiary != null },
            "no signed term named a beneficiary at all"
        )

        // The plan actually exercised what it claimed to.
        assertTrue(driver.backsTaken.isNotEmpty(), "Back navigation was never exercised")
        assertTrue(
            driver.backsFromConsideration.isNotEmpty(),
            "Back out of a consideration choice was never exercised"
        )
        assertTrue(driver.repeatsRequested > 0, "consideration repeat was never exercised")
        assertTrue(
            final.negotiation.signed.any { it.bundledTerm != null },
            "no bundle trade was signed"
        )
        assertTrue(final.negotiation.declinedTermIds.isNotEmpty(), "no proposal was ever rejected")
        assertTrue(
            final.negotiation.signed.any { it.term.amendments.isNotEmpty() },
            "no amendment survived into the contract"
        )
        assertTrue(driver.timerStarts.size > 20, "timers were barely used: ${driver.timerStarts.size}")

        // The two climax terms always execute last.
        val order = final.finale.executionOrder
        assertEquals(final.negotiation.signed.size, order.size)
        val lastTwo = order.takeLast(2).map { final.negotiation.signed[it] }
        assertTrue(lastTwo.all { it.closingFor != null }, "climax terms are not last in the running order")

        // TV and phones agree on the authoritative version.
        assertEquals(final.version, h.tvView().version)
        assertEquals(final.version, driver.p1.view?.version)
        assertEquals(final.version, driver.p2.view?.version)
    }

    @Test
    fun `consideration is varied rather than repeating one action`() {
        val (_, driver) = playBroadGame()
        driver.run()
        val distinct = driver.considerationTitles.distinct().size
        assertTrue(
            distinct >= driver.considerationTitles.size - 2,
            "consideration repeated too often: $distinct distinct of ${driver.considerationTitles.size}"
        )
    }

    @Test
    fun `all three finale orders run to completion and keep the climax terms last`() {
        for (order in FinaleOrder.entries) {
            val plan = Plan(finaleVotes = mapOf(Slot.PLAYER_1 to order, Slot.PLAYER_2 to order))
            val (h, driver) = playBroadGame(plan = plan)
            val final = driver.run()
            assertEquals(GamePhase.COMPLETED, final.phase, "order $order did not complete")
            assertEquals(order, final.finale.chosenOrder)
            val lastTwo = final.finale.executionOrder.takeLast(2).map { final.negotiation.signed[it] }
            assertTrue(lastTwo.all { it.closingFor != null }, "order $order moved a climax term earlier")
            assertEquals(final.version, h.tvView().version)
        }
    }

    @Test
    fun `a disagreement on the finale order is decided by the Dominant`() {
        val plan = Plan(
            finaleVotes = mapOf(
                Slot.PLAYER_1 to FinaleOrder.SIGNED_ORDER,
                Slot.PLAYER_2 to FinaleOrder.DOMINANTS_CUT
            )
        )
        val (h, driver) = playBroadGame(plan = plan)
        val final = driver.run()
        assertEquals(GamePhase.COMPLETED, final.phase)
        assertTrue(
            final.finale.chosenOrder in listOf(FinaleOrder.SIGNED_ORDER, FinaleOrder.DOMINANTS_CUT),
            "the Dominant must choose between the two revealed options"
        )
        assertEquals(Slot.PLAYER_1, h.state().setup.dominant)
    }

    @Test
    fun `the uninterrupted finale format uses smooth escalation without asking`() {
        val (_, driver) = playBroadGame(finale = FinaleFormat.UNINTERRUPTED)
        val final = driver.run()
        assertEquals(FinaleOrder.SMOOTH_ESCALATION, final.finale.chosenOrder)
        val regular = final.finale.executionOrder
            .map { final.negotiation.signed[it] }
            .filter { it.closingFor == null }
            .map { it.term.level }
        assertEquals(regular.sorted(), regular, "smooth escalation must run gentlest first")
    }

    @Test
    fun `a longer contract still lands on exactly the requested number of terms`() {
        val (_, driver) = playBroadGame(length = SessionLength.FULL, plan = Plan(bundleAt = setOf(2, 5)))
        val final = driver.run()
        assertEquals(15, final.negotiation.regularSlotsUsed)
        assertEquals(2, final.negotiation.signedClosing.size)
        assertTrue(final.negotiation.signed.count { it.bundledTerm != null } >= 1)
    }
}
