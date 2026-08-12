package com.thecontract.tv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Pins the television layout to one design canvas, whatever the set reports.
 *
 * Televisions do not agree on density. A 1080p set is usually xhdpi, which works out at
 * 960 x 540 dp; a 4K set is usually xxxhdpi at the same 960 x 540 dp; but plenty of boxes and
 * sticks report something else entirely — tvdpi, or 4K pixels at xhdpi, or 720p at xhdpi — and
 * the same `48.dp` then means a different fraction of the screen on each of them. A layout tuned
 * against one of those densities has content running off the edge on another, which is exactly
 * the failure this exists to remove.
 *
 * So the reported density is ignored. This measures the window in *pixels* and derives a density
 * that makes those pixels come out as [DESIGN_WIDTH] x [DESIGN_HEIGHT] design units, letting the
 * screen keep whatever is left over on the long axis if it is not 16:9. The result is that
 * 1080p and 2160p produce an identical layout — same proportions, same relative type size, the
 * 4K one simply drawn with four times the pixels — and no set can be too small for the design,
 * because the design is measured in fractions of that set.
 *
 * The accessibility text scale is deliberately **not** carried through. On a canvas this tightly
 * fitted, a 1.3x text scale is the difference between a clock that fits and a clock cut off at
 * the bezel; the type here is already sized for a ten-foot viewing distance, which is the need
 * that setting exists to serve.
 */
@Composable
fun TvCanvas(content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val fallback = LocalDensity.current
        // Before the window has been measured there is nothing to scale against; the device
        // density is a safe stand-in for the frame or two that takes.
        val density = if (widthPx <= 0f || heightPx <= 0f) {
            fallback.density
        } else {
            min(widthPx / DESIGN_WIDTH, heightPx / DESIGN_HEIGHT)
        }
        CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 1f)) {
            Box(Modifier.fillMaxSize()) { content() }
        }
    }
}

/**
 * Draws [content] at its natural size, or shrinks it just enough to fit the height on offer.
 *
 * A television cannot scroll a passive column: nothing in it takes focus, so a remote has no way
 * to reach whatever falls below the fold, and anything that overflows is simply gone. The layout
 * is sized so the ordinary screens fit with room to spare, but term text is written by hand and
 * the longest instruction in the catalogue is three times the median — so "ordinarily fits" is not
 * the same as "fits", and the gap between those two is exactly where content used to disappear.
 *
 * Shrinking is done by lowering the density rather than by scaling the drawn result. The content
 * is laid out a second time against the smaller figure, so the text stays crisp and still fills
 * the full width of the column; scaling the finished layer instead would leave it soft and pull it
 * away from the right-hand edge. Because a lower density also makes the column wider in dp, lines
 * wrap less than they did, and the second pass usually comes out shorter than the arithmetic asks
 * for — which errs towards leaving a little room at the bottom rather than towards overflowing.
 *
 * [minScale] is the floor. Past roughly seven tenths the type stops being readable from a sofa,
 * and shrinking further would trade a visible problem for an illegible one.
 */
@Composable
fun FitToHeight(
    modifier: Modifier = Modifier,
    minScale: Float = 0.7f,
    content: @Composable () -> Unit
) {
    SubcomposeLayout(modifier) { constraints ->
        val available = constraints.maxHeight
        val loose = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)

        // `weight` hands this a fixed height, so what is reported back has to respect the
        // minimum as well as the maximum however little of it the content actually uses.
        fun report(used: Int) = constraints.constrainHeight(minOf(used, available))

        val natural = subcompose(FitPass.Natural, content).map { it.measure(loose) }
        val wanted = natural.maxOfOrNull { it.height } ?: 0

        // The common case: it fits, and only one composition ever happens.
        if (wanted <= available || available <= 0 || wanted <= 0) {
            return@SubcomposeLayout layout(constraints.maxWidth, report(wanted)) {
                natural.forEach { it.place(0, 0) }
            }
        }

        val scale = (available.toFloat() / wanted).coerceAtLeast(minScale)
        val shrunk = subcompose(FitPass.Shrunk) {
            CompositionLocalProvider(
                LocalDensity provides Density(density * scale, fontScale = 1f)
            ) { content() }
        }.map { it.measure(loose) }

        layout(constraints.maxWidth, report(shrunk.maxOfOrNull { it.height } ?: 0)) {
            shrunk.forEach { it.place(0, 0) }
        }
    }
}

private enum class FitPass { Natural, Shrunk }

/**
 * The canvas every size in this package is written against. Changing either number rescales the
 * whole app: a larger canvas means the same `48.dp` covers a smaller fraction of the screen, so
 * everything is drawn smaller.
 *
 * 1280 x 720 rather than the 960 x 540 this started at. That earlier figure is the dp size a
 * 1080p set reports at xhdpi, which made it the obvious choice, but writing the layout against it
 * left only 444 dp of usable height once the margins were taken off — and the header, the running
 * clock and the gaps between them came to 439 dp of that. The term being performed was left 4.7 dp
 * to draw itself in, and since nothing in that column can take focus, a remote could not scroll
 * down to what had been pushed off. Widening the canvas to 720 dp of height, which is the same
 * 16:9 shape, is what buys the room back; the type is unchanged, so on a 1080p set body text goes
 * from 44 px to 33 px, still well above what is readable across a room.
 */
const val DESIGN_WIDTH = 1280f
const val DESIGN_HEIGHT = 720f

/**
 * The fixed geometry of the television screen, in design units on that canvas.
 *
 * These are held here, next to the canvas they are measured against, because they only mean
 * anything in relation to it: the margin is a fraction of the screen, not a number of pixels, and
 * every previous version of this layout drifted by having one of them changed without the other.
 * Deriving the margin arithmetically is what stops that happening again.
 */
object TvLayout {
    /**
     * The overscan margin. A television may crop the outer five per cent of the picture, and five
     * per cent of 1280 is not five per cent of 720 — writing one figure for both, as this did,
     * over-pads the axis that has the least to give by 21 dp at each end.
     */
    val safeAreaX = (DESIGN_WIDTH * 0.05f).dp
    val safeAreaY = (DESIGN_HEIGHT * 0.05f).dp

    /** Between the game and the backup controls beside it. */
    val columnGap = 36.dp

    /**
     * The backup control column. Wide enough for the longest button label on two lines, and no
     * wider: what is left over goes to the game, which is the half of the screen anybody is
     * actually reading.
     */
    val remoteWidth = 380.dp
}
