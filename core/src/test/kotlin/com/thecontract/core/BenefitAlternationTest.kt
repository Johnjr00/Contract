package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.engine.BenefitAnalysis
import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Plan
import com.thecontract.core.harness.Setups
import com.thecontract.core.model.Answer
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.GameState
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.SessionLength
import com.thecontract.core.model.SignedTerm
import com.thecontract.core.model.Slot
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Does the contract actually take turns?
 *
 * The rule the engine implements is that the man who came out ahead on the last one-sided term is
 * not the man the next one is bound to favour. Two things about that are worth stating plainly,
 * because neither is obvious from the outside:
 *
 * The turn moves on a **signature**, not on a proposal. A term that is rejected leaves the debt
 * exactly where it was, so the replacement is bound to favour the same man again — which is right,
 * since nobody received anything, but it does mean a run of rejections shows a run of proposals in
 * one man's favour.
 *
 * A **balanced** term names nobody. Mutual terms and protocol terms score level, so they are
 * skipped rather than treated as a turn: they leave the debt where it was rather than passing it.
 *
 * So the property that should hold is not "every term alternates" but "no man is ahead twice
 * running, counting only the terms that had a winner". These games check that against real
 * playthroughs rather than against the selector in isolation, since alternation is a preference
 * the selector will drop if nothing eligible survives with it in place.
 */
class BenefitAlternationTest {

    @Test
    fun `no man comes out ahead on two one-sided terms running`() {
        var checked = 0
        for ((name, run) in games()) {
            val sequence = beneficiaries(run)
            checked += sequence.size

            sequence.zipWithNext().forEachIndexed { i, (a, b) ->
                assertTrue(
                    a != b,
                    "$name: terms ${i + 1} and ${i + 2} both came out in favour of $a — $sequence"
                )
            }
        }
        assertTrue(checked >= 20, "not enough one-sided terms were signed to prove anything: $checked")
    }

    /**
     * The share each man takes across a whole contract. Alternation is a local rule, so this is
     * the check that it adds up to something fair rather than merely never repeating.
     */
    @Test
    fun `neither man takes more than one term more than the other`() {
        for ((name, run) in games()) {
            val sequence = beneficiaries(run)
            val p1 = sequence.count { it == Slot.PLAYER_1 }
            val p2 = sequence.count { it == Slot.PLAYER_2 }

            assertTrue(
                kotlin.math.abs(p1 - p2) <= 1,
                "$name: the contract favoured one man $p1 to $p2 — $sequence"
            )
        }
    }

    /** A trade is one turn on its combined benefit, not two turns for whoever is named first. */
    @Test
    fun `a trade counts once`() {
        val run = play(Setups.broad(), Plan(bundleAt = setOf(1, 3)))
        val trades = run.negotiation.signedRegular.filter { it.bundledTerm != null }
        assertTrue(trades.isNotEmpty(), "no trade was signed, so this proves nothing")

        // Each trade contributes a single entry to the turn order.
        val sequence = beneficiaries(run)
        sequence.zipWithNext().forEach { (a, b) ->
            assertTrue(a != b, "a trade handed the same man two turns running — $sequence")
        }
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Who came out ahead on each signed regular term, in signing order, with the balanced ones
     * left out. This mirrors what the selector itself counts: a bundle scores as one item on its
     * combined benefit.
     */
    private fun beneficiaries(state: GameState): List<Slot> =
        state.negotiation.signedRegular.mapNotNull { beneficiary(it) }

    private fun beneficiary(signed: SignedTerm): Slot? {
        val parts = signed.allTerms.mapNotNull { r ->
            ContentLibrary.termsById[r.termId]?.let { it to PartyBinding(r.giver, r.receiver) }
        }
        return if (parts.isEmpty()) signed.term.beneficiary else BenefitAnalysis.combinedBeneficiary(parts)
    }

    /** A spread of profiles, lengths and negotiating behaviour, so this is not one lucky game. */
    private fun games(): List<Pair<String, GameState>> = listOf(
        "broad, straight through" to play(Setups.broad(), Plan()),
        "broad, long" to play(Setups.broad(length = SessionLength.FULL), Plan()),
        "broad, rejections" to play(Setups.broad(), Plan(rejectAt = setOf(1, 2, 4))),
        "broad, counteroffers" to play(Setups.broad(), Plan(counterAt = setOf(2, 5))),
        "broad, trades" to play(Setups.broad(), Plan(bundleAt = setOf(2))),
        "mixed profile" to play(Setups.mixed(), Plan(), Setups.mixedAnswers()),
        "restrictive profile" to play(Setups.restrictive(), Plan(rejectAt = setOf(3)), Setups.restrictiveAnswers())
    )

    private fun play(
        setup: com.thecontract.core.model.SharedSetup,
        plan: Plan,
        answers: Map<String, Answer> = Setups.allAnswers(Answer.YES)
    ): GameState {
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        val driver = GameDriver(h, p1, p2, plan)
        driver.submitSetup(setup)
        driver.fillProfile(Slot.PLAYER_1, answers)
        driver.fillProfile(Slot.PLAYER_2, answers)
        val final = driver.run()
        check(final.phase == GamePhase.COMPLETED) { "the game did not finish: ${final.phase}" }
        return final
    }
}
