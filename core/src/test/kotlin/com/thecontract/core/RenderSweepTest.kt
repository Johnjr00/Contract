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

    /**
     * A lexicon key offers several wordings per register and the seed decides which one an item
     * gets, so rendering each item once would leave most of the alternatives unread. Walking the
     * seed through [Lexicon.SEED_CYCLE] takes every key through every one of its wordings in
     * every template that uses it.
     */
    private fun sweepSeeds(): IntRange = 0 until Lexicon.SEED_CYCLE

    @Test
    fun `sweep every rendered instruction`() {
        val failures = mutableListOf<String>()
        val binding = PartyBinding(Slot.PLAYER_1, Slot.PLAYER_2)
        for (explicitness in Explicitness.entries) {
            val c = ctx(explicitness)
            for (seed in sweepSeeds()) {
                (ContentLibrary.regularTerms + ContentLibrary.climaxTerms).forEach { term ->
                    val rc = Renderer.context(c, binding).copy(variantSeed = seed)
                    val text = com.thecontract.core.style.StyleEngine
                        .renderInstruction(term.base, term.explicit, term.extremeTail, rc)
                    val problems = ContentRules.problems(text, names)
                    if (problems.isNotEmpty()) {
                        failures += "TERM ${term.id} [$explicitness seed $seed] ${problems.joinToString("; ")}\n    $text"
                    }
                }
                ContentLibrary.considerations.forEach { action ->
                    val rc = Renderer.context(c, binding).copy(variantSeed = seed)
                    val text = com.thecontract.core.style.StyleEngine
                        .renderInstruction(action.base, action.explicit, action.extremeTail, rc)
                    val problems = ContentRules.problems(text, names)
                    if (problems.isNotEmpty()) {
                        failures += "CONS ${action.id} [$explicitness seed $seed] ${problems.joinToString("; ")}\n    $text"
                    }
                }
            }
        }
        failures.distinct().take(60).forEach(::println)
        assertEquals(0, failures.distinct().size, "rendered instructions above cannot be carried out as written")
    }

    /**
     * Repetition is a content defect in its own right. A wording that reaches the screen in a
     * large share of the library stops registering — twenty-four separate kissing terms once all
     * read "deep, wet and messy" — so no single lexicon wording may dominate its own key.
     */
    @Test
    fun `no single wording dominates the library`() {
        val binding = PartyBinding(Slot.PLAYER_1, Slot.PLAYER_2)
        val failures = mutableListOf<String>()
        val items = (ContentLibrary.regularTerms + ContentLibrary.climaxTerms)
            .map { it.id to listOf(it.base, it.explicit) }
            .plus(ContentLibrary.considerations.map { it.id to listOf(it.base, it.explicit) })

        for (explicitness in Explicitness.entries) {
            val c = ctx(explicitness)
            Lexicon.keys.forEach { key ->
                val options = Lexicon.alternatives(key, explicitness)
                if (options.size < 2) return@forEach
                val users = items.filter { (_, templates) -> templates.any { "[$key]" in it } }
                if (users.size < options.size * 2) return@forEach
                val counts = users.groupingBy { (id, _) ->
                    com.thecontract.core.style.Lexicon
                        .resolve(key, explicitness, Renderer.context(c, binding, id).variantSeed + key.hashCode())
                }.eachCount()
                // With the seed spread evenly, no wording should take more than three quarters
                // of the items that use the key.
                val worst = counts.maxBy { it.value }
                if (worst.value > users.size * 3 / 4) {
                    failures += "[$key] $explicitness: \"${worst.key}\" covers ${worst.value} of ${users.size} items"
                }
            }
        }
        failures.forEach(::println)
        assertEquals(0, failures.size, "wordings above are repeated across too much of the library")
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
            Explicitness.entries.forEach { register ->
                Lexicon.alternatives(key, register).forEach { value ->
                    if (carriesItsOwnObject.containsMatchIn(value)) {
                        failures += "[$key] $register = \"$value\" already carries its own object"
                    }
                    if (value != value.trim() || value.endsWith(".") || value.endsWith(",")) {
                        failures += "[$key] $register = \"$value\" is not a clean drop-in"
                    }
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
