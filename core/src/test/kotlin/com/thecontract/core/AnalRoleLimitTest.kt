package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.engine.Eligibility
import com.thecontract.core.engine.EligibilityEngine
import com.thecontract.core.engine.GameContext
import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Plan
import com.thecontract.core.harness.Setups
import com.thecontract.core.model.AnalRole
import com.thecontract.core.model.Answer
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.Explicitness
import com.thecontract.core.model.FinaleFormat
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.MaybeCondition
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.PlayerSetup
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.PrivateProfile
import com.thecontract.core.model.Role
import com.thecontract.core.model.SessionLength
import com.thecontract.core.model.SharedSetup
import com.thecontract.core.model.Slot
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A top's hole is off the table for the whole night, and a vers top's for all but one moment of
 * it.
 *
 * "Top" never meant only "does not get penetrated". Rimming, fingering, a plug, a hand — all of
 * it is his hole, and the eligibility engine used to wave every one of them through because it
 * checked `canBottom` on penetration alone. These tests state the rule over complete games as
 * well as over the library, because the vers top's allowance is one *per game* and nothing about
 * a single term can tell you whether it has already been spent.
 */
class AnalRoleLimitTest {

    private fun setup(p1: AnalRole, p2: AnalRole, length: SessionLength = SessionLength.FULL) = SharedSetup(
        player1 = PlayerSetup("Marcus", Role.DOMINANT, p1),
        player2 = PlayerSetup("Dan", Role.SUBMISSIVE, p2),
        sessionLength = length,
        explicitness = Explicitness.EXTREME,
        finaleFormat = FinaleFormat.THREE_ORDERS,
        stopWord = "red",
        defaultMaybeCondition = MaybeCondition.GENTLER,
        boundaries = emptySet(),
        equipment = Equipment.entries.toSet()
    )

    /** Everything that reached a phone, and who was on the receiving end of it. */
    private fun receptiveCounts(h: Harness): Map<Slot, Int> {
        val s = h.state()
        val counts = mutableMapOf<Slot, Int>()
        fun bump(slots: Set<Slot>) = slots.forEach { counts[it] = (counts[it] ?: 0) + 1 }
        s.negotiation.signed.forEach { signed ->
            signed.allTerms.forEach { rendered ->
                ContentLibrary.termsById[rendered.termId]?.let {
                    bump(EligibilityEngine.receptiveParties(it, PartyBinding(rendered.giver, rendered.receiver)))
                }
            }
        }
        s.negotiation.receipts.forEach { receipt ->
            ContentLibrary.considerationsById[receipt.actionId]?.let {
                bump(EligibilityEngine.receptiveParties(it, receipt.performer, receipt.recipient))
            }
        }
        return counts
    }

    private fun playFullGame(setup: SharedSetup, plan: Plan = Plan()): Harness {
        val h = Harness().startGame()
        val p1 = h.newPhone("p1").join()
        val p2 = h.newPhone("p2").join()
        val driver = GameDriver(h, p1, p2, plan)
        check(driver.submitSetup(setup)) { "setup refused" }
        driver.run()
        assertEquals(GamePhase.COMPLETED, h.state().phase, "game did not finish")
        return h
    }

