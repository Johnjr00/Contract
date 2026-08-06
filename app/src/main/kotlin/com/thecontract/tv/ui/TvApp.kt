package com.thecontract.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thecontract.core.model.GamePhase
import com.thecontract.core.protocol.ClientView
import com.thecontract.core.protocol.ConsiderationCard
import com.thecontract.core.protocol.TermCard
import com.thecontract.core.protocol.TimerView

/**
 * The television surface.
 *
 * Everything here is drawn from the [ClientView] the server produced for the "tv" audience.
 * There is no path from this file to a private profile answer or to an unsubmitted selection,
 * because those values are simply never present in the object it is handed.
 */
@Composable
fun TvApp(
    view: ClientView,
    joinPort: Int,
    serverRunning: Boolean,
    onChoice: (String) -> Unit,
    onRemoteAction: (RemoteAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(TvColors.background)
            .padding(48.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Header(view, serverRunning, joinPort)

            view.reclaimRequest?.let { request ->
                ReclaimPrompt(request.playerName, request.requestId, onRemoteAction)
            }

            TvCard {
                BasicText(view.heading, style = TvType.display)
                if (view.body.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    BasicText(view.body, style = TvType.body)
                }
                if (view.waiting) {
                    Spacer(Modifier.height(12.dp))
                    BasicText("Waiting…", style = TvType.muted)
                }
            }

            view.blockedNotice?.let { notice ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(TvColors.panelRaised, RoundedCornerShape(12.dp))
                        .border(2.dp, TvColors.warn, RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    BasicText(notice, style = TvType.body)
                }
            }

            view.join?.let { join ->
                JoinPanel(join.url, join.qrMatrix, join.interfaceName, join.address, join.port)
            }

            view.term?.let { TermPanel(it, "Proposed term") }
            view.bundledTerm?.let { TermPanel(it, "Second term in the trade") }
            view.consideration?.let { ConsiderationPanel(it) }

            if (view.timers.isNotEmpty()) TimerPanel(view.timers)

            view.execution?.let { execution ->
                TvCard(raised = true) {
                    BasicText("Final scene", style = TvType.label)
                    BasicText(
                        "Term ${execution.stepIndex} of ${execution.stepCount}" +
                            if (execution.started) " · running" else " · not started",
                        style = TvType.heading
                    )
                    Spacer(Modifier.height(8.dp))
                    BasicText("Stop word: ${execution.stopWord}", style = TvType.muted)
                }
            }

            view.draft?.let { draft ->
                TvCard {
                    BasicText("Draft contract", style = TvType.title)
                    Spacer(Modifier.height(8.dp))
                    BasicText(
                        "${draft.signedRegular} of ${draft.requiredRegular} regular terms · " +
                            "${draft.signedClosing} of 2 closing terms · " +
                            "${draft.receiptsCompleted} consideration receipts",
                        style = TvType.muted
                    )
                    draft.byAct.forEach { (act, terms) ->
                        Spacer(Modifier.height(20.dp))
                        BasicText(act, style = TvType.label)
                        terms.forEach { term ->
                            Spacer(Modifier.height(10.dp))
                            BasicText("${term.index}. ${term.title}", style = TvType.heading)
                            BasicText(term.instruction, style = TvType.body)
                            term.bundledInstruction?.let {
                                BasicText("Traded with: $it", style = TvType.body)
                            }
                            term.considerationTitle?.let {
                                BasicText("Consideration performed: $it", style = TvType.muted)
                            }
                        }
                    }
                }
            }

            if (view.savedContracts.isNotEmpty() && view.phase == GamePhase.NO_SESSION) {
                TvCard {
                    BasicText("Saved contracts on this TV", style = TvType.title)
                    Spacer(Modifier.height(8.dp))
                    view.savedContracts.forEach {
                        BasicText("${it.title} — ${it.termCount} terms", style = TvType.muted)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }

        Spacer(Modifier.width(36.dp))

        // Remote-controlled column. Every backup control the specification asks for lives here.
        Column(
            modifier = Modifier
                .width(460.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BasicText("Remote", style = TvType.label)

            view.choices.filter { it.kind != "status" }.forEach { choice ->
                TvButton(
                    label = choice.label,
                    detail = choice.detail,
                    danger = choice.danger,
                    onClick = { onChoice(choice.id) }
                )
            }

            view.choices.filter { it.kind == "status" }.forEach { choice ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(TvColors.panel, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    BasicText(choice.label, style = TvType.muted)
                }
            }

            RemotePanel(view, onRemoteAction)
        }
    }
}

@Composable
private fun Header(view: ClientView, serverRunning: Boolean, joinPort: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicText("The Contract", style = TvType.title)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Pill(
                if (serverRunning) "Server on :$joinPort" else "Server offline",
                if (serverRunning) TvColors.ok else TvColors.stop
            )
            listOf("PLAYER_1", "PLAYER_2").forEach { slot ->
                val name = view.names[slot] ?: slot
                val state = view.connections[slot]
                Pill(
                    "$name: " + when (state) {
                        "CONNECTED" -> "connected"
                        "DISCONNECTED" -> "offline"
                        else -> "not joined"
                    },
                    when (state) {
                        "CONNECTED" -> TvColors.ok
                        "DISCONNECTED" -> TvColors.stop
                        else -> TvColors.muted
                    }
                )
            }
        }
    }
    view.progress?.let {
        Spacer(Modifier.height(8.dp))
        BasicText(
            "Act ${it.act} — ${it.actTitle} · ${it.signedRegular}/${it.requiredRegular} terms signed · " +
                "${it.signedClosing}/2 closing terms · ${it.receipts} consideration receipts",
            style = TvType.muted
        )
    }
}

@Composable
private fun Pill(text: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier
            .background(TvColors.panel, RoundedCornerShape(999.dp))
            .border(2.dp, color, RoundedCornerShape(999.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        BasicText(text, style = TvType.label.copy(color = color))
    }
}

@Composable
private fun JoinPanel(
    url: String,
    matrix: List<String>,
    interfaceName: String,
    address: String,
    port: Int
) {
    TvCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            QrCodeView(matrix)
            Spacer(Modifier.width(32.dp))
            Column {
                BasicText("Scan to join", style = TvType.title)
                Spacer(Modifier.height(12.dp))
                BasicText(url, style = TvType.body)
                Spacer(Modifier.height(16.dp))
                BasicText("Interface: $interfaceName", style = TvType.muted)
                BasicText("Address: $address:$port", style = TvType.muted)
                Spacer(Modifier.height(16.dp))
                BasicText(
                    "This is a plain local connection on your own network. It is not encrypted — " +
                        "the code itself is the key, so do not share it.",
                    style = TvType.muted
                )
            }
        }
    }
}

@Composable
private fun TermPanel(term: TermCard, label: String) {
    TvCard {
        BasicText(
            "$label · ${term.actTitle}" + if (term.climax) " · closing term" else "",
            style = TvType.label
        )
        Spacer(Modifier.height(8.dp))
        BasicText(term.title, style = TvType.title)
        Spacer(Modifier.height(12.dp))
        BasicText(term.instruction, style = TvType.body)
        if (term.equipment.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            BasicText("Uses: ${term.equipment.joinToString(", ")}", style = TvType.muted)
        }
        term.conditions.forEach {
            Spacer(Modifier.height(6.dp))
            BasicText("Condition: $it", style = TvType.muted)
        }
        term.amendments.forEach {
            Spacer(Modifier.height(6.dp))
            BasicText("Amendment: $it", style = TvType.muted)
        }
        if (term.timers.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            BasicText(
                "Timed: " + term.timers.joinToString(" · ") { "${it.label} ${it.totalSeconds}s" },
                style = TvType.muted
            )
        }
        Spacer(Modifier.height(14.dp))
        BasicText(term.benefitExplanation, style = TvType.body.copy(color = TvColors.accent))
    }
}

@Composable
private fun ConsiderationPanel(consideration: ConsiderationCard) {
    TvCard(raised = true) {
        BasicText("Consideration", style = TvType.label)
        Spacer(Modifier.height(8.dp))
        BasicText(consideration.title, style = TvType.title)
        Spacer(Modifier.height(12.dp))
        BasicText(consideration.instruction, style = TvType.body)
        Spacer(Modifier.height(12.dp))
        BasicText(
            if (consideration.mutual) {
                "Both of you perform this, and both of you confirm."
            } else {
                "${consideration.performerName} performs this for ${consideration.recipientName}."
            },
            style = TvType.muted
        )
    }
}

@Composable
private fun TimerPanel(timers: List<TimerView>) {
    TvCard {
        BasicText("Timers", style = TvType.label)
        timers.forEach { timer ->
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    BasicText(timer.label, style = TvType.heading)
                    BasicText(timer.state.lowercase(), style = TvType.label)
                }
                BasicText(
                    formatClock(timer.remainingMs),
                    style = TvType.clock.copy(
                        color = when (timer.state) {
                            "RUNNING" -> TvColors.ok
                            "COMPLETED" -> TvColors.muted
                            else -> TvColors.ink
                        }
                    )
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(TvColors.line, RoundedCornerShape(3.dp))
            ) {
                val fraction = if (timer.totalSeconds <= 0) {
                    0f
                } else {
                    (timer.remainingMs.toFloat() / (timer.totalSeconds * 1000f)).coerceIn(0f, 1f)
                }
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .background(TvColors.accent, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

private fun formatClock(remainingMs: Long): String {
    val total = (remainingMs / 1000).coerceAtLeast(0)
    val minutes = total / 60
    val seconds = total % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
