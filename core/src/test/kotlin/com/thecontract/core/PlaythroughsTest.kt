package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.engine.BenefitAnalysis
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
import com.thecontract.core.model.PartyBinding
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
 * Twenty complete games, played through the wire protocol, capturing every line of content that
 * reaches a phone screen and holding all of it to [ContentRules].
 *
 * [RenderSweepTest] proves the library reads correctly one item at a time. This proves it reads
 * correctly *in play*, where the text a player actually sees has been through the maybe
 * conditions and counteroffer amendments that rewrite an instruction before it is shown. An
 * amendment that appended a clause to a sentence which already ended in one would only ever show
 * up here.
 *
 * The twenty games span all four explicitness registers, both session lengths, every finale format,
 * full and stripped equipment, and profiles from everything-yes to heavily bounded, so the run
 * covers the eligibility paths that decide which content is reachable at all.
 *
 * Every screen is written to `build/reports/playthroughs.txt` for reading by eye, which is the
 * only check that catches a sentence that is grammatical, specific, and still nonsense.
 */
class PlaythroughsTest {

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
                defaultMaybeCondition = MaybeCondition.UNDER_TWO_MINUTES,
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
        ),
        Game(
            "11 · Extremely filthy, top and bottom, full kit, bundle-heavy",
            SharedSetup(
                player1 = PlayerSetup("Gil", Role.DOMINANT, AnalRole.TOP),
                player2 = PlayerSetup("Ross", Role.SUBMISSIVE, AnalRole.BOTTOM),
                sessionLength = SessionLength.FULL,
                explicitness = Explicitness.EXTREME,
                finaleFormat = FinaleFormat.THREE_ORDERS,
                stopWord = "red",
                defaultMaybeCondition = MaybeCondition.SAVE_FOR_FINALE,
                boundaries = emptySet(),
                equipment = Equipment.entries.toSet()
            ),
            Setups.allAnswers(Answer.YES),
            Plan(bundleAt = setOf(1, 3, 5), counterAt = setOf(2))
        ),
        Game(
            "12 · Filthy, bottom and top reversed, no marks, no degradation",
            SharedSetup(
                player1 = PlayerSetup("Ade", Role.SUBMISSIVE, AnalRole.BOTTOM),
                player2 = PlayerSetup("Bruno", Role.DOMINANT, AnalRole.TOP),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.FILTHY,
                finaleFormat = FinaleFormat.UNINTERRUPTED,
                stopWord = "amber",
                defaultMaybeCondition = MaybeCondition.WITHOUT_TOY,
                boundaries = setOf(Boundary.NO_MARKS, Boundary.NO_DEGRADATION),
                equipment = Equipment.entries.toSet()
            ),
            Setups.mixedAnswers(3),
            Plan(rejectAt = setOf(1, 2), counterAt = setOf(4))
        ),
        Game(
            "13 · Erotic, everything maybe, minimal kit, midpoint checks throughout",
            SharedSetup(
                player1 = PlayerSetup("Hal", Role.DOMINANT, AnalRole.VERS_BOTTOM),
                player2 = PlayerSetup("Ira", Role.SUBMISSIVE, AnalRole.VERS_TOP),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.EROTIC,
                finaleFormat = FinaleFormat.DOMINANT_PRIVATE,
                stopWord = "stop",
                defaultMaybeCondition = MaybeCondition.MASSAGE_FIRST,
                boundaries = setOf(Boundary.MASSAGE_NONSEXUAL),
                equipment = setOf(Equipment.MASSAGE_OIL, Equipment.TOWELS)
            ),
            Setups.allAnswers(Answer.MAYBE),
            Plan(counterAt = setOf(2, 4), backAt = setOf(1))
        ),
        Game(
            "14 · Direct, no restraints, no roleplay, toys only",
            SharedSetup(
                player1 = PlayerSetup("Jem", Role.DOMINANT, AnalRole.VERS),
                player2 = PlayerSetup("Kai", Role.SUBMISSIVE, AnalRole.VERS),
                sessionLength = SessionLength.FULL,
                explicitness = Explicitness.DIRECT,
                finaleFormat = FinaleFormat.THREE_ORDERS,
                stopWord = "red",
                defaultMaybeCondition = MaybeCondition.WITHOUT_RESTRAINT,
                boundaries = setOf(Boundary.NO_RESTRAINTS, Boundary.NO_ROLEPLAY),
                equipment = setOf(
                    Equipment.LUBRICANT, Equipment.DILDO, Equipment.VIBRATOR,
                    Equipment.ANAL_PLUG, Equipment.SLEEVE, Equipment.TOWELS
                )
            ),
            Setups.allAnswers(Answer.YES),
            Plan(bundleAt = setOf(2), repeatConsiderationAt = setOf(3))
        ),
        Game(
            "15 · Extremely filthy, no anal penetration but anal play allowed",
            SharedSetup(
                player1 = PlayerSetup("Lars", Role.DOMINANT, AnalRole.NO_ANAL),
                player2 = PlayerSetup("Milo", Role.SUBMISSIVE, AnalRole.NO_ANAL),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.EXTREME,
                finaleFormat = FinaleFormat.UNINTERRUPTED,
                stopWord = "red",
                defaultMaybeCondition = MaybeCondition.ROLES_REVERSED,
                boundaries = setOf(Boundary.NO_ANAL_PENETRATION),
                equipment = Equipment.entries.toSet()
            ),
            Setups.allAnswers(Answer.YES),
            Plan(counterAt = setOf(1), bothCounterAt = setOf(3))
        ),
        Game(
            "16 · Filthy, full session, everything rejected once",
            Setups.broad(SessionLength.FULL, FinaleFormat.THREE_ORDERS).copy(
                explicitness = Explicitness.FILTHY,
                defaultMaybeCondition = MaybeCondition.UNDER_TWO_MINUTES
            ),
            Setups.mixedAnswers(4),
            Plan(rejectAt = setOf(1, 3, 5, 7), counterAt = setOf(2, 6))
        ),
        Game(
            "17 · Erotic, both erection difficulty, no orgasm control",
            SharedSetup(
                player1 = PlayerSetup("Nate", Role.DOMINANT, AnalRole.VERS, erectionDifficulty = true),
                player2 = PlayerSetup("Otto", Role.SUBMISSIVE, AnalRole.VERS, erectionDifficulty = true),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.EROTIC,
                finaleFormat = FinaleFormat.UNINTERRUPTED,
                stopWord = "stop",
                defaultMaybeCondition = MaybeCondition.GENTLER,
                boundaries = setOf(Boundary.NO_ORGASM_CONTROL, Boundary.NO_PAIN, Boundary.NO_MARKS),
                equipment = setOf(Equipment.MASSAGE_OIL, Equipment.LUBRICANT, Equipment.MASSAGE_CANDLE, Equipment.TOWELS)
            ),
            Setups.allAnswers(Answer.YES),
            Plan(counterAt = setOf(1, 3), backAtConsideration = setOf(2))
        ),
        Game(
            "18 · Direct, impact gear only, counters and a trade",
            SharedSetup(
                player1 = PlayerSetup("Piet", Role.DOMINANT, AnalRole.VERS_TOP),
                player2 = PlayerSetup("Quinn", Role.SUBMISSIVE, AnalRole.VERS_BOTTOM),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.DIRECT,
                finaleFormat = FinaleFormat.DOMINANT_PRIVATE,
                stopWord = "amber",
                defaultMaybeCondition = MaybeCondition.ONLY_AS_TRADE,
                boundaries = emptySet(),
                equipment = setOf(
                    Equipment.LUBRICANT, Equipment.PADDLE, Equipment.FLOGGER,
                    Equipment.CROP, Equipment.NIPPLE_CLAMPS, Equipment.TOWELS
                )
            ),
            Setups.allAnswers(Answer.YES),
            Plan(bundleAt = setOf(1), bothCounterAt = setOf(2), rejectAt = setOf(4))
        ),
        Game(
            "19 · Extremely filthy, full session, stop-before-orgasm default",
            Setups.broad(SessionLength.FULL, FinaleFormat.UNINTERRUPTED).copy(
                defaultMaybeCondition = MaybeCondition.STOP_BEFORE_ORGASM
            ),
            Setups.mixedAnswers(5),
            Plan(counterAt = setOf(1, 4), bundleAt = setOf(2), backAt = setOf(3), repeatConsiderationAt = setOf(5))
        ),
        Game(
            "20 · Erotic, most restrictive profile the game still allows",
            SharedSetup(
                player1 = PlayerSetup("Rhys", Role.DOMINANT, AnalRole.NO_ANAL, erectionDifficulty = true),
                player2 = PlayerSetup("Stan", Role.SUBMISSIVE, AnalRole.NO_ANAL, erectionDifficulty = true),
                sessionLength = SessionLength.QUICK,
                explicitness = Explicitness.EROTIC,
                finaleFormat = FinaleFormat.THREE_ORDERS,
                stopWord = "stop",
                defaultMaybeCondition = MaybeCondition.GENTLER,
                boundaries = setOf(
                    Boundary.NO_ANAL_ACTIVITY, Boundary.NO_ANAL_PENETRATION, Boundary.NO_TOYS,
                    Boundary.NO_PAIN, Boundary.NO_ROUGH_SEX, Boundary.NO_DEGRADATION,
                    Boundary.NO_FOOT_PLAY, Boundary.NO_MARKS, Boundary.NO_SPIT_PLAY,
                    Boundary.NO_RESTRAINTS, Boundary.NO_ROLEPLAY
                ),
                equipment = setOf(Equipment.MASSAGE_OIL, Equipment.TOWELS)
            ),
            Setups.allAnswers(Answer.YES),
            Plan()
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

    /**
     * What the opening term may not reach for. Buttocks are deliberately not on this list: act
     * one is massage and kissing, and "down to the top of his ass" is a landmark on a lower-back
     * massage rather than somewhere the first term of the night is going.
     */
    private val TOO_FAR_FOR_THE_OPENING =
        Regex("\\b(cock|hole|balls|anus|rims?|fucks?|swallows)\\b", RegexOption.IGNORE_CASE)

    /**
     * The rules the game itself has to keep, checked against the running session rather than
     * against the library, because these are engine behaviour and not authored text:
     *
     *  - the act never runs backwards, so the contract gets more intense from first term to last;
     *  - the opening term stays away from cocks and holes;
     *  - consideration is chosen by the man who gained *less* from the term, and every option he
     *    is shown is worked on him rather than by him.
     */
    private fun checkCriteria(h: Harness, game: Game, failures: MutableList<String>) {
        val s = h.state()
        val signed = s.negotiation.signed.filterNot { it.term.climax }
        val levels = signed.map { it.term.level }
        if (levels != levels.sorted()) {
            failures += "${game.label}: signed term levels run backwards: $levels"
        }
        signed.firstOrNull()?.let { first ->
            if (first.term.level != 1) {
                failures += "${game.label}: opening term is level ${first.term.level}, not 1"
            }
            TOO_FAR_FOR_THE_OPENING.find(first.term.instruction)?.let {
                failures += "${game.label}: opening term reaches for \"${it.value}\": ${first.term.instruction}"
            }
        }
        val receipts = s.negotiation.receipts.associateBy { it.id }
        signed.forEach { entry ->
            val consideration = entry.considerationReceiptId?.let { receipts[it] } ?: return@forEach
            // A bundle is paid for as one thing, so who owes is decided across both terms.
            val beneficiary = if (entry.bundledTerm != null) {
                BenefitAnalysis.combinedBeneficiary(
                    entry.allTerms.mapNotNull { rendered ->
                        ContentLibrary.termsById[rendered.termId]
                            ?.let { it to PartyBinding(rendered.giver, rendered.receiver) }
                    }
                )
            } else {
                entry.term.beneficiary
            } ?: return@forEach
            if (consideration.mutual) return@forEach
            if (consideration.performer != beneficiary) {
                failures += "${game.label}: ${consideration.actionId} was performed by the man who " +
                    "already gained more from ${entry.term.termId}"
            }
            if (consideration.recipient != beneficiary.other) {
                failures += "${game.label}: ${consideration.actionId} was not worked on the man who was owed"
            }
        }
    }

    @Test
    fun `twenty complete games, every screen read and checked`() {
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
            checkCriteria(h, game, failures)

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
        println("wrote ${out.absolutePath}: $totalLines lines of content across twenty games")

        assertTrue(totalLines > 900, "only $totalLines lines seen; the games are not covering the library")
        failures.forEach(::println)
        assertEquals(0, failures.size, "content above reached a phone screen in a state it cannot be acted on")
    }
}
