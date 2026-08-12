package com.thecontract.core

import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Plan
import com.thecontract.core.harness.Setups
import com.thecontract.core.model.AnalRole
import com.thecontract.core.model.Answer
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.Explicitness
import com.thecontract.core.model.FinaleFormat
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.GameState
import com.thecontract.core.model.MaybeCondition
import com.thecontract.core.model.PlayerSetup
import com.thecontract.core.model.RenderedConsideration
import com.thecontract.core.model.RenderedTerm
import com.thecontract.core.model.Role
import com.thecontract.core.model.SessionLength
import com.thecontract.core.model.SharedSetup
import com.thecontract.core.model.Slot
import com.thecontract.core.protocol.TermCard
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Five complete quick-session games, written out as one plain-text readout.
 *
 * Narrower than [PlaythroughsTest], which harvests every screen and holds all of it to
 * [ContentRules]. This captures only the four things a reader asked to see, in the order the pair
 * are shown them:
 *
 *  - every **term presented** for negotiation, including the amended and traded re-presentations;
 *  - every **consideration action presented** on the private list, not only the one chosen;
 *  - every **final (closing) term presented** — the private list each man picks his own finish from;
 *  - every **final consideration action presented** for those closing terms.
 *
 * It asserts nothing about the words. The output is the deliverable.
 */
class QuickPlaythroughsReadout {

    private data class Game(
        val label: String,
        val setup: SharedSetup,
        val answers: Map<String, Answer>,
        val plan: Plan
    )

    /** All five are QUICK sessions, spread across the registers, roles, boundaries and kit. */
    private fun games(): List<Game> = listOf(
        Game(
            "1 — everything yes, Extremely filthy, straight signatures",
            Setups.broad(SessionLength.QUICK, FinaleFormat.THREE_ORDERS),
            Setups.allAnswers(Answer.YES),
            Plan()
        ),
        Game(
            "2 — mixed answers, Filthy, two counteroffers, private Dominant finale",
            Setups.mixed(),
            Setups.mixedAnswers(),
            Plan(counterAt = setOf(1, 3))
        ),
        Game(
            "3 — no anal, no toys, no pain, Erotic, one rejection",
            Setups.restrictive(),
            Setups.restrictiveAnswers(),
            Plan(rejectAt = setOf(2))
        ),
        Game(
            "4 — Direct register, top and bottom, full kit, a trade and a counteroffer",
            SharedSetup(
                player1 = PlayerSetup("Aaron", Role.DOMINANT, AnalRole.TOP),
                player2 = PlayerSetup("Vic", Role.SUBMISSIVE, AnalRole.BOTTOM),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.DIRECT,
                finaleFormat = FinaleFormat.THREE_ORDERS,
                stopWord = "red",
                defaultMaybeCondition = MaybeCondition.GENTLER,
                boundaries = emptySet(),
                equipment = Equipment.entries.toSet()
            ),
            Setups.allAnswers(Answer.YES),
            Plan(bundleAt = setOf(2), counterAt = setOf(4))
        ),
        Game(
            "5 — everything maybe, Erotic, no marks, both counter, restraint-free kit",
            SharedSetup(
                player1 = PlayerSetup("Noel", Role.DOMINANT, AnalRole.VERS),
                player2 = PlayerSetup("Reg", Role.SUBMISSIVE, AnalRole.VERS),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.EROTIC,
                finaleFormat = FinaleFormat.UNINTERRUPTED,
                stopWord = "stop",
                defaultMaybeCondition = MaybeCondition.UNDER_TWO_MINUTES,
                boundaries = setOf(Boundary.NO_MARKS),
                equipment = setOf(
                    Equipment.MASSAGE_OIL, Equipment.LUBRICANT, Equipment.TOWELS, Equipment.MUSIC
                )
            ),
            Setups.allAnswers(Answer.MAYBE),
            Plan(bothCounterAt = setOf(2), rejectAt = setOf(4))
        )
    )

    /**
     * One negotiation slot's worth of readout — a proposal, a private closing-term list, or a
     * closing term — accumulated in the order the players were shown it.
     *
     * [seen] keys the things already written down, so a screen the driver revisits (a term still
     * open while the other man answers, a list re-read after a Back) is not printed twice, while a
     * genuinely different presentation of the same term — amended by a counteroffer, or reissued
     * carrying the second half of a trade — is.
     */
    private class Section(val id: String, val heading: String) {
        val blocks = mutableListOf<String>()
        val seen = mutableSetOf<String>()
        var termsWritten = 0
        var outcome: String? = null
    }

