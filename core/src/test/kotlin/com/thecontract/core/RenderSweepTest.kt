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
