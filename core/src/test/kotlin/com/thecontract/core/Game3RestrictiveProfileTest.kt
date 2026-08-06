package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.engine.EligibilityEngine
import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Plan
import com.thecontract.core.harness.Setups
import com.thecontract.core.harness.SimulatedPhone
import com.thecontract.core.harness.TestClock
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category
import com.thecontract.core.model.FinaleOrder
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.PartyRef
import com.thecontract.core.model.Slot
import com.thecontract.core.persistence.JsonFileStateStore
import com.thecontract.core.protocol.ProtocolJson
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Game 3 (section 45) — restrictive profile.
 *
 * Both players have the erection-difficulty option, no anal at all, no toys, no pain, no rough
 * sex, no degradation, no foot play, minimal equipment, one uninterrupted finale — with the app
 * process restarting during negotiation and the TV restarting again before the finale.
 */
class Game3RestrictiveProfileTest {

    private class Session(
        val h: Harness,
        val p1: SimulatedPhone,
        val p2: SimulatedPhone,
        val driver: GameDriver
    )

    /** Simulates process death: everything in memory is discarded, only the store survives. */
    private fun restart(
        store: JsonFileStateStore,
        clock: TestClock,
        p1Token: String?,
        p2Token: String?
    ): Session {
        val h = Harness(store, clock)
        assertTrue(h.manager.resumeSavedSession(), "the unfinished session did not restore")
        val p1 = h.newPhone("sam", p1Token).join()
        val p2 = h.newPhone("ben", p2Token).join()
        assertEquals(Slot.PLAYER_1, p1.slot, "Player 1 did not reclaim his slot after restart")
        assertEquals(Slot.PLAYER_2, p2.slot, "Player 2 did not reclaim his slot after restart")
        return Session(h, p1, p2, GameDriver(h, p1, p2, Plan(finaleVotes = emptyMap())))
    }

    @Test
    fun `restrictive game survives two restarts and still completes with both climax terms`(@TempDir dir: File) {
        val store = JsonFileStateStore(dir)
        val clock = TestClock()

        var h = Harness(store, clock).startGame()
        var p1 = h.newPhone("sam").join()
        var p2 = h.newPhone("ben").join()
        var driver = GameDriver(h, p1, p2, Plan(counterAt = setOf(3)))
        driver.submitSetup(Setups.restrictive())
        driver.fillProfile(Slot.PLAYER_1, Setups.restrictiveAnswers())
        driver.fillProfile(Slot.PLAYER_2, Setups.restrictiveAnswers())
        assertEquals(GamePhase.PROPOSAL, h.state().phase)

        val p1Token = p1.resumeToken
        val p2Token = p2.resumeToken
        assertNotNull(p1Token)
        assertNotNull(p2Token)

        // --- restart 1: mid-negotiation -------------------------------------------------
        var steps = 0
        while (h.state().negotiation.signed.size < 3 && steps++ < 2000) driver.runOneStep()
        val signedBeforeRestart = h.state().negotiation.signed.size
        val versionBefore = h.state().version
        h.manager.persistNow()

        var session = restart(store, clock, p1Token, p2Token)
        h = session.h; p1 = session.p1; p2 = session.p2; driver = session.driver
        assertEquals(signedBeforeRestart, h.state().negotiation.signed.size, "signed terms lost across restart")
        assertTrue(h.state().version >= versionBefore, "state version went backwards")
        // Profiles survived and are still private.
        assertTrue(h.state().profile(Slot.PLAYER_1).complete)
        assertTrue(h.state().profile(Slot.PLAYER_2).complete)

        // --- run to the final contract review, then restart again -----------------------
        steps = 0
        while (h.state().phase != GamePhase.FINAL_CONTRACT_REVIEW && h.state().phase != GamePhase.COMPLETED &&
            steps++ < 4000
        ) {
            driver.runOneStep()
        }
        assertEquals(GamePhase.FINAL_CONTRACT_REVIEW, h.state().phase)
        h.manager.persistNow()

        session = restart(store, clock, p1Token, p2Token)
        h = session.h; p1 = session.p1; p2 = session.p2; driver = session.driver
        assertEquals(GamePhase.FINAL_CONTRACT_REVIEW, h.state().phase, "the TV reboot lost the finale state")

        // --- finish ---------------------------------------------------------------------
        val final = driver.run()
        assertEquals(GamePhase.COMPLETED, final.phase)
        assertEquals(10, final.negotiation.regularSlotsUsed)
        assertEquals(2, final.negotiation.signedClosing.size, "both climax terms must be generated")
        assertEquals(
            setOf(Slot.PLAYER_1, Slot.PLAYER_2),
            final.negotiation.signedClosing.mapNotNull { it.closingFor }.toSet()
        )
        // One uninterrupted contract always uses Smooth Escalation.
        assertEquals(FinaleOrder.SMOOTH_ESCALATION, final.finale.chosenOrder)

        // No incompatible content leaked anywhere.
        val forbiddenActivities = setOf(
            "topping", "bottoming", "fingering", "anal_toys", "anal_external", "massage_anus",
            "plug", "dildo", "vibration", "cock_ring", "nipple_clamps", "rimming",
            "spanking", "impact_toys", "degradation", "hard_sex", "rough_handling", "pinning",
            "harder_oral", "rough_hair_pulling", "foot_kissing", "foot_stimulation", "massage_calves_feet"
        )
        val allSignedTerms = final.negotiation.signed.flatMap { it.allTerms }
        for (term in allSignedTerms) {
            val d = ContentLibrary.term(term.termId)
            assertTrue(
                d.activities.none { it in forbiddenActivities },
                "term ${term.termId} contains content this couple ruled out: ${d.activities}"
            )
            assertTrue(!d.analPenetration, "anal penetration leaked into ${term.termId}")
            assertTrue(Category.ANAL !in d.categories && Category.RIMMING !in d.categories)
            assertTrue(
                d.requiredEquipment.none { it.toyLike } && !d.genericToy,
                "a toy leaked into ${term.termId}"
            )
            assertTrue(
                final.setup.equipment.containsAll(d.requiredEquipment),
                "term ${term.termId} needs equipment they do not have"
            )
            assertTrue(
                EligibilityEngine.blockingBoundaries(d).none { it in final.setup.boundaries },
                "term ${term.termId} is blocked by one of their boundaries"
            )
        }

        // Erection-dependent content is confined to the two closing terms.
        for (signed in final.negotiation.signedRegular) {
            for (term in signed.allTerms) {
                assertEquals(
                    PartyRef.NEITHER,
                    ContentLibrary.term(term.termId).erectionRequired,
                    "regular term ${term.termId} depends on an erection"
                )
            }
        }

        // Consideration stayed varied even with a very narrow pool.
        val used = final.negotiation.receipts.map { it.actionId }
        assertTrue(used.distinct().size >= (used.size * 2) / 3, "consideration repeated too much: $used")
    }