    @Test
    fun `a top is never on the receiving end of anything anal, across whole games`() {
        val pairings = listOf(
            AnalRole.TOP to AnalRole.BOTTOM,
            AnalRole.TOP to AnalRole.VERS,
            AnalRole.TOP to AnalRole.VERS_BOTTOM,
            AnalRole.BOTTOM to AnalRole.TOP,
            AnalRole.TOP to AnalRole.TOP
        )
        val plans = listOf(Plan(), Plan(bundleAt = setOf(1, 3), counterAt = setOf(2)), Plan(rejectAt = setOf(1, 2)))
        pairings.forEach { (r1, r2) ->
            plans.forEach { plan ->
                val setup = setup(r1, r2)
                val h = playFullGame(setup, plan)
                val counts = receptiveCounts(h)
                Slot.entries.forEach { slot ->
                    if (setup.player(slot).analRole == AnalRole.TOP) {
                        assertEquals(
                            0, counts[slot] ?: 0,
                            "${setup.player(slot).name} is a top in $r1/$r2 but was the receptive party " +
                                "${counts[slot]} times"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `a vers top is on the receiving end at most once, across whole games`() {
        val pairings = listOf(
            AnalRole.VERS_TOP to AnalRole.BOTTOM,
            AnalRole.VERS_TOP to AnalRole.VERS,
            AnalRole.VERS_TOP to AnalRole.VERS_BOTTOM,
            AnalRole.VERS_TOP to AnalRole.VERS_TOP,
            AnalRole.VERS to AnalRole.VERS_TOP
        )
        val plans = listOf(
            Plan(),
            Plan(bundleAt = setOf(1, 2, 4), counterAt = setOf(3)),
            Plan(rejectAt = setOf(2), repeatConsiderationAt = setOf(1, 3))
        )
        pairings.forEach { (r1, r2) ->
            plans.forEach { plan ->
                val setup = setup(r1, r2)
                val h = playFullGame(setup, plan)
                val counts = receptiveCounts(h)
                Slot.entries.forEach { slot ->
                    if (setup.player(slot).analRole == AnalRole.VERS_TOP) {
                        assertTrue(
                            (counts[slot] ?: 0) <= 1,
                            "${setup.player(slot).name} is a vers top in $r1/$r2 but was the receptive " +
                                "party ${counts[slot]} times"
                        )
                    }
                }
            }
        }
    }

    /**
     * The library check, so a newly written term cannot slip past by not being reachable in the
     * games above: nothing in the whole library may be offered with a top on the receiving end.
     */
    @Test
    fun `no term or action in the library is eligible with a top receiving`() {
        val s = setup(AnalRole.TOP, AnalRole.VERS)
        val ctx = GameContext(
            s,
            Slot.entries.associateWith { slot ->
                PrivateProfile(slot, PreferenceLibrary.all.associate { it.id to Answer.YES }, complete = true)
            }
        )
        val failures = mutableListOf<String>()
        ContentLibrary.allTerms.forEach { term ->
            val e = EligibilityEngine.evaluate(term, ctx)
            if (e is Eligibility.Ok && Slot.PLAYER_1 in EligibilityEngine.receptiveParties(term, e.binding)) {
                failures += "term ${term.id} puts the top on the receiving end"
            }
        }
        ContentLibrary.considerations.forEach { action ->
            listOf(Slot.PLAYER_1 to Slot.PLAYER_2, Slot.PLAYER_2 to Slot.PLAYER_1).forEach { (performer, recipient) ->
                val e = EligibilityEngine.evaluateConsideration(action, performer, recipient, ctx)
                if (e is Eligibility.Ok &&
                    Slot.PLAYER_1 in EligibilityEngine.receptiveParties(action, performer, recipient)
                ) {
                    failures += "consideration ${action.id} puts the top on the receiving end"
                }
            }
        }
        failures.forEach(::println)
        assertEquals(0, failures.size, "content above reaches a top's hole")
    }

    /** Once a vers top has spent his one, nothing anal is offered to him again. */
    @Test
    fun `a vers top with his allowance spent is offered nothing further`() {
        val s = setup(AnalRole.VERS_TOP, AnalRole.VERS)
        val profiles = Slot.entries.associateWith { slot ->
            PrivateProfile(slot, PreferenceLibrary.all.associate { it.id to Answer.YES }, complete = true)
        }
        val spent = GameContext(s, profiles, mapOf(Slot.PLAYER_1 to 1))
        val fresh = GameContext(s, profiles, emptyMap())

        val offeredWhenFresh = ContentLibrary.allTerms.count { term ->
            val e = EligibilityEngine.evaluate(term, fresh)
            e is Eligibility.Ok && Slot.PLAYER_1 in EligibilityEngine.receptiveParties(term, e.binding)
        }
        assertTrue(offeredWhenFresh > 0, "a vers top with his allowance intact should be able to take a turn")

        val offeredWhenSpent = ContentLibrary.allTerms.count { term ->
            val e = EligibilityEngine.evaluate(term, spent)
            e is Eligibility.Ok && Slot.PLAYER_1 in EligibilityEngine.receptiveParties(term, e.binding)
        }
        assertEquals(0, offeredWhenSpent, "a vers top who has had his one is still being offered more")
    }
}
