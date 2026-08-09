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
import com.thecontract.core.style.Lexicon
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Renders the whole library in every register and fails on anything a player could not carry out.
 *
 * Grepping the source cannot catch these. The defects only exist after `{G}` and `{R}` become
 * "Marcus" and "Dan" and `[v_pull_hair]` becomes whichever of its four strings this register
 * uses — "sucks both his ear", "fists his hair", or a sentence that opens with "he strokes him"
 * before either man has been named. The rules themselves live in [ContentRules].
 */
class RenderSweepTest {

    private val names = listOf("Marcus", "Dan")

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

    @Test
    fun `sweep every rendered instruction`() {
        val failures = mutableListOf<String>()
        for (explicitness in Explicitness.entries) {
            val c = ctx(explicitness)
            val binding = PartyBinding(Slot.PLAYER_1, Slot.PLAYER_2)
            (ContentLibrary.regularTerms + ContentLibrary.climaxTerms).forEach { term ->
                val text = Renderer.renderTerm(term, binding, c).instruction
                val problems = ContentRules.problems(text, names)
                if (problems.isNotEmpty()) {
                    failures += "TERM ${term.id} [$explicitness] ${problems.joinToString("; ")}\n    $text"
                }
            }
            ContentLibrary.considerations.forEach { action ->
                val text = Renderer.renderConsideration(action, Slot.PLAYER_1, Slot.PLAYER_2, c).instruction
                val problems = ContentRules.problems(text, names)
                if (problems.isNotEmpty()) {
                    failures += "CONS ${action.id} [$explicitness] ${problems.joinToString("; ")}\n    $text"
                }
            }
        }
        failures.forEach(::println)
        assertEquals(0, failures.size, "rendered instructions above cannot be carried out as written")
    }

    /**
     * Timer labels reach the phone next to the instruction, so a label that names a different act
     * from the one being timed is as misleading as a bad instruction.
     */
    @Test
    fun `sweep every rendered timer label`() {
        val failures = mutableListOf<String>()
        val c = ctx(Explicitness.EXTREME)
        val binding = PartyBinding(Slot.PLAYER_1, Slot.PLAYER_2)
        (ContentLibrary.regularTerms + ContentLibrary.climaxTerms).forEach { term ->
            Renderer.renderTerm(term, binding, c).timers.forEach { timer ->
                if (timer.label.isBlank()) failures += "TERM ${term.id} has a blank timer label"
                if (timer.seconds !in 10..600) {
                    failures += "TERM ${term.id} timer \"${timer.label}\" is ${timer.seconds}s"
                }
            }
        }
        ContentLibrary.considerations.forEach { action ->
            Renderer.renderConsideration(action, Slot.PLAYER_1, Slot.PLAYER_2, c).timers.forEach { timer ->
                if (timer.label.isBlank()) failures += "CONS ${action.id} has a blank timer label"
                if (timer.seconds !in 10..600) {
                    failures += "CONS ${action.id} timer \"${timer.label}\" is ${timer.seconds}s"
                }
            }
        }
        failures.forEach(::println)
        assertEquals(0, failures.size, "timer labels above are wrong")
    }

    /**
     * The slot contract from [Lexicon]: a verb key takes its object from the template, so no
     * variant may carry an object of its own, and force belongs to the adverb rather than the
     * verb. "fists" broke the first rule; "grinds his palms into" broke the second wherever the
     * template went on to say "with light pressure".
     */
    @Test
    fun `every lexicon verb is a bare transitive verb phrase`() {
        val carriesItsOwnObject = Regex(
            "\\b(?:his|her|their|it|him|the) (?:hair|cock|ass|mouth|throat)\\b$",
            RegexOption.IGNORE_CASE
        )
        val failures = mutableListOf<String>()
        Lexicon.keys.filter { it.startsWith("v_") }.forEach { key ->
            Lexicon.variants(key).forEachIndexed { i, value ->
                val register = Explicitness.entries[i]
                if (carriesItsOwnObject.containsMatchIn(value)) {
                    failures += "[$key] $register = \"$value\" already carries its own object"
                }
                if (value != value.trim() || value.endsWith(".") || value.endsWith(",")) {
                    failures += "[$key] $register = \"$value\" is not a clean drop-in"
                }
            }
        }
        failures.forEach(::println)
        assertEquals(0, failures.size, "lexicon verbs above break the slot contract")
    }

    /** Nothing in the library may reference a key the lexicon does not define. */
    @Test
    fun `every token used in the library resolves`() {
        val used = (ContentLibrary.regularTerms + ContentLibrary.climaxTerms)
            .flatMap { listOf(it.base, it.explicit, it.title) }
            .plus(ContentLibrary.considerations.flatMap { listOf(it.base, it.explicit, it.title) })
            .flatMap { Regex("\\[([a-z_]+)]").findAll(it).map { m -> m.groupValues[1] }.toList() }
            .toSet()
        val unknown = used.filterNot { Lexicon.has(it) }
        assertTrue(unknown.isEmpty(), "content references undefined lexicon keys: $unknown")
    }
}
