package com.thecontract.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thecontract.core.model.GamePhase
import com.thecontract.core.protocol.ClientView
import com.thecontract.core.protocol.ConsiderationCard
import com.thecontract.core.protocol.TermCard
import com.thecontract.core.protocol.TimerView
import com.thecontract.tv.ServerHolder

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
    // What is on screen decides how much room the rest of it gets, so both facts are settled
    // once, here, rather than being asked again by each panel that cares.
    val sceneRunning = view.timers.any { it.state == "RUNNING" || it.state == "PAUSED" }
    val hasCard = view.term != null || view.consideration != null

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(TvColors.background)
            .padding(horizontal = TvLayout.safeAreaX, vertical = TvLayout.safeAreaY)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Header(view, serverRunning, joinPort)

            // Pinned above the scrolling area on purpose: while a timer runs it is the only
            // thing on this screen anyone is going to look at, so it must be readable at a
            // glance and must never be somewhere below the fold.
            ActiveTimerPanel(view.timers)

            FitToHeight(modifier = Modifier.weight(1f)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    view.reclaimRequest?.let { request ->
                        ReclaimPrompt(request.playerName, request.requestId, onRemoteAction)
                    }

                    // Ahead of the heading, which while pairing only repeats the same words. A join
                    // code nobody can see is the one thing that screen exists to show. Once play
                    // starts the term on offer is what has to be up here instead, so the code drops
                    // away.
                    if (view.phase in PAIRING_PHASES) {
                        view.join?.let { join ->
                            JoinPanel(join.url, join.qrMatrix, join.interfaceName, join.address, join.port)
                        }
                    }

                    PhaseHeading(view, hasCard, sceneRunning)

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

                    view.term?.let { TermPanel(it, termLabel(view), sceneRunning) }
                    view.bundledTerm?.let { TermPanel(it, "Second term in the trade", sceneRunning) }
                    view.consideration?.let { ConsiderationPanel(it, sceneRunning) }

                    // Only when the panel above is not already showing one. That panel draws the
                    // timer that is actually running; this draws every timer the term carries, and
                    // terms carry up to seven. Both at once meant the running clock appeared twice
                    // and the second copy pushed the term itself off the screen.
                    if (!sceneRunning && view.timers.isNotEmpty()) TimerPanel(view.timers)

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
                }
            }
        }

        Spacer(Modifier.width(TvLayout.columnGap))

        // Remote-controlled column. Every backup control the specification asks for lives here.
        // 380 dp, not the 460 dp it was: at the old canvas that left the game itself 368 dp — a
        // backup control panel wider than the term under negotiation — and every line in the
        // column beside it wrapped two or three times over because of it.
        Column(
            modifier = Modifier
                .width(TvLayout.remoteWidth)
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

/**
 * What step the game is on, at whatever prominence the rest of the screen can spare.
 *
 * On its own — pairing, waiting, the finished contract — this is the screen, and it is drawn as a
 * full card in the largest type there is. Beside a term card it is a caption: the term card
 * already carries the same words in its own label ("Proposed term · Act II"), so a second copy of
 * them at 40sp was costing 258 dp of a screen that had none to give. And once a timer is running
 * it goes entirely: the pair are performing, not reading, and what has to be legible then is the
 * instruction and the clock.
 *
 * The narration line survives all three, because it is the only place the one-time voice download
 * is ever reported, and a television that has gone quiet mid-scene with no explanation reads as a
 * fault.
 */
@Composable
private fun PhaseHeading(view: ClientView, hasCard: Boolean, sceneRunning: Boolean) {
    if (hasCard && sceneRunning) {
        NarrationLine(leadingGap = false)
        return
    }
    if (hasCard) {
        Column(Modifier.fillMaxWidth()) {
            BasicText(view.heading, style = TvType.title)
            if (view.body.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                BasicText(view.body, style = TvType.muted)
            }
            if (view.waiting) {
                Spacer(Modifier.height(6.dp))
                BasicText("Waiting…", style = TvType.muted)
            }
            NarrationLine()
        }
        return
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
        NarrationLine()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Header(view: ClientView, serverRunning: Boolean, joinPort: Int) {
    // One Column, not a loose run of siblings. The caller arranges its children with
    // `spacedBy(20.dp)`, so a header that emitted five children would collect that gap four
    // more times — which is exactly what used to push the running clock off the bottom.
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicText("The Contract", style = TvType.title)
            Pill(
                if (serverRunning) "Server on :$joinPort" else "Server offline",
                if (serverRunning) TvColors.ok else TvColors.stop
            )
        }
        Spacer(Modifier.height(10.dp))
        // Flowed, not a plain Row. A pill never wraps its own text, so two long names in a row
        // simply ran past the end of the column and the second one was cut in half by the panel
        // beside it. Given the room this wraps to a second line instead, which costs 8 dp on the
        // rare screen that needs it and never loses a word.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("PLAYER_1" to "Player 1", "PLAYER_2" to "Player 2").forEach { (slot, fallback) ->
                val name = view.names[slot] ?: fallback
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
        view.progress?.let {
            Spacer(Modifier.height(8.dp))
            BasicText(
                "Act ${it.act} — ${it.actTitle} · ${it.signedRegular}/${it.requiredRegular} signed · " +
                    "${it.signedClosing}/2 closing · ${it.receipts} receipts",
                style = TvType.muted
            )
        }
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
        BasicText(text, style = TvType.label.copy(color = color), softWrap = false, maxLines = 1)
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
        // The code is sized from the space actually available rather than fixed. A fixed 320dp
        // code exactly filled this card's interior, which left the details beside it zero width:
        // every word wrapped to one letter per line, the row grew to thousands of pixels, and the
        // code itself ended up centred far below the screen. The card looked simply empty.
        BoxWithConstraints {
            val codeSize = (maxWidth * 0.47f).coerceIn(140.dp, 260.dp)
            Column {
                BasicText("Scan to join", style = TvType.title)
                Spacer(Modifier.height(10.dp))
                Row {
                    QrCodeView(matrix, size = codeSize)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        BasicText("Interface", style = TvType.label)
                        BasicText(interfaceName, style = TvType.muted)
                        Spacer(Modifier.height(10.dp))
                        BasicText("Address", style = TvType.label)
                        BasicText("$address:$port", style = TvType.muted)
                    }
                }
                // Full width, below the code: the join token makes this URL far too long to sit
                // in a side column without wrapping to two or three characters a line.
                Spacer(Modifier.height(12.dp))
                BasicText(url, style = TvType.label)
                Spacer(Modifier.height(8.dp))
                BasicText(
                    "Plain local connection, not encrypted. The code is the key — do not share it.",
                    style = TvType.label
                )
            }
        }
    }
}

