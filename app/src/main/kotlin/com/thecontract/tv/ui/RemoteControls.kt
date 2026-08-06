package com.thecontract.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.Slot
import com.thecontract.core.protocol.ClientView

/** Everything the Android TV remote can do as backup (section 41). */
sealed interface RemoteAction {
    data object Pause : RemoteAction
    data object Resume : RemoteAction
    data object StopAllTimers : RemoteAction
    data class Timer(val timerId: String, val command: String) : RemoteAction
    data class Exec(val command: String) : RemoteAction
    data object OpenDraft : RemoteAction
    data object CloseDraft : RemoteAction
    data object SaveContract : RemoteAction
    data object NewGame : RemoteAction
    data object ResumeSession : RemoteAction
    data object AbandonSession : RemoteAction
    data object EndSession : RemoteAction
    data object RestartPairing : RemoteAction
    data class ReleaseSlot(val slot: Slot) : RemoteAction
    data class ConfirmReclaim(val requestId: String, val approve: Boolean) : RemoteAction
    data class SelectInterface(val id: String) : RemoteAction
    /** Temporarily acts for a player whose phone has dropped out of a public step. */
    data class SignFor(val slot: Slot) : RemoteAction
    data class RejectFor(val slot: Slot) : RemoteAction
}

/**
 * The remote-controlled backup panel.
 *
 * Nothing here ever reveals a private profile, a private vote, a private counteroffer choice or
 * a private finale selection — those actions are not offered at all. Acting on a player's behalf
 * is limited to the public steps and always needs an explicit confirmation first.
 */
@Composable
fun RemotePanel(view: ClientView, onAction: (RemoteAction) -> Unit) {
    var confirming by remember { mutableStateOf<RemoteAction?>(null) }
    var showAdvanced by remember { mutableStateOf(false) }

    fun confirm(action: RemoteAction) {
        confirming = action
    }

    Spacer(Modifier.height(12.dp))
    BasicText("Backup controls", style = TvType.label)

    confirming?.let { pending ->
        TvCard(raised = true) {
            BasicText("Confirm this from the remote?", style = TvType.heading)
            Spacer(Modifier.height(8.dp))
            BasicText(describe(pending, view), style = TvType.muted)
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TvButton("Yes, do it") {
                    onAction(pending)
                    confirming = null
                }
                TvButton("Cancel", danger = true) { confirming = null }
            }
        }
        Spacer(Modifier.height(12.dp))
    }

    if (view.paused) {
        TvButton("Resume from the remote", detail = "Use this when a phone is offline") {
            confirm(RemoteAction.Resume)
        }
    } else if (view.phase != GamePhase.NO_SESSION) {
        TvButton("Pause everything now", danger = true) { onAction(RemoteAction.Pause) }
    }

    if (view.phase == GamePhase.NO_SESSION) {
        TvButton("Start a new game") { onAction(RemoteAction.NewGame) }
        return
    }

    if (view.timers.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        BasicText("Timers", style = TvType.label)
        view.timers.forEach { timer ->
            TvButton(
                label = if (timer.state == "RUNNING") "Pause: ${timer.label}" else "Start: ${timer.label}",
                detail = timer.state.lowercase()
            ) {
                onAction(
                    RemoteAction.Timer(timer.id, if (timer.state == "RUNNING") "pause" else "start")
                )
            }
            TvButton("Reset: ${timer.label}") { onAction(RemoteAction.Timer(timer.id, "reset")) }
        }
        if (view.timers.size > 1) {
            TvButton("Stop all timers", danger = true) { onAction(RemoteAction.StopAllTimers) }
        }
    }

    if (view.phase == GamePhase.FINAL_EXECUTION) {
        Spacer(Modifier.height(10.dp))
        BasicText("Final execution", style = TvType.label)
        val execution = view.execution
        if (execution?.started != true) TvButton("Begin this term") { onAction(RemoteAction.Exec("begin")) }
        TvButton("Mark complete") { onAction(RemoteAction.Exec("complete")) }
        TvButton("Next term") { onAction(RemoteAction.Exec("next")) }
        if (execution?.canPrevious == true) {
            TvButton("Previous term") { onAction(RemoteAction.Exec("prev")) }
        }
    }

    Spacer(Modifier.height(10.dp))
    if (view.draft != null) {
        TvButton("Close the draft") { onAction(RemoteAction.CloseDraft) }
    } else {
        TvButton("Review the draft") { onAction(RemoteAction.OpenDraft) }
    }

    // Acting for a player whose phone has dropped, during a public step only.
    val offline = Slot.entries.filter { view.connections[it.name] == "DISCONNECTED" }
    if (offline.isNotEmpty() && view.phase in PUBLIC_STEPS) {
        Spacer(Modifier.height(10.dp))
        BasicText("A phone is offline", style = TvType.label)
        offline.forEach { slot ->
            val name = view.names[slot.name] ?: slot.name
            TvButton("Sign for $name", detail = "Only for this public step") {
                confirm(RemoteAction.SignFor(slot))
            }
            TvButton("Reject for $name", danger = true) { confirm(RemoteAction.RejectFor(slot)) }
            TvButton("Release ${name}'s slot", danger = true) { confirm(RemoteAction.ReleaseSlot(slot)) }
        }
    }

    Spacer(Modifier.height(10.dp))
    TvButton(if (showAdvanced) "Hide setup controls" else "Network and pairing") {
        showAdvanced = !showAdvanced
    }

    if (showAdvanced) {
        view.join?.availableInterfaces?.forEach { option ->
            TvButton("Use ${option.label}") { confirm(RemoteAction.SelectInterface(option.id)) }
        }
        TvButton("Restart pairing", detail = "Keeps the contract, mints a new code") {
            confirm(RemoteAction.RestartPairing)
        }
        if (view.phase == GamePhase.COMPLETED) {
            TvButton("Save this contract") { onAction(RemoteAction.SaveContract) }
        }
        TvButton("End this session", danger = true) { confirm(RemoteAction.EndSession) }
        TvButton("Abandon and delete this session", danger = true) {
            confirm(RemoteAction.AbandonSession)
        }
    }
}

