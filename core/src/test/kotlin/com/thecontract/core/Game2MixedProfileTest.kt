package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.engine.Eligibility
import com.thecontract.core.engine.EligibilityEngine
import com.thecontract.core.engine.GameContext
import com.thecontract.core.engine.Renderer
import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Plan
import com.thecontract.core.harness.Setups
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.FinaleOrder
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.PartyRef
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.PrivateProfile
import com.thecontract.core.model.Slot
import com.thecontract.core.protocol.ProtocolJson
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Game 2 (section 45) — mixed profile.
 *
 * Yes/Maybe/No answers, limited equipment, Vers Top and Vers Bottom, one erection-difficulty
 * selection, No visible marks, No degradation, No foot play, the private Dominant finale, and a
 * phone that disconnects and reconnects mid-negotiation.
 */
class Game2MixedProfileTest {

    private class Fixture {
        val h = Harness().startGame()
        val p1 = h.newPhone("ravi").join()
        val p2 = h.newPhone("tom").join()
        val driver = GameDriver(h, p1, p2, Plan(counterAt = setOf(2), rejectAt = setOf(4)))

        init {
            driver.submitSetup(Setups.mixed())
            driver.fillProfile(Slot.PLAYER_1, Setups.mixedAnswers(0))
            driver.fillProfile(Slot.PLAYER_2, Setups.mixedAnswers(2))
        }
    }

    @Test
    fun `mixed game completes and respects every boundary and limit`() {
        val f = Fixture()

        // Disconnect Player 2 partway through and reconnect with the stored resume token.
        var disconnected = false
        var steps = 0
        while (f.h.state().phase != GamePhase.COMPLETED && steps++ < 4000) {
            if (!disconnected && f.h.state().negotiation.signed.size == 2) {
                disconnected = true
                val token = f.p2.resumeToken
                f.p2.disconnect()
                assertEquals(
                    "DISCONNECTED",
                    f.h.tvView().connections["PLAYER_2"],
                    "the TV must show the slot as disconnected, not release it"
                )
                val returning = f.h.newPhone("tom", token).join()
                assertEquals(Slot.PLAYER_2, returning.slot, "a reconnecting phone must keep its own slot")
                f.driver.replacePhone(Slot.PLAYER_2, returning)
            }
            f.driver.runOneStep()
        }

        val final = f.h.state()
        assertEquals(GamePhase.COMPLETED, final.phase)
        assertTrue(disconnected, "the reconnect scenario never ran")
        assertEquals(10, final.negotiation.regularSlotsUsed)
        assertEquals(2, final.negotiation.signedClosing.size)

        val setup = final.setup
        val allowedEquipment = setup.equipment.map { it.label }.toSet()

        for (signed in final.negotiation.signed) {
            for (term in signed.allTerms) {
                val definition = ContentLibrary.term(term.termId)

                // No missing equipment ever reaches a player.
                term.equipmentUsed.forEach { label ->
                    assertTrue(label in allowedEquipment, "term ${term.termId} used unavailable '$label'")
                }
                assertTrue(
                    setup.equipment.containsAll(definition.requiredEquipment),
                    "term ${term.termId} requires equipment the couple does not have"
                )

                // No marking content.
                assertTrue(
                    definition.requiredEquipment.none { it in Boundary.markingEquipment },
                    "term ${term.termId} uses marking equipment despite No visible marks"
                )
                assertTrue(
                    Boundary.NO_MARKS !in EligibilityEngine.blockingBoundaries(definition),
                    "term ${term.termId} is blocked by No visible marks but was signed"
                )

                // No degradation, no foot play.
                assertTrue("degradation" !in definition.activities, "degradation leaked into ${term.termId}")
                assertTrue(
                    definition.activities.none { it in setOf("foot_kissing", "foot_stimulation", "massage_calves_feet") },
                    "foot play leaked into ${term.termId}"
                )
            }
        }

        // Erection-dependent content stays away from Tom until his own closing term.
        val tom = Slot.PLAYER_2
        for (signed in final.negotiation.signedRegular) {
            for (term in signed.allTerms) {
                val definition = ContentLibrary.term(term.termId)
                val constrained = when (definition.erectionRequired) {
                    PartyRef.NEITHER -> emptyList()
                    PartyRef.GIVER -> listOf(term.giver)
                    PartyRef.RECEIVER -> listOf(term.receiver)
                    PartyRef.BOTH -> listOf(term.giver, term.receiver)
                }
                assertTrue(
                    tom !in constrained,
                    "regular term ${term.termId} requires Tom to stay hard"
                )
            }
        }

        // Every consideration receipt respected the same limits.
        for (receipt in final.negotiation.receipts) {
            val action = ContentLibrary.consideration(receipt.actionId)
            assertTrue(
                setup.equipment.containsAll(action.requiredEquipment),
                "consideration ${action.id} needs equipment the couple does not have"
            )
            assertTrue(
                action.activities.none { it in setOf("foot_kissing", "foot_stimulation", "massage_calves_feet") },
                "foot play leaked into consideration ${action.id}"
            )
        }
    }

