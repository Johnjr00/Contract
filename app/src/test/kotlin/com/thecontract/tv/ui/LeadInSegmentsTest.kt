package com.thecontract.tv.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The lead-in split decides how long the television is silent after a term appears, and it is the
 * one part of the narration path that can be checked without a device: everything either side of
 * it is native code and an audio track.
 *
 * What matters is that no word is ever lost — the segments have to reassemble into the passage —
 * and that a cut is only taken where a speaker would pause anyway.
 */
class LeadInSegmentsTest {

    /** A representative instruction: one sentence, about the median length of the catalogue's. */
    private val instruction =
        "He gets between his legs and works him slowly, one hand flat on his stomach so he " +
            "cannot chase it, and does not speed up once."

    @Test
    fun `a long passage is split at the first comma past the minimum`() {
        val parts = leadInSegments(instruction)
        assertEquals(2, parts.size, "a passage this long should be opened with a short clause")
        assertTrue(parts[0].endsWith(","), "the cut belongs on a mark, not mid-clause: '${parts[0]}'")
        assertTrue(parts[0].length <= NarrationPlayer.LEAD_IN_CHARS)
        assertTrue(parts[0].length >= NarrationPlayer.MIN_LEAD_IN_CHARS)
    }

    @Test
    fun `nothing is added, lost or reordered`() {
        for (text in listOf(instruction, SHORT, NO_MARKS, TWO_SENTENCES, EARLY_COMMA)) {
            val rejoined = leadInSegments(text).joinToString(" ")
            assertEquals(
                text.trim().replace(Regex("\\s+"), " "),
                rejoined.replace(Regex("\\s+"), " "),
                "the segments must put the passage back together exactly"
            )
        }
    }

    @Test
    fun `a passage short enough to say at once is left whole`() {
        assertEquals(listOf(SHORT), leadInSegments(SHORT))
    }

    /** A seam mid-clause is worse than the wait it saves. */
    @Test
    fun `a long passage with no mark near the front is left whole`() {
        assertEquals(listOf(NO_MARKS), leadInSegments(NO_MARKS))
    }

    @Test
    fun `a full stop is preferred to a comma when both are in range`() {
        val parts = leadInSegments(TWO_SENTENCES)
        assertEquals("He stands.", parts[0])
    }

    /** Too early a comma leaves a fragment nobody can parse; the later mark is taken instead. */
    @Test
    fun `a mark before the minimum is not used`() {
        val parts = leadInSegments(EARLY_COMMA)
        assertTrue(
            parts[0].length >= NarrationPlayer.MIN_LEAD_IN_CHARS,
            "opened with a fragment: '${parts[0]}'"
        )
    }

    @Test
    fun `blank and whitespace passages do not throw`() {
        assertEquals(listOf(""), leadInSegments(""))
        assertEquals(listOf(""), leadInSegments("   "))
    }

    private companion object {
        const val SHORT = "He kneels and waits."
        const val NO_MARKS =
            "He kneels down in front of him and waits there without moving or speaking until " +
                "the clock above them has run all the way down to nothing at all"
        const val TWO_SENTENCES =
            "He stands. He kneels in front of him, hands behind his own back, for the whole of it."
        const val EARLY_COMMA =
            "Yes, he kneels in front of him with his hands behind his own back and waits there."
    }
}