    @Test
    fun `five quick playthroughs, every presented term and consideration action`() {
        val out = StringBuilder()
        out.append(PREAMBLE)

        games().forEach { game ->
            val h = Harness().startGame()
            val p1 = h.newPhone("p1").join()
            val p2 = h.newPhone("p2").join()
            val driver = GameDriver(h, p1, p2, game.plan)
            check(driver.submitSetup(game.setup)) { "${game.label}: setup refused" }
            driver.fillProfile(Slot.PLAYER_1, game.answers)
            driver.fillProfile(Slot.PLAYER_2, game.answers)

            val sections = LinkedHashMap<String, Section>()
            var lastProposalId: String? = null
            var signedCount = 0
            var declinedCount = h.state().negotiation.declinedTermIds.size

            var steps = 0
            while (h.state().phase != GamePhase.COMPLETED && steps++ < 4000) {
                val s = h.state()

                // Attribute the previous step's result to the proposal that was open for it.
                lastProposalId?.let { id ->
                    val section = sections[id]
                    if (s.negotiation.signed.size > signedCount) {
                        val entry = s.negotiation.signed.last()
                        section?.outcome = "SIGNED as contract item #${entry.index + 1}"
                    } else if (s.negotiation.declinedTermIds.size > declinedCount) {
                        section?.outcome = "REJECTED — withdrawn and replaced, no contract slot used"
                    }
                }
                signedCount = s.negotiation.signed.size
                declinedCount = s.negotiation.declinedTermIds.size

                record(h, s, sections)
                s.negotiation.current?.let { lastProposalId = it.proposalId }
                driver.runOneStep()
            }
            assertEquals(GamePhase.COMPLETED, h.state().phase, "${game.label} did not finish")

            val final = h.state()
            lastProposalId?.let { id ->
                if (final.negotiation.signed.size > signedCount) {
                    sections[id]?.outcome = "SIGNED as contract item #${final.negotiation.signed.size}"
                }
            }
            record(h, final, sections)

            out.append(gameHeader(game, final))
            sections.values.forEach { section ->
                out.append('\n').append(section.heading).append('\n')
                out.append("-".repeat(section.heading.length)).append('\n')
                section.blocks.forEach { out.append(it) }
                section.outcome?.let { out.append("  => ").append(it).append('\n') }
            }
            out.append('\n')
        }

        val file = File(System.getProperty("contract.readout") ?: "build/quick-playthroughs.txt")
        file.parentFile?.mkdirs()
        file.writeText(out.toString())
        println("wrote ${file.absolutePath}")
    }

