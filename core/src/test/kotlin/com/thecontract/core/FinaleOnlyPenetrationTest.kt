package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The two things reserved for the end of the night: a man putting his cock in the other's arse,
 * and a man coming. Both belong to the closing terms, which the finale always performs last.
 *
 * A regular term is negotiated mid-evening and lands somewhere in the running order, so neither
 * can be written into one. Fingers, tongues, toys and fisting are not covered — this is about
 * intercourse, not everything that goes near a hole.
 */
class FinaleOnlyPenetrationTest {

    @Test
    fun `intercourse and finishing stay in the closing terms`() {
        val fucking = ContentLibrary.regularTerms.filter { term ->
            term.activities.any { it == "topping" || it == "bottoming" }
        }
        assertTrue(fucking.isEmpty(), "intercourse in a regular term: ${fucking.map { it.id }}")

        // Orgasm as the end of a term, not a standing rule about asking first: a term may well
        // say "before he comes" or "he does not come" and be right to.
        val comes = Regex(
            """\b(until he comes|until he finishes|lets him come|makes? him come|comes inside|""" +
                """comes in him|empties himself|unloads inside|gives him every drop)\b"""
        )
        val finishing = ContentLibrary.regularTerms.filter {
            comes.containsMatchIn(it.base + " " + it.explicit + " " + (it.extremeTail ?: ""))
        }
        assertTrue(finishing.isEmpty(), "finishing in a regular term: ${finishing.map { it.id }}")
    }
}