/**
 * Labelled suggestion lists for an instruction that tells a man to do something without telling
 * him what — things he could say, positions he could use. Headed as suggestions, because a list
 * sitting under an instruction otherwise reads as more of the instruction.
 */
@Composable
private fun SuggestionLists(lists: List<com.thecontract.core.model.Suggestions>) {
    lists.forEach { group ->
        if (group.items.isEmpty()) return@forEach
        Spacer(Modifier.height(12.dp))
        BasicText(group.heading, style = TvType.label)
        group.items.forEach {
            Spacer(Modifier.height(4.dp))
            BasicText("• $it", style = TvType.muted)
        }
    }
}

/**
 * A one-line note on what the narrator is doing.
 *
 * Speech on this hardware is generated at roughly the speed it is spoken, so there is a gap
 * between a term appearing and the first word of it. Saying so is better than silence that
 * looks like a fault. The measured factor is shown because it is the number that decides
 * whether this box can carry a heavier voice, and it can only be measured here.
 *
 * The download line matters more than it looks. It is the only place the one-time fetch of the
 * voice is visible, and it appears on the pairing screen, which is where a television that has
 * just been set up will be sitting while it runs — a progress figure there is the difference
 * between a wait somebody understands and a game that seems to have gone quiet for no reason.
 */
@Composable
private fun NarrationLine(leadingGap: Boolean = true) {
    val status by ServerHolder.narration.collectAsState()
    val download = status.download
    val text = when {
        download != null ->
            "Downloading the voice · ${download.percent}% of ${download.bytesTotal / 1_000_000} MB · " +
                "once only, then it works offline"
        // The installer's own words when it has them: being unable to reach the network is a
        // thing a player can fix, and "unavailable" would hide that.
        status.failed -> status.failureNote ?: "Narration unavailable on this device."
        status.speaking -> "Reading aloud…" + rtfSuffix(status.realTimeFactor)
        status.realTimeFactor != null -> "Narration ready" + rtfSuffix(status.realTimeFactor)
        else -> return
    }
    if (leadingGap) Spacer(Modifier.height(12.dp))
    BasicText(text, style = TvType.muted)
}

private fun rtfSuffix(rtf: Double?): String =
    rtf?.let { " · ${(it * 100).toInt() / 100.0}x real time" } ?: ""

/**
 * The term itself.
 *
 * While a timer runs this is cut back to the label, the title and the instruction. Everything
 * that follows — what it needs, how long it takes, whose turn it is to gain — is what the pair
 * read while they were deciding whether to sign it. Once the clock is going that decision is
 * behind them and the instruction is the only line still being acted on, so it is the one that
 * has to stay at full size rather than be shrunk to fit around the rest.
 */
