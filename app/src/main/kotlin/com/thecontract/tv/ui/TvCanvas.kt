package com.thecontract.tv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
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
 * The canvas every size in this package is written against: the dp size of a 1080p television at
 * the xhdpi density such a set normally reports. Changing either number rescales the whole app.
 */
const val DESIGN_WIDTH = 960f
const val DESIGN_HEIGHT = 540f