    /** Everything on the board right now that the readout has not already written down. */
    private fun record(h: Harness, s: GameState, sections: LinkedHashMap<String, Section>) {
        val n = s.negotiation

        // The private list a man picks his own closing term from. It is only ever on his phone,
        // so it is read from his phone view rather than from the shared state.
        if (s.phase == GamePhase.CLOSING_TERM_SELECTION && n.current == null) {
            val turn = n.closingTurn
            if (turn != null) {
                val options = h.phoneView(turn).termOptions
                val section = section(sections, "offer-${turn.name}", "FINAL TERM — ${s.setup.name(turn)} chooses")
                if (options.isNotEmpty() && section.seen.add(options.joinToString("|") { it.termId })) {
                    section.blocks += "\n  FINAL TERMS PRESENTED to ${s.setup.name(turn)} — " +
                        "${options.size} options, private to his phone\n"
                    options.forEachIndexed { i, option -> section.blocks += card(i + 1, option) }
                }
            }
        }

        val current = n.current ?: return
        val closing = current.term.climax
        val section = section(
            sections,
            current.proposalId,
            if (closing) {
                "FINAL TERM — ${s.setup.name(current.term.receiver)} finishes"
            } else {
                "PROPOSAL ${current.proposalId.removePrefix("p")}"
            }
        )

        // The term as it stands. A counteroffer rewrites the instruction and the rewritten term is
        // put in front of the pair again, so it is written down again.
        if (section.seen.add("term|${current.term.termId}|${current.term.instruction}")) {
            val label = when {
                section.termsWritten == 0 && closing ->
                    "FINAL TERM PRESENTED (chosen from his private list, now put to both)"
                section.termsWritten == 0 -> "TERM PRESENTED"
                current.appliedAmendments.isNotEmpty() ->
                    "TERM RE-PRESENTED, amended: " + current.appliedAmendments.joinToString(", ") { it.name }
                else -> "TERM RE-PRESENTED"
            }
            section.termsWritten++
            section.blocks += "\n  $label\n" + term(s, current.term)
            current.blockedBoundary?.let {
                section.blocks += "    note: a stronger term was skipped here for the boundary \"$it\"\n"
            }
        }
        // A trade attaches a second term to the same proposal. The pair approve it, pay for it and
        // sign it as one item, so it belongs in this section rather than one of its own.
        current.bundledTerm?.let { traded ->
            if (section.seen.add("traded|${traded.termId}|${traded.instruction}")) {
                section.blocks += "\n  TERM PRESENTED as a trade against the one above — " +
                    "the two are signed and paid for together\n" + term(s, traded)
            }
        }

        // The private consideration list, whoever it belongs to, in full.
        val choosing = s.phase == GamePhase.CONSIDERATION_PRIVATE_SELECTION ||
            s.phase == GamePhase.CLOSING_TERM_CONSIDERATION
        if (choosing && current.considerationOptions.isNotEmpty()) {
            val key = "options|" + current.considerationOptions.joinToString("|") { it.actionId }
            if (section.seen.add(key)) {
                val chooser = current.beneficiary?.other
                val who = chooser?.let { s.setup.name(it) } ?: "both men (balanced term)"
                val what = if (closing) "FINAL CONSIDERATION ACTIONS" else "CONSIDERATION ACTIONS"
                section.blocks += "\n  $what PRESENTED to $who — " +
                    "${current.considerationOptions.size} options, private to his phone\n"
                current.considerationOptions.forEachIndexed { i, c ->
                    section.blocks += consideration(s, i + 1, c)
                }
            }
        }
        current.consideration?.let { chosen ->
            if (section.seen.add("chosen|${chosen.actionId}")) {
                val what = if (closing) "final consideration" else "consideration"
                section.blocks += "\n  -> chose as $what: ${chosen.title} (${chosen.actionId})\n" +
                    "     performed by ${s.setup.name(chosen.performer)} on " +
                    "${s.setup.name(chosen.recipient)}${if (chosen.mutual) ", both at once" else ""}\n"
            }
        }
    }

    private fun section(sections: LinkedHashMap<String, Section>, id: String, heading: String): Section =
        sections.getOrPut(id) { Section(id, heading) }

    private fun term(s: GameState, r: RenderedTerm): String = buildString {
        append("    ").append(r.title).append("   [").append(r.termId).append(" · level ")
            .append(r.level).append(if (r.climax) " · closing" else "").append("]\n")
        append("      ").append(r.instruction).append('\n')
        // A closing term binds the man who is guaranteed to finish as the *receiver*, which reads
        // backwards as "does it to" against an instruction in which he is plainly the one coming.
        if (r.climax) {
            append("      giver ").append(s.setup.name(r.giver)).append(" · receiver ")
                .append(s.setup.name(r.receiver)).append(" — this is the term that guarantees ")
                .append(s.setup.name(r.receiver)).append(" finishes\n")
            append("      benefit: ").append(r.beneficiary?.let { s.setup.name(it) } ?: "balanced").append('\n')
        } else {
            append("      ").append(s.setup.name(r.giver)).append(" does it to ").append(s.setup.name(r.receiver))
            append(if (r.mutual) " (both at once)" else "")
            append("; benefit: ").append(r.beneficiary?.let { s.setup.name(it) } ?: "balanced").append('\n')
        }
        if (r.equipmentUsed.isNotEmpty()) append("      uses: ").append(r.equipmentUsed.joinToString(", ")).append('\n')
        if (r.timers.isNotEmpty()) {
            append("      timing: ").append(r.timers.joinToString(" | ") { "${it.label} ${it.seconds}s" }).append('\n')
        }
        r.conditions.forEach { append("      condition: ").append(it).append('\n') }
        r.amendments.forEach { append("      amendment: ").append(it).append('\n') }
        r.suggestions.forEach { g ->
            append("      ").append(g.heading).append(": ").append(g.items.joinToString(" / ")).append('\n')
        }
    }

