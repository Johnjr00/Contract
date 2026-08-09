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
import com.thecontract.core.model.MaybeCondition
import com.thecontract.core.model.PlayerSetup
import com.thecontract.core.model.Role
import com.thecontract.core.model.SessionLength
import com.thecontract.core.model.SharedSetup
import com.thecontract.core.model.Slot
import com.thecontract.core.protocol.ClientView
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Ten complete games, played through the wire protocol, capturing every line of content that
 * reaches a phone screen and holding all of it to [ContentRules].
 *
 * [RenderSweepTest] proves the library reads correctly one item at a time. This proves it reads
 * correctly *in play*, where the text a player actually sees has been through the maybe
 * conditions and counteroffer amendments that rewrite an instruction before it is shown. An
 * amendment that appended a clause to a sentence which already ended in one would only ever show
 * up here.
 *
 * The ten games span all four explicitness registers, both session lengths, every finale format,
 * full and stripped equipment, and profiles from everything-yes to heavily bounded, so the run
 * covers the eligibility paths that decide which content is reachable at all.
 *
 * Every screen is written to `build/reports/playthroughs.txt` for reading by eye, which is the
 * only check that catches a sentence that is grammatical, specific, and still nonsense.
 */
class TenPlaythroughsTest {

    private data class Game(
        val label: String,
        val setup: SharedSetup,
        val answers: Map<String, Answer>,
        val plan: Plan
    )

    private fun games(): List<Game> = listOf(
        Game(
            "1 · everything yes, Extremely filthy, quick, straight signatures",
            Setups.broad(SessionLength.QUICK, FinaleFormat.THREE_ORDERS),
            Setups.allAnswers(Answer.YES),
            Plan()
        ),
        Game(
            "2 · everything yes, Extremely filthy, full session, rejections and counteroffers",
            Setups.broad(SessionLength.FULL, FinaleFormat.UNINTERRUPTED),
            Setups.allAnswers(Answer.YES),
            Plan(rejectAt = setOf(1, 4), counterAt = setOf(2, 6), bundleAt = setOf(3))
        ),
        Game(
            "3 · mixed answers, Filthy, private Dominant finale",
            Setups.mixed(),
            Setups.mixedAnswers(),
            Plan(counterAt = setOf(1, 3), bothCounterAt = setOf(5))
        ),
        Game(
            "4 · no anal, no toys, no pain, Erotic, uninterrupted finale",
            Setups.restrictive(),
            Setups.restrictiveAnswers(),
            Plan(rejectAt = setOf(2))
        ),
        Game(
            "5 · Direct register, top and bottom, full equipment",
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
            "6 · Erotic register, both vers, maybe-heavy profile",
            SharedSetup(
                player1 = PlayerSetup("Noel", Role.DOMINANT, AnalRole.VERS),
                player2 = PlayerSetup("Reg", Role.SUBMISSIVE, AnalRole.VERS),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.EROTIC,
                finaleFormat = FinaleFormat.UNINTERRUPTED,
                stopWord = "stop",
                defaultMaybeCondition = MaybeCondition.PAUSE_MIDWAY,
                boundaries = setOf(Boundary.NO_MARKS),
                equipment = setOf(Equipment.MASSAGE_OIL, Equipment.LUBRICANT, Equipment.TOWELS, Equipment.MUSIC)
            ),
            Setups.allAnswers(Answer.MAYBE),
            Plan(counterAt = setOf(1, 2, 3))
        ),
        Game(
            "7 · Filthy register, vers top and vers bottom, restraint gear only",
            SharedSetup(
                player1 = PlayerSetup("Kit", Role.DOMINANT, AnalRole.VERS_TOP),
                player2 = PlayerSetup("Jonah", Role.SUBMISSIVE, AnalRole.VERS_BOTTOM),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.FILTHY,
                finaleFormat = FinaleFormat.DOMINANT_PRIVATE,
                stopWord = "amber",
                defaultMaybeCondition = MaybeCondition.ONLY_AS_TRADE,
                boundaries = setOf(Boundary.NO_DEGRADATION),
                equipment = setOf(
                    Equipment.LUBRICANT, Equipment.CUFFS, Equipment.ROPE,
                    Equipment.BLINDFOLD, Equipment.COLLAR, Equipment.LEASH, Equipment.TOWELS
                )
            ),
            Setups.mixedAnswers(1),
            Plan(bundleAt = setOf(1), rejectAt = setOf(3), backAt = setOf(2))
        ),
        Game(
            "8 · Extremely filthy, full session, back-outs and a repeated consideration",
            Setups.broad(SessionLength.FULL, FinaleFormat.DOMINANT_PRIVATE),
            Setups.mixedAnswers(2),
            Plan(backAtConsideration = setOf(2), repeatConsiderationAt = setOf(4), counterAt = setOf(5))
        ),
        Game(
            "9 · Erotic, no rough sex, no restraints, no orgasm control",
            SharedSetup(
                player1 = PlayerSetup("Owen", Role.DOMINANT, AnalRole.VERS, erectionDifficulty = true),
                player2 = PlayerSetup("Silas", Role.SUBMISSIVE, AnalRole.VERS),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.EROTIC,
                finaleFormat = FinaleFormat.THREE_ORDERS,
                stopWord = "red",
                defaultMaybeCondition = MaybeCondition.STOP_BEFORE_ORGASM,
                boundaries = setOf(
                    Boundary.NO_ROUGH_SEX, Boundary.NO_RESTRAINTS,
                    Boundary.NO_ORGASM_CONTROL, Boundary.NO_PAIN
                ),
                equipment = setOf(Equipment.MASSAGE_OIL, Equipment.LUBRICANT, Equipment.FEATHER, Equipment.ICE)
            ),
            Setups.allAnswers(Answer.YES),
            Plan(counterAt = setOf(2), rejectAt = setOf(4))
        ),
        Game(
            "10 · Direct, no anal at all, minimal kit, everything countered",
            SharedSetup(
                player1 = PlayerSetup("Beau", Role.SUBMISSIVE, AnalRole.NO_ANAL),
                player2 = PlayerSetup("Cal", Role.DOMINANT, AnalRole.NO_ANAL),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.DIRECT,
                finaleFormat = FinaleFormat.UNINTERRUPTED,
                stopWord = "stop",
                defaultMaybeCondition = MaybeCondition.UNDER_TWO_MINUTES,
                boundaries = setOf(
                    Boundary.NO_ANAL_ACTIVITY, Boundary.NO_ANAL_PENETRATION,
                    Boundary.NO_TOYS, Boundary.NO_SPIT_PLAY, Boundary.NO_FOOT_PLAY
                ),
                equipment = setOf(Equipment.MASSAGE_OIL, Equipment.TOWELS)
            ),
            Setups.allAnswers(Answer.YES),
            Plan(bothCounterAt = setOf(1, 3), bundleAt = setOf(2))
        )
    )