private val PUBLIC_STEPS = setOf(
    GamePhase.PROPOSAL,
    GamePhase.WAITING_FOR_PROPOSAL_RESPONSES,
    GamePhase.COUNTEROFFER_APPROVAL,
    GamePhase.BUNDLE_APPROVAL,
    GamePhase.CLOSING_TERM_SELECTION
)

private fun describe(action: RemoteAction, view: ClientView): String = when (action) {
    is RemoteAction.Resume -> "Resume the session without waiting for both phones to confirm."
    is RemoteAction.SignFor -> "Sign the current proposal on behalf of ${view.names[action.slot.name]}."
    is RemoteAction.RejectFor -> "Reject the current proposal on behalf of ${view.names[action.slot.name]}."
    is RemoteAction.ReleaseSlot ->
        "Free ${view.names[action.slot.name]}'s player slot so a different phone can take it. " +
            "The contract is kept."
    is RemoteAction.ConfirmReclaim ->
        if (action.approve) "Let this new phone take over the slot." else "Refuse the takeover."
    is RemoteAction.SelectInterface -> "Advertise the join code on this network interface instead."
    is RemoteAction.RestartPairing ->
        "Disconnect both phones and show a new code. Everything negotiated so far is kept."
    is RemoteAction.EndSession -> "End the session here."
    is RemoteAction.AbandonSession -> "Delete this unfinished session from the TV."
    else -> "Carry out this control."
}

/** Shown separately, because a takeover request must be answered before anything else. */
@Composable
fun ReclaimPrompt(slotName: String, requestId: String, onAction: (RemoteAction) -> Unit) {
    TvCard(raised = true) {
        BasicText("A phone wants to take over $slotName", style = TvType.title)
        Spacer(Modifier.height(10.dp))
        BasicText(
            "Another browser scanned the code and is asking for a slot that already belongs to " +
                "someone. Only allow this if it is the same person on a different phone.",
            style = TvType.body
        )
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TvButton("Allow the takeover") { onAction(RemoteAction.ConfirmReclaim(requestId, true)) }
            TvButton("Refuse", danger = true) { onAction(RemoteAction.ConfirmReclaim(requestId, false)) }
        }
    }
}
