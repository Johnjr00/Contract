package com.thecontract.tv.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws the QR matrix produced offline by the core module.
 *
 * The matrix arrives as rows of `0`/`1`, so there is no image decoding, no bitmap allocation
 * beyond the canvas itself, and nothing is fetched from anywhere.
 */
@Composable
fun QrCodeView(matrix: List<String>, size: Dp = 320.dp, modifier: Modifier = Modifier) {
    if (matrix.isEmpty()) return
    Box(
        modifier = modifier
            .size(size)
            // A quiet zone in the light colour is required for reliable scanning.
            .background(Color.White)
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.size(size - 24.dp)) {
            val rows = matrix.size
            val columns = matrix.first().length
            if (rows == 0 || columns == 0) return@Canvas
            val cellWidth = this.size.width / columns
            val cellHeight = this.size.height / rows
            for (y in 0 until rows) {
                val row = matrix[y]
                // Bounded by this row rather than by the first one. A ragged matrix is not
                // supposed to happen, but reading past the end of a string here throws on the
                // main thread during composition, where nothing can catch it.
                for (x in 0 until minOf(columns, row.length)) {
                    if (row[x] == '1') {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(x * cellWidth, y * cellHeight),
                            // A hair of overdraw stops seams appearing between cells.
                            size = Size(cellWidth + 0.6f, cellHeight + 0.6f)
                        )
                    }
                }
            }
        }
    }
}
