package com.thecontract.tv.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the fixed geometry of the television screen.
 *
 * The layout has run off the edge of a real set twice, both times because one number moved and
 * another that depends on it did not. None of this can be checked by rendering — there is no
 * television in a unit test — but the parts that are pure arithmetic are the parts that drifted,
 * and those can be pinned here.
 */
class TvLayoutBudgetTest {

    /**
     * `TvCanvas` derives its density from `min(width/DESIGN_WIDTH, height/DESIGN_HEIGHT)`, so a
     * canvas that is not 16:9 would letterbox itself on the ordinary 16:9 set and quietly give
     * back some of the screen the layout is counting on.
     */
    @Test
    fun `the design canvas is sixteen by nine`() {
        assertEquals(16f / 9f, DESIGN_WIDTH / DESIGN_HEIGHT, 0.0001f)
    }

    /** Five per cent of each axis is the margin a set may crop, and the two are not equal. */
    @Test
    fun `the safe area is five per cent of each axis`() {
        assertEquals(DESIGN_WIDTH * 0.05f, TvLayout.safeAreaX.value, 0.01f)
        assertEquals(DESIGN_HEIGHT * 0.05f, TvLayout.safeAreaY.value, 0.01f)
        assertTrue(
            TvLayout.safeAreaX.value > TvLayout.safeAreaY.value,
            "a 16:9 canvas crops more across than down; one figure for both over-pads the height"
        )
    }

    /**
     * The backup controls sit beside the game, not in front of it. At the canvas this replaced,
     * a 460 dp control column against 864 dp of usable width left the term under negotiation
     * 368 dp — narrower than the controls — and every line in it wrapped two or three times.
     */
    @Test
    fun `the game gets the larger share of the width`() {
        val usable = DESIGN_WIDTH - TvLayout.safeAreaX.value * 2
        val game = usable - TvLayout.columnGap.value - TvLayout.remoteWidth.value

        assertTrue(game > 0f, "the remote column and the margins do not leave room for the game")
        assertTrue(
            game >= TvLayout.remoteWidth.value * 1.5f,
            "the game column is $game dp against ${TvLayout.remoteWidth.value} dp of controls; " +
                "it should have at least half again as much"
        )
    }

    /**
     * The height that is left for the term after the fixed furniture above it. The header and the
     * running-clock panel are roughly 116 dp and 220 dp measured against this canvas; what is left
     * has to be enough for a term card, which is about 250 dp for an instruction of the length the
     * catalogue actually holds. This is the check that was failing on a real television: the same
     * arithmetic on the old canvas left 4.7 dp.
     */
    @Test
    fun `a running scene leaves room for the term being performed`() {
        val usable = DESIGN_HEIGHT - TvLayout.safeAreaY.value * 2
        val furniture = HEADER_HEIGHT + RUNNING_CLOCK_HEIGHT + STACK_GAP * 2

        assertTrue(
            usable - furniture >= CONDENSED_TERM_HEIGHT,
            "only ${usable - furniture} dp is left for the term, which needs about " +
                "$CONDENSED_TERM_HEIGHT dp"
        )
    }

    private companion object {
        /** Measured against this canvas: title row, the two player pills, the progress line. */
        const val HEADER_HEIGHT = 116f

        /** The raised card carrying the 74sp clock and its progress bar. */
        const val RUNNING_CLOCK_HEIGHT = 220f

        /** Label, title and a p90-length instruction, with the card's own padding. */
        const val CONDENSED_TERM_HEIGHT = 250f

        /** `Arrangement.spacedBy` between the header, the clock and the content beneath them. */
        const val STACK_GAP = 20f
    }
}
