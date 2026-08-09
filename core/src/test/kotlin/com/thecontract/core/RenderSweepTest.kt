package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.engine.GameContext
import com.thecontract.core.engine.Renderer
import com.thecontract.core.model.Answer
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.Explicitness
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.PlayerSetup
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.PrivateProfile
import com.thecontract.core.model.Role
import com.thecontract.core.model.SharedSetup
import com.thecontract.core.model.Slot
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Renders the whole library in every register and fails on anything that reads badly once the
 * names are substituted in. Grepping the source cannot catch these: the defects only exist after
 * `{G}` and `{R}` become "Marcus" and "Dan" — "sucks both his ear", or a sentence opening with
 * "he strokes him" before either man has been named.
 */
class RenderSweepTest {

    /**
     * "one of Dan's ear". Two of these survived every earlier pass because they only read wrong
     * once the possessive is substituted in, and both were caught by eye rather than by a check.
     */
    private val PLURAL_MISMATCH = Regex(
        "\\b(?:one|both|each) of \\w+(?:'s)? " +
            "(?:ear|hand|arm|leg|thigh|cheek|nipple|foot|calf|wrist|shoulder|knee)\\b",
        RegexOption.IGNORE_CASE
    )

    /**
     * "reaches round to strokes his cock". The lexicon hands back a third-person verb, which is
     * right everywhere except immediately after "to".
     */
    private val CONJUGATED_AFTER_TO = Regex(
        "\\bto (?:strokes|sucks|kisses|licks|rims|grips|pulls|pushes|takes|holds|works|" +
            "spanks|edges|teases|fucks|kneads|massages|rubs)\\b",
        RegexOption.IGNORE_CASE
    )

    /**
     * An instruction may only end on something both men can point at: the timer, a count, an
     * orgasm, or the other one saying so. "until the muscle stops fighting him", "until he goes
     * loose", "until he swears at him for it" are all judgement calls dressed up as instructions,
     * and every one of them was written before anybody noticed the pattern.
     */
    private val OBJECTIVE_UNTIL = Regex(
        // Orgasm in all four registers, a spoken cue, a timer, a count, or a plain physical
        // fact somebody can see. Anything else is one man guessing how the other one feels.
        "timer|finish|\\bcomes\\b|unloads|empties|is done|\\bdoes\\b|" +
            "tells him|is told|\\bsays\\b|is spoken to|permission|" +
            "\\bmoved\\b|all the way in|count of|\\bhurts\\b|\\bhard\\b|slick from",
        RegexOption.IGNORE_CASE
    )

    /**
     * A reciprocal instruction takes a plural verb. The lexicon only knows the third person
     * singular, so "{G} and {R} [v_rim] each other" came out as "Marcus and Dan eats each other".
     */
    private val COMPOUND_SINGULAR = Regex("\\b(?:Marcus|Dan) and (?:Marcus|Dan)\\b[^.]*?\\b\\w+s each other\\b")

    /** "Dan's the collar", from a possessive token in front of an equipment name. */
    private val DOUBLE_DETERMINER = Regex("\\b\\w+'s the \\b")

    /** "has him swallows him": the verb after "has him" has to be bare. */
    private val HAS_HIM_CONJUGATED = Regex("\\bhas him \\w+s\\b")

    private fun vagueUntil(text: String): String? =
        Regex("until ([^.,;]{3,70})").findAll(text)
            .map { it.groupValues[1].trim() }
            .firstOrNull { !OBJECTIVE_UNTIL.containsMatchIn(it) }

    private fun ctx(explicitness: Explicitness) = GameContext(
        SharedSetup(
            player1 = PlayerSetup("Marcus", Role.DOMINANT),
            player2 = PlayerSetup("Dan", Role.SUBMISSIVE),
            explicitness = explicitness,
            equipment = Equipment.entries.toSet()
        ),
        Slot.entries.associateWith { slot ->
            PrivateProfile(slot, PreferenceLibrary.all.associate { it.id to Answer.YES }, complete = true)
        }
    )

    /** A pronoun standing in for someone who has not been named yet in the sentence. */
    private fun danglingPronoun(text: String): Boolean {
        val firstName = Regex("\\b(Marcus|Dan)\\b").find(text)?.range?.first ?: return true
        val firstPronoun = Regex("\\b(he|him|his)\\b", RegexOption.IGNORE_CASE).find(text)?.range?.first
            ?: return false
        return firstPronoun < firstName
    }

    @Test
    fun `sweep every rendered instruction`() {
        var flagged = 0
        for (explicitness in Explicitness.entries) {
            val c = ctx(explicitness)
            val binding = PartyBinding(Slot.PLAYER_1, Slot.PLAYER_2)
            (ContentLibrary.regularTerms + ContentLibrary.climaxTerms).forEach { term ->
                val text = Renderer.renderTerm(term, binding, c).instruction
                val problems = buildList {
                    if (danglingPronoun(text)) add("pronoun before any name")
                    if (Regex("\\b(\\w+) \\1\\b").containsMatchIn(text)) add("doubled word")
                    if (text.contains("  ")) add("double space")
                    if (!text.trimEnd().endsWith(".")) add("no full stop")
                    if (PLURAL_MISMATCH.containsMatchIn(text)) add("singular noun after one of/both of")
                    if (CONJUGATED_AFTER_TO.containsMatchIn(text)) add("conjugated verb after \"to\"")
                    vagueUntil(text)?.let { add("ends on a judgement call: \"until $it\"") }
                    if (COMPOUND_SINGULAR.containsMatchIn(text)) add("singular verb, two subjects")
                    if (DOUBLE_DETERMINER.containsMatchIn(text)) add("possessive followed by \"the\"")
                    if (HAS_HIM_CONJUGATED.containsMatchIn(text)) add("conjugated verb after \"has him\"")
                }
                if (problems.isNotEmpty()) {
                    flagged++
                    println("TERM ${term.id} [$explicitness] ${problems.joinToString()}\n    $text")
                }
            }
            ContentLibrary.considerations.forEach { action ->
                val text = Renderer.renderConsideration(action, Slot.PLAYER_1, Slot.PLAYER_2, c).instruction
                val problems = buildList {
                    if (danglingPronoun(text)) add("pronoun before any name")
                    if (Regex("\\b(\\w+) \\1\\b").containsMatchIn(text)) add("doubled word")
                    if (text.contains("  ")) add("double space")
                    if (!text.trimEnd().endsWith(".")) add("no full stop")
                    if (PLURAL_MISMATCH.containsMatchIn(text)) add("singular noun after one of/both of")
                    if (CONJUGATED_AFTER_TO.containsMatchIn(text)) add("conjugated verb after \"to\"")
                    vagueUntil(text)?.let { add("ends on a judgement call: \"until $it\"") }
                    if (COMPOUND_SINGULAR.containsMatchIn(text)) add("singular verb, two subjects")
                    if (DOUBLE_DETERMINER.containsMatchIn(text)) add("possessive followed by \"the\"")
                    if (HAS_HIM_CONJUGATED.containsMatchIn(text)) add("conjugated verb after \"has him\"")
                }
                if (problems.isNotEmpty()) {
                    flagged++
                    println("CONS ${action.id} [$explicitness] ${problems.joinToString()}\n    $text")
                }
            }
        }
        assertEquals(0, flagged, "rendered instructions above read badly once the names are in")
    }
}
