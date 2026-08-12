package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.engine.ConsiderationSelector
import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Plan
import com.thecontract.core.harness.Setups
import com.thecontract.core.model.Answer
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.SessionLength
import com.thecontract.core.model.Slot
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Consideration is a payment. Everything here follows from that: the player who gained less from
 * the term is the one who is owed, so he is the one who says what the payment is, every option
 * is something done to him, and none of them is something the payer enjoys in his own right.
 */
class ConsiderationFairnessTest {

    private fun playAndInspect(plan: Plan = Plan(), check: (Harness, GameDriver) -> Unit) {
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        val driver = GameDriver(h, p1, p2, plan)
        driver.submitSetup(Setups.broad(SessionLength.QUICK))
        driver.fillProfile(Slot.PLAYER_1, Setups.allAnswers(Answer.YES))
        driver.fillProfile(Slot.PLAYER_2, Setups.allAnswers(Answer.YES))

        var guard = 0
        while (h.state().phase != GamePhase.COMPLETED && guard++ < 4000) {
            check(h, driver)
            driver.runOneStep()
        }
        assertEquals(GamePhase.COMPLETED, h.state().phase, "the game did not finish")
    }

    @Test
    fun `the player who gains less chooses, and every option is worked on him`() {
        var selectionScreens = 0
        playAndInspect { h, driver ->
            val s = h.state()
            if (s.phase != GamePhase.CONSIDERATION_PRIVATE_SELECTION &&
                s.phase != GamePhase.CLOSING_TERM_CONSIDERATION
            ) {
                return@playAndInspect
            }
            val current = s.negotiation.current ?: return@playAndInspect
            val beneficiary = current.beneficiary ?: return@playAndInspect
            selectionScreens++

            val owed = beneficiary.other
            val chooser = if (owed == Slot.PLAYER_1) driver.p1 else driver.p2
            val payer = if (owed == Slot.PLAYER_1) driver.p2 else driver.p1

            assertTrue(
                chooser.choiceIdsStartingWith("pick_consideration:").isNotEmpty(),
                "the player who is owed was not offered the list"
            )
            assertTrue(
                payer.choiceIdsStartingWith("pick_consideration:").isEmpty(),
                "the player who owes consideration was allowed to choose it himself"
            )

            current.considerationOptions.forEach { option ->
                assertEquals(
                    beneficiary, option.performer,
                    "option ${option.actionId} is not performed by the player who owes it"
                )
                assertEquals(
                    owed, option.recipient,
                    "option ${option.actionId} is not worked on the player who is owed"
                )
                val action = ContentLibrary.considerations.first { it.id == option.actionId }
                assertTrue(
                    !action.usesPerformersCock,
                    "option ${option.actionId} gets the payer off, so it is not a payment"
                )
            }
        }
        assertTrue(selectionScreens >= 5, "too few unbalanced consideration screens to judge: $selectionScreens")
    }

    /**
     * The man who is owed picks the payment, so the list has to be long enough to be a choice.
     * Ten for a single term and twelve for a closing one, and a balanced term is held to the same
     * count as a one-sided one — reciprocal offers used to come from a pool of six actions across
     * three bands, so a balanced term late in a game could reach his phone with one option on it.
     */
    @Test
    fun `every consideration list is a full list`() {
        var lists = 0
        var balancedLists = 0
        // Several negotiations rather than one. Whether a balanced term comes up at all is a draw
        // from the weighted pool, and a single game is one sample of it — the reciprocal-offer
        // path went unchecked whenever that draw happened not to land, which is a property of the
        // pool on the day rather than of the code under test.
        val plans = listOf(
            Plan(),
            Plan(rejectAt = setOf(1, 3)),
            Plan(counterAt = setOf(2)),
            Plan(bundleAt = setOf(2)),
            Plan(rejectAt = setOf(2), counterAt = setOf(4))
        )
        for (plan in plans) {
            playAndInspect(plan) { h, _ ->
                val s = h.state()
                if (s.phase != GamePhase.CONSIDERATION_PRIVATE_SELECTION &&
                    s.phase != GamePhase.CLOSING_TERM_CONSIDERATION
                ) {
                    return@playAndInspect
                }
                val current = s.negotiation.current ?: return@playAndInspect
                if (current.considerationOptions.isEmpty()) return@playAndInspect
                val expected = if (current.term.climax) 12 else 10
                assertEquals(
                    expected, current.considerationOptions.size,
                    "${current.term.termId} offered ${current.considerationOptions.size} options"
                )
                assertEquals(
                    expected, current.considerationOptions.map { it.actionId }.distinct().size,
                    "${current.term.termId} offered the same action twice"
                )
                lists++
                if (current.beneficiary == null) balancedLists++
            }
        }
        assertTrue(lists >= 5, "too few consideration lists to judge: $lists")
        assertTrue(balancedLists >= 1, "no balanced term came up, so reciprocal offers went unchecked")
    }

    @Test
    fun `consideration never runs ahead of the act it is offered in`() {
        var checked = 0
        playAndInspect { h, _ ->
            val s = h.state()
            val current = s.negotiation.current ?: return@playAndInspect
            if (current.considerationOptions.isEmpty()) return@playAndInspect
            val ceiling = ConsiderationSelector.band(s)
            current.considerationOptions.forEach { option ->
                val action = ContentLibrary.considerations.first { it.id == option.actionId }
                assertTrue(
                    action.intensity <= ceiling,
                    "${option.actionId} is intensity ${action.intensity} in a band-$ceiling act"
                )
                checked++
            }
        }
        assertTrue(checked > 0, "no consideration options were seen")
    }

    @Test
    fun `the opening act keeps away from cocks and holes`() {
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        val driver = GameDriver(h, p1, p2)
        driver.submitSetup(Setups.broad(SessionLength.QUICK))
        driver.fillProfile(Slot.PLAYER_1, Setups.allAnswers(Answer.YES))
        driver.fillProfile(Slot.PLAYER_2, Setups.allAnswers(Answer.YES))

        // Act one, before anything is signed: the first proposal and the first list of options.
        val first = h.state().negotiation.current!!.term
        val firstTerm = ContentLibrary.termsById.getValue(first.termId)
        assertEquals(1, firstTerm.level, "the opening proposal is above act one")

        var guard = 0
        while (h.state().negotiation.current?.considerationOptions.isNullOrEmpty() && guard++ < 40) {
            driver.runOneStep()
        }
        val options = h.state().negotiation.current!!.considerationOptions
        assertTrue(options.isNotEmpty(), "no opening consideration options appeared")
        options.forEach { option ->
            val action = ContentLibrary.considerations.first { it.id == option.actionId }
            assertEquals(
                1, action.intensity,
                "the opening act offered ${option.actionId} at intensity ${action.intensity}"
            )
        }
    }
}