    @Test
    fun `no profile answer ever appears in a TV view`(@TempDir dir: File) {
        val store = JsonFileStateStore(dir)
        val h = Harness(store).startGame()
        val p1 = h.newPhone("sam").join()
        val p2 = h.newPhone("ben").join()
        val driver = GameDriver(h, p1, p2)
        driver.submitSetup(Setups.restrictive())
        driver.fillProfile(Slot.PLAYER_1, Setups.restrictiveAnswers())
        driver.fillProfile(Slot.PLAYER_2, Setups.restrictiveAnswers())
        driver.run()

        // Preference ids and labels are the private material. None may reach the TV, ever.
        val prefIds = com.thecontract.core.model.PreferenceLibrary.all.map { it.id }
        val prefLabels = com.thecontract.core.model.PreferenceLibrary.all.map { it.label }
        for (view in h.tvViews) {
            val json = ProtocolJson.encodeToString(view)
            prefIds.forEach { id -> assertTrue(!json.contains("\"$id\""), "TV view leaked preference id $id") }
            prefLabels.forEach { label ->
                assertTrue(!json.contains(label), "TV view leaked preference label '$label'")
            }
            assertTrue(view.profile == null, "TV view carried a profile form in ${view.phase}")
        }
    }

    @Test
    fun `a restrictive couple still has usable closing terms for both players`() {
        val setup = Setups.restrictive()
        val ctx = com.thecontract.core.engine.GameContext(
            setup,
            Slot.entries.associateWith {
                com.thecontract.core.model.PrivateProfile(it, Setups.restrictiveAnswers(), complete = true)
            }
        )
        for (finisher in Slot.entries) {
            val binding = com.thecontract.core.model.PartyBinding(finisher.other, finisher)
            val options = ContentLibrary.climaxTerms.filter { term ->
                val e = EligibilityEngine.evaluate(term, ctx, preferredBinding = binding)
                e is com.thecontract.core.engine.Eligibility.Ok && e.binding == binding
            }
            assertTrue(options.size >= 2, "only ${options.size} closing options for $finisher")
            assertTrue(
                options.none { Boundary.NO_ANAL_PENETRATION in EligibilityEngine.blockingBoundaries(it) },
                "a penetrative closing term survived a No-anal profile"
            )
        }
    }
}
