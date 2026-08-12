package com.thecontract.core

import com.thecontract.core.engine.FinaleOrdering
import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Plan
import com.thecontract.core.harness.Setups
import com.thecontract.core.model.Answer
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.GameState
import com.thecontract.core.model.SessionLength
import com.thecontract.core.model.Slot
import java.io.File
import kotlin.test.Test

/**
 * Five complete games, with every screen a player is shown written out.
 *
 * Not an assertion — a readout. The validator and the render sweep hold the library to rules that
 * can be written down; this exists for the ones that cannot, which is whether an instruction can
 * actually be carried out by two men in a room. Only reading it tells you that.
 */
class PlaythroughReadout {

    @Test
    fun readout() {
        val out = StringBuilder()
        val games = listOf(
            "Game 1 — straight through" to Plan(),
            "Game 2 — two rejections" to Plan(rejectAt = setOf(2, 5)),
            "Game 3 — counteroffers" to Plan(counterAt = setOf(1, 4)),
            "Game 4 — a trade" to Plan(bundleAt = setOf(3)),
            "Game 5 — mixed" to Plan(rejectAt = setOf(3), counterAt = setOf(6), bundleAt = setOf(2))
        )
        games.forEachIndexed { i, (name, plan) -> out.append(play(name, plan, i)) }
        val file = File(System.getProperty("contract.rootDir") ?: ".", "build/playthrough-readout.txt")
        file.parentFile.mkdirs()
        file.writeText(out.toString())
        println("Readout written to " + file.absolutePath)
    }

    private fun play(name: String, plan: Plan, index: Int): String {
        val out = StringBuilder()
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        val driver = GameDriver(h, p1, p2, plan)
        driver.submitSetup(Setups.broad(SessionLength.QUICK))
        driver.fillProfile(Slot.PLAYER_1, Setups.allAnswers(Answer.YES))
        driver.fillProfile(Slot.PLAYER_2, Setups.allAnswers(Answer.YES))

        out.append("\n").append("=".repeat(78)).append("\n")
        out.append(name).append("\n")
        out.append("=".repeat(78)).append("\n")

        val seenTerms = LinkedHashSet<String>()
        val seenLists = LinkedHashSet<String>()
        var guard = 0
        while (h.state().phase != GamePhase.COMPLETED && guard++ < 6000) {
            val s = h.state()
            val current = s.negotiation.current
            if (current != null) {
                // Every term put in front of the pair, whether or not it ends up signed.
                if (s.phase == GamePhase.PROPOSAL && seenTerms.add(current.proposalId + "|" + current.term.termId)) {
                    out.append("\n--- TERM PRESENTED ").append("-".repeat(52)).append("\n")
                    out.append(term(s, current.term))
                    current.bundledTerm?.let {
                        out.append("\n    ...traded with:\n").append(term(s, it))
                    }
                }
                // Every option on a consideration screen, not only the one chosen.
                val key = current.proposalId + "|" + current.considerationOptions.size
                if (current.considerationOptions.isNotEmpty() &&
                    (s.phase == GamePhase.CONSIDERATION_PRIVATE_SELECTION ||
                        s.phase == GamePhase.CLOSING_TERM_CONSIDERATION) &&
                    seenLists.add(key)
                ) {
                    out.append("\n--- CONSIDERATION OPTIONS (")
                        .append(current.considerationOptions.size).append(") ").append("-".repeat(34)).append("\n")
                    current.considerationOptions.forEach { c ->
                        out.append("  [").append(c.intensity).append("] ").append(c.title).append("\n")
                        out.append("      ").append(c.instruction).append("\n")
                        if (c.timers.isNotEmpty()) {
                            out.append("      timing: ")
                                .append(c.timers.joinToString(" | ") { "${it.label} ${it.seconds}s" }).append("\n")
                        }
                    }
                }
            }
            driver.runOneStep()
        }

        val final = h.state()
        out.append("\n--- THE FINAL SCENE, IN ORDER ").append("-".repeat(42)).append("\n")
        FinaleOrdering.resolve(final).forEachIndexed { i, step ->
            val signed = final.negotiation.signed[step.signedIndex]
            val r = signed.half(step.bundled) ?: return@forEachIndexed
            out.append("\nStep ").append(i + 1)
            if (signed.bundledTerm != null) out.append(if (step.bundled) "  (second half of a trade)" else "  (first half of a trade)")
            out.append("\n").append(term(final, r))
        }

        out.append("\n--- CONSIDERATIONS PERFORMED ").append("-".repeat(43)).append("\n")
        final.negotiation.receipts.forEach { r ->
            out.append("\n  ").append(r.title).append("\n")
            out.append("      ").append(r.instruction).append("\n")
            out.append("      performed by ").append(final.setup.name(r.performer))
                .append(" for ").append(final.setup.name(r.recipient))
                .append(if (r.mutual) " (both)" else "").append("\n")
        }
        out.append("\n")
        return out.toString()
    }

    private fun term(s: GameState, r: com.thecontract.core.model.RenderedTerm): String {
        val b = StringBuilder()
        b.append("  ").append(r.title).append("   [level ").append(r.level).append("]\n")
        b.append("      ").append(r.instruction).append("\n")
        b.append("      ").append(s.setup.name(r.giver)).append(" does it to ")
            .append(s.setup.name(r.receiver))
        r.beneficiary?.let { b.append("; it is for ").append(s.setup.name(it)) }
        b.append("\n")
        if (r.equipmentUsed.isNotEmpty()) b.append("      uses: ").append(r.equipmentUsed.joinToString(", ")).append("\n")
        if (r.timers.isNotEmpty()) {
            b.append("      timing: ").append(r.timers.joinToString(" | ") { "${it.label} ${it.seconds}s" }).append("\n")
        }
        r.conditions.forEach { b.append("      condition: ").append(it).append("\n") }
        r.amendments.forEach { b.append("      amendment: ").append(it).append("\n") }
        return b.toString()
    }
}
