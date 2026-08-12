package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Where the two things reserved for the end of the night are allowed to appear.
 *
 * A man putting his cock in the other's arse, and a man coming, both belong to the closing terms —
 * the ones the finale always performs last. A regular term is negotiated in the middle of the
 * evening and performed somewhere in the running order, so neither can be written into one.
 *
 * Fingers, tongues and toys are not covered by this: the rule is about intercourse, not about
 * everything that goes near a hole.
 */
class FinaleOnlyPenetrationTest {

    @Test
    fun `no regular term has a man putting his cock in the other`() {
        val offenders = ContentLibrary.regularTerms.filter { term ->
            term.activities.any { it == "topping" || it == "bottoming" }
        }
        assertTrue(
            offenders.isEmpty(),
            "intercourse belongs to the closing terms: ${offenders.map { it.id }}"
        )
    }

    @Test
    fun `no regular term has anybody coming`() {
        // Orgasm as the end of the term, rather than a standing rule about asking first: a term
        // may well say "before he comes" or "he does not come" and be exactly right to.
        val comes = Regex(
            """\b(until he comes|until he finishes|lets him come|makes? him come|comes inside|""" +
                """comes in him|empties himself|unloads inside|gives him every drop)\b"""
        )
        val offenders = ContentLibrary.regularTerms.filter { term ->
            val text = term.base + " " + term.explicit + " " + (term.extremeTail ?: "")
            comes.containsMatchIn(text)
        }
        assertTrue(
            offenders.isEmpty(),
            "finishing belongs to the closing terms: ${offenders.map { it.id }}"
        )
    }

    /** The closing terms are still the place it does happen, or the night has no end. */
    @Test
    fun `the closing terms still carry it`() {
        val penetrative = ContentLibrary.climaxTerms.count { t ->
            t.activities.any { it == "topping" || it == "bottoming" }
        }
        assertTrue(penetrative >= 8, "only $penetrative closing terms involve intercourse")
    }
}