@Composable
private fun TermPanel(term: TermCard, label: String, sceneRunning: Boolean = false) {
    TvCard {
        BasicText(
            "$label · ${term.actTitle}" + if (term.climax) " · closing term" else "",
            style = TvType.label
        )
        Spacer(Modifier.height(8.dp))
        BasicText(term.title, style = TvType.title)
        // The final scene sends these empty, because the screen above already says them.
        if (term.instruction.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            BasicText(term.instruction, style = TvType.body)
        }
        if (sceneRunning) return@TvCard
        SuggestionLists(term.suggestions)
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
            BasicText(timingLine(term.timers), style = TvType.muted)
        }
        if (term.benefitExplanation.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            BasicText(term.benefitExplanation, style = TvType.body.copy(color = TvColors.accent))
        }
    }
}

@Composable
private fun ConsiderationPanel(consideration: ConsiderationCard, sceneRunning: Boolean = false) {
    TvCard(raised = true) {
        BasicText("Consideration", style = TvType.label)
        Spacer(Modifier.height(8.dp))
        BasicText(consideration.title, style = TvType.title)
        Spacer(Modifier.height(12.dp))
        BasicText(consideration.instruction, style = TvType.body)
        if (sceneRunning) return@TvCard
        SuggestionLists(consideration.suggestions)
        if (consideration.timers.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            BasicText(timingLine(consideration.timers), style = TvType.muted)
        }
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

/**
 * The timer that is actually running, big enough to read from across a dark room.
 *
 * Only one is ever shown. Terms can carry several segments, but only one runs at a time, and a
 * wall of countdowns is exactly the thing nobody can parse mid-scene. A paused timer still shows,
 * because a paused clock that vanished would read as a bug. Nothing is drawn when every timer is
 * idle or finished, so this costs no space during negotiation.
 */
@Composable
private fun ActiveTimerPanel(timers: List<TimerView>) {
    val active = timers.firstOrNull { it.state == "RUNNING" }
        ?: timers.firstOrNull { it.state == "PAUSED" }
        ?: return

    val running = active.state == "RUNNING"
    val status = buildString {
        append(if (running) "Running now" else "Paused")
        if (timers.size > 1) {
            append(" · segment ${timers.count { it.state == "COMPLETED" } + 1} of ${timers.size}")
        }
    }
    TvCard(raised = true) {
        BasicText(status, style = TvType.label)
        Spacer(Modifier.height(6.dp))
        BasicText(active.label, style = TvType.title)
        BasicText(
            formatClock(active.remainingMs),
            style = TvType.hero.copy(color = if (running) TvColors.ok else TvColors.muted)
        )
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(TvColors.line, RoundedCornerShape(7.dp))
        ) {
            val fraction = if (active.totalSeconds <= 0) {
                0f
            } else {
                (active.remainingMs.toFloat() / (active.totalSeconds * 1000f)).coerceIn(0f, 1f)
            }
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(if (running) TvColors.ok else TvColors.muted, RoundedCornerShape(7.dp))
            )
        }
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

/**
 * What the term card is called depends on where the game is. It read "Proposed term" on every
 * screen that carried one, including the screen that had just announced it signed and every
 * screen of the final scene, where nothing is being proposed to anybody.
 */
private fun termLabel(view: ClientView): String = when {
    // A trade is performed as two steps. Saying so is what keeps the second from reading as a
    // term nobody remembers agreeing to.
    view.execution?.partOfTrade == true -> "Term in the scene · one half of a trade"
    view.execution != null -> "Term in the scene"
    view.phase == GamePhase.TERM_SIGNED -> "Signed term"
    view.phase in EARNING_PHASES -> "The term being earned"
    else -> "Proposed term"
}

/** The steps where a consideration is being chosen, performed or judged. */
private val EARNING_PHASES = setOf(
    GamePhase.CONSIDERATION_PRIVATE_SELECTION,
    GamePhase.CLOSING_TERM_CONSIDERATION,
    GamePhase.CONSIDERATION_PUBLIC_EXECUTION,
    GamePhase.WAITING_FOR_SIGNATURE_CONFIRMATION
)

/**
 * "Timed: Left ear 0:45 · Right ear 0:45 · 1:30 in total". Shown wherever a term or a
 * consideration is offered, because choosing between two actions without knowing whether one
 * runs for forty-five seconds or four minutes is choosing blind.
 */
private fun timingLine(timers: List<TimerView>): String {
    val parts = timers.joinToString(" · ") { "${it.label} ${formatClock(it.totalSeconds * 1000L)}" }
    val total = timers.sumOf { it.totalSeconds }
    return "Timed: " + parts + if (timers.size > 1) " · ${formatClock(total * 1000L)} in total" else ""
}

/** The screens whose whole purpose is getting a phone connected. */
private val PAIRING_PHASES = setOf(
    GamePhase.PAIRING,
    GamePhase.PLAYER_1_SETUP,
    GamePhase.WAITING_FOR_PLAYER_2
)