    @Test
    fun `receiving oral stays available to the player with erection difficulty`() {
        val setup = Setups.mixed()
        val ctx = GameContext(
            setup,
            Slot.entries.associateWith { PrivateProfile(it, Setups.allAnswers(com.thecontract.core.model.Answer.YES), complete = true) }
        )
        val oralTermsForTom = ContentLibrary.regularTerms.filter { term ->
            "oral" in term.activities && !term.climax
        }.mapNotNull { term ->
            val e = EligibilityEngine.evaluate(term, ctx)
            if (e is Eligibility.Ok && e.binding.receiver == Slot.PLAYER_2) term else null
        }
        assertTrue(
            oralTermsForTom.isNotEmpty(),
            "the player with erection difficulty must still be able to receive oral"
        )

        // …while erection-dependent topping is not available to him outside his closing term.
        val toppingForTom = ContentLibrary.regularTerms.filter { term ->
            term.erectionRequired == PartyRef.GIVER
        }.mapNotNull { term ->
            val e = EligibilityEngine.evaluate(term, ctx)
            if (e is Eligibility.Ok && e.binding.giver == Slot.PLAYER_2) term else null
        }
        assertTrue(
            toppingForTom.isEmpty(),
            "erection-dependent topping must stay restricted for him: ${toppingForTom.map { it.id }}"
        )

        // It comes back only in his own closing term, which carries a written fallback.
        val closing = ContentLibrary.climaxTerms.filter { it.erectionRequired == PartyRef.RECEIVER }
            .mapNotNull { term ->
                val binding = com.thecontract.core.model.PartyBinding(Slot.PLAYER_1, Slot.PLAYER_2)
                val e = EligibilityEngine.evaluate(term, ctx, preferredBinding = binding)
                if (e is Eligibility.Ok && e.binding == binding) term else null
            }
        assertTrue(closing.isNotEmpty(), "erection-dependent content never returns in his closing term")
        closing.forEach {
            assertTrue(
                (it.base + it.explicit).contains("if the timer", ignoreCase = true),
                "closing term ${it.id} has no fallback if firmness changes"
            )
        }
    }

    @Test
    fun `a Maybe condition rewrites the instruction rather than contradicting it`() {
        val setup = Setups.mixed()
        val answers = Setups.allAnswers(com.thecontract.core.model.Answer.MAYBE)
        val ctx = GameContext(setup, Slot.entries.associateWith { PrivateProfile(it, answers, complete = true) })
        // A term the profile still asks about. Terms built entirely from assumed activities —
        // most of the massage vocabulary now — carry no Maybe to turn into a condition, so
        // picking merely the first term with activities would prove nothing.
        val term = ContentLibrary.regularTerms.first { t ->
            t.requiredEquipment.isEmpty() && t.activities.any { a ->
                PreferenceLibrary.byId[PreferenceLibrary.give(a)]?.asked == true ||
                    PreferenceLibrary.byId[PreferenceLibrary.receive(a)]?.asked == true
            }
        }
        val binding = EligibilityEngine.candidateBindings(term, ctx).first()
        val plain = Renderer.renderTerm(term, binding, ctx)
        val conditions = EligibilityEngine.maybeConditions(term, binding, ctx)
        assertTrue(conditions.isNotEmpty(), "a profile of all Maybes must attach conditions")
        val conditioned = Renderer.renderTerm(term, binding, ctx, conditions)

        assertNotEquals(plain.instruction, conditioned.instruction, "the condition did not rewrite the instruction")
        assertTrue(conditioned.conditions.isNotEmpty())
        assertTrue(
            conditioned.instruction.length > plain.instruction.length ||
                conditioned.timers != plain.timers,
            "the condition changed neither the words nor the timers"
        )
    }

    @Test
    fun `the private Dominant finale selection never reaches the TV`() {
        val f = Fixture()
        f.driver.run()
        val final = f.h.state()
        assertEquals(GamePhase.COMPLETED, final.phase)
        assertEquals(FinaleOrder.DOMINANTS_CUT, final.finale.chosenOrder)

        val duringPrivateSelection = f.h.tvViews.filter {
            it.phase == GamePhase.PRIVATE_DOMINANT_FINALE_SELECTION
        }
        assertTrue(duringPrivateSelection.isNotEmpty(), "the private selection phase never happened")
        duringPrivateSelection.forEach { view ->
            val json = ProtocolJson.encodeToString(view)
            FinaleOrder.entries.forEach { order ->
                assertTrue(
                    !json.contains(order.name) && !json.contains(orderLabel(order)),
                    "the TV revealed the Dominant's private choice: ${order.name}"
                )
            }
            assertTrue(view.choices.isEmpty(), "the TV offered choices during a private selection")
        }
    }

    @Test
    fun `unavailable equipment never appears in any view`() {
        val f = Fixture()
        f.driver.run()
        val absent = Equipment.entries.filter { it !in Setups.mixed().equipment }
        val allViews = f.h.tvViews + listOfNotNull(f.p1.view, f.p2.view)
        for (view in allViews) {
            val json = ProtocolJson.encodeToString(view)
            for (item in absent) {
                // The setup form legitimately lists every option; game content must not.
                if (view.setup != null) continue
                assertTrue(
                    !json.contains("\"${item.label}\""),
                    "view in ${view.phase} named unavailable equipment ${item.label}"
                )
            }
        }
    }

    private fun orderLabel(o: FinaleOrder) = when (o) {
        FinaleOrder.SIGNED_ORDER -> "Signed Order"
        FinaleOrder.SMOOTH_ESCALATION -> "Smooth Escalation"
        FinaleOrder.DOMINANTS_CUT -> "Dominant's Cut"
    }
}