    /** One line of content, with enough context to find it again in the source. */
    private data class Line(val screen: String, val id: String, val title: String, val text: String, val timing: String)

    private fun timing(timers: List<com.thecontract.core.protocol.TimerView>): String =
        if (timers.isEmpty()) "" else timers.joinToString(" · ") { "${it.label} ${it.totalSeconds}s" }

    /**
     * Everything readable on one phone screen.
     *
     * During the finale the card's instruction is deliberately blank and the paragraph lives in
     * `body` instead, so that the same text is not printed twice on one screen. Read it from
     * wherever the phone is actually showing it.
     */
    private fun harvest(view: ClientView, into: MutableSet<Line>) {
        view.execution?.let { exec ->
            if (view.body.isNotBlank()) {
                val id = exec.term?.termId ?: "step ${exec.stepIndex}"
                into += Line("finale step", id, view.heading, view.body, timing(exec.term?.timers.orEmpty()))
            }
        }
        view.term?.takeIf { it.instruction.isNotBlank() }
            ?.let { into += Line("term", it.termId, it.title, it.instruction, timing(it.timers)) }
        view.bundledTerm?.let { into += Line("bundled term", it.termId, it.title, it.instruction, timing(it.timers)) }
        view.termOptions.forEach {
            into += Line("term option", it.termId, it.title, it.instruction, timing(it.timers))
        }
        view.consideration?.let {
            into += Line("consideration", it.actionId, it.title, it.instruction, timing(it.timers))
        }
        view.considerationOptions.forEach {
            into += Line("consideration option", it.actionId, it.title, it.instruction, timing(it.timers))
        }
        view.draft?.byAct?.values?.flatten()?.forEach { signed ->
            into += Line("signed term", "#${signed.index}", signed.title, signed.instruction, timing(signed.timers))
            signed.bundledInstruction?.let { text ->
                into += Line("signed bundle", "#${signed.index}", signed.bundledTitle ?: "", text, "")
            }
            signed.considerationInstruction?.let { text ->
                into += Line("signed consideration", "#${signed.index}", signed.considerationTitle ?: "", text, "")
            }
        }
    }

    @Test
    fun `ten complete games, every screen read and checked`() {
        val report = StringBuilder()
        val failures = mutableListOf<String>()
        var totalLines = 0

        games().forEach { game ->
            val h = Harness().startGame()
            val p1 = h.newPhone("p1").join()
            val p2 = h.newPhone("p2").join()
            val driver = GameDriver(h, p1, p2, game.plan)
            check(driver.submitSetup(game.setup)) { "${game.label}: setup refused" }
            val seen = linkedSetOf<Line>()

            var steps = 0
            while (h.state().phase != GamePhase.COMPLETED && steps++ < 4000) {
                driver.runOneStep()
                Slot.entries.forEach { slot -> harvest(h.phoneView(slot), seen) }
            }
            assertEquals(GamePhase.COMPLETED, h.state().phase, "${game.label} did not finish")
            Slot.entries.forEach { slot -> harvest(h.phoneView(slot), seen) }

            val names = listOf(game.setup.player1.name, game.setup.player2.name)
            report.append("\n${"=".repeat(96)}\nGAME ${game.label}\n")
                .append("register ${game.setup.explicitness} · ${game.setup.sessionLength} · ")
                .append("finale ${game.setup.finaleFormat} · ${seen.size} distinct screens\n")
                .append("=".repeat(96)).append('\n')

            seen.forEach { line ->
                totalLines++
                report.append("\n[${line.screen}] ${line.id} — ${line.title}\n    ${line.text}\n")
                if (line.timing.isNotEmpty()) report.append("    timing: ${line.timing}\n")
                ContentRules.problems(line.text, names).forEach { problem ->
                    failures += "${game.label}\n  [${line.screen}] ${line.id}: $problem\n  ${line.text}"
                    report.append("    *** $problem\n")
                }
            }
        }

        val out = File("build/reports/playthroughs.txt")
        out.parentFile.mkdirs()
        out.writeText(report.toString())
        println("wrote ${out.absolutePath}: $totalLines lines of content across ten games")

        assertTrue(totalLines > 400, "only $totalLines lines seen; the games are not covering the library")
        failures.forEach(::println)
        assertEquals(0, failures.size, "content above reached a phone screen in a state it cannot be acted on")
    }
}