    private fun card(number: Int, c: TermCard): String = buildString {
        append("    ").append(number).append(". ").append(c.title).append("   [").append(c.termId)
            .append(" · level ").append(c.level).append("]\n")
        append("       ").append(c.instruction).append('\n')
        if (c.climax) {
            append("       giver ").append(c.giverName).append(" · receiver ").append(c.receiverName)
                .append(" — ").append(c.receiverName).append(" is the one guaranteed to finish\n")
        } else {
            append("       ").append(c.giverName).append(" does it to ").append(c.receiverName)
            append(if (c.mutual) " (both at once)" else "").append('\n')
        }
        if (c.equipment.isNotEmpty()) append("       uses: ").append(c.equipment.joinToString(", ")).append('\n')
        if (c.timers.isNotEmpty()) {
            append("       timing: ").append(c.timers.joinToString(" | ") { "${it.label} ${it.totalSeconds}s" }).append('\n')
        }
        c.conditions.forEach { append("       condition: ").append(it).append('\n') }
        c.suggestions.forEach { g ->
            append("       ").append(g.heading).append(": ").append(g.items.joinToString(" / ")).append('\n')
        }
    }

    private fun consideration(s: GameState, number: Int, c: RenderedConsideration): String = buildString {
        append("    ").append(number).append(". ").append(c.title).append("   [").append(c.actionId)
            .append(" · intensity ").append(c.intensity).append("]\n")
        append("       ").append(c.instruction).append('\n')
        append("       ").append(s.setup.name(c.performer)).append(" on ").append(s.setup.name(c.recipient))
        append(if (c.mutual) " (both at once)" else "").append('\n')
        if (c.equipmentUsed.isNotEmpty()) append("       uses: ").append(c.equipmentUsed.joinToString(", ")).append('\n')
        if (c.timers.isNotEmpty()) {
            append("       timing: ").append(c.timers.joinToString(" | ") { "${it.label} ${it.seconds}s" }).append('\n')
        }
        c.suggestions.forEach { g ->
            append("       ").append(g.heading).append(": ").append(g.items.joinToString(" / ")).append('\n')
        }
    }

    private fun gameHeader(game: Game, s: GameState): String = buildString {
        append('\n').append("=".repeat(96)).append('\n')
        append("GAME ").append(game.label).append('\n')
        append("=".repeat(96)).append('\n')
        val one = game.setup.player1
        val two = game.setup.player2
        append(one.name).append(" — ").append(one.role).append(", ").append(one.analRole)
        append(if (one.erectionDifficulty) ", erection difficulty" else "").append('\n')
        append(two.name).append(" — ").append(two.role).append(", ").append(two.analRole)
        append(if (two.erectionDifficulty) ", erection difficulty" else "").append('\n')
        append("register ").append(game.setup.explicitness).append(" · ").append(game.setup.sessionLength)
            .append(" (").append(s.requiredRegularTerms).append(" regular terms + 2 closing)")
            .append(" · finale ").append(game.setup.finaleFormat)
            .append(" · stop word \"").append(game.setup.stopWord).append("\"\n")
        append("boundaries: ")
            .append(game.setup.boundaries.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "none")
            .append('\n')
        append("equipment: ").append(game.setup.equipment.size).append(" items\n")
        append("profile answers: ").append(answerSummary(game.answers)).append('\n')
    }

    private fun answerSummary(answers: Map<String, Answer>): String {
        val counts = Answer.entries.associateWith { a -> answers.values.count { it == a } }
        return counts.entries.filter { it.value > 0 }.joinToString(", ") { "${it.value} ${it.key}" }
    }

    private companion object {
        val PREAMBLE = """
            THE CONTRACT — FIVE QUICK PLAYTHROUGHS
            ======================================

            Five complete games, each a QUICK session played start to finish through the wire
            protocol by two simulated phones, exactly as two real phones would play it.

            Written out below, in the order the players are shown them:

              TERM PRESENTED                     a term put up for negotiation. A term re-appears
                                                 when a counteroffer rewrites it or a trade
                                                 attaches a second term to it; both are shown.
              CONSIDERATION ACTIONS PRESENTED    the whole private list offered to the man who
                                                 gained less from the term, not only his pick.
              FINAL TERMS PRESENTED              the private list each man chooses his own closing
                                                 term from, and then that term put to both.
              FINAL CONSIDERATION ACTIONS        the consideration list for a closing term.

            Nothing here is performed during negotiation: terms are agreed now and carried out in
            the final scene, consideration actions are performed on the spot to earn a signature.

            Selection is seeded from the session id, which is fresh every session, so this is one
            sample of five games rather than a fixed script — re-running deals different content
            against the same five setups.

            Adults only. Generated by QuickPlaythroughsReadout.

        """.trimIndent()
    }
}
