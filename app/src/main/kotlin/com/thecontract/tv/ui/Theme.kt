package com.thecontract.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Television palette. High contrast, dark, safe on OLED, readable from a sofa. */
object TvColors {
    val background = Color(0xFF12100F)
    val panel = Color(0xFF1C1917)
    val panelRaised = Color(0xFF272220)
    val line = Color(0xFF3A322E)
    val ink = Color(0xFFF4EFE9)
    val muted = Color(0xFFB6A99E)
    val accent = Color(0xFFC8825A)
    val accentInk = Color(0xFF1A1210)
    val ok = Color(0xFF6FAE7A)
    val warn = Color(0xFFD9A441)
    val stop = Color(0xFFC4553F)
}

/** Type scale sized for a 10-foot viewing distance. */
object TvType {
    val display = TextStyle(color = TvColors.ink, fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
    val title = TextStyle(color = TvColors.ink, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
    val heading = TextStyle(color = TvColors.ink, fontSize = 24.sp, fontWeight = FontWeight.Medium)
    val body = TextStyle(color = TvColors.ink, fontSize = 22.sp, lineHeight = 32.sp)
    val muted = TextStyle(color = TvColors.muted, fontSize = 19.sp, lineHeight = 28.sp)
    val label = TextStyle(color = TvColors.muted, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    val clock = TextStyle(color = TvColors.ink, fontSize = 44.sp, fontWeight = FontWeight.Bold)

    /** The running-timer readout. Sized to be legible from a sofa, not from a desk. */
    val hero = TextStyle(color = TvColors.ink, fontSize = 74.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun TvCard(
    modifier: Modifier = Modifier,
    raised: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (raised) TvColors.panelRaised else TvColors.panel, RoundedCornerShape(14.dp))
            .border(1.dp, TvColors.line, RoundedCornerShape(14.dp))
            .padding(24.dp)
    ) {
        content()
    }
}

/**
 * A remote-navigable button.
 *
 * `clickable` makes the row focusable and maps the D-pad centre and Enter to a click, which is
 * exactly what an Android TV remote sends. Focus is drawn explicitly because a television user
 * has no cursor to tell them where they are.
 */
@Composable
fun TvButton(
    label: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    danger: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val border = when {
        focused -> TvColors.accent
        danger -> TvColors.stop
        else -> TvColors.line
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (focused) TvColors.accent else TvColors.panelRaised,
                RoundedCornerShape(12.dp)
            )
            .border(3.dp, border, RoundedCornerShape(12.dp))
            .onFocusChanged { focused = it.isFocused }
            .clickable(enabled = enabled) { onClick() }
            .padding(PaddingValues(horizontal = 24.dp, vertical = 18.dp))
    ) {
        BasicText(
            text = label,
            style = TvType.heading.copy(
                color = when {
                    focused -> TvColors.accentInk
                    danger -> TvColors.stop
                    else -> TvColors.ink
                }
            )
        )
        if (detail != null) {
            BasicText(
                text = detail,
                style = TvType.label.copy(color = if (focused) TvColors.accentInk else TvColors.muted)
            )
        }
    }
}
