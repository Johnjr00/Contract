package com.thecontract.core.protocol

import com.thecontract.core.model.Amendment
import com.thecontract.core.model.Answer
import com.thecontract.core.model.FinaleOrder
import com.thecontract.core.model.ProposalResponse
import com.thecontract.core.model.SharedSetup
import com.thecontract.core.model.Slot
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Everything a phone can ask the server to do. Phones never mutate state themselves; they send
 * one of these and wait for the authoritative broadcast that follows.
 */
@Serializable
sealed interface GameAction

@Serializable @SerialName("submit_setup")
data class SubmitSetup(val setup: SharedSetup) : GameAction

/** "Read it again": asks the television to repeat whatever it last narrated. */
@Serializable @SerialName("read_again")
data object ReadAgain : GameAction

@Serializable @SerialName("update_setup")
data class UpdateSetup(val setup: SharedSetup) : GameAction

/**
 * Saves the setup screen as it currently stands under a name, for a later game.
 *
 * The setup travels with the action because the in-progress form lives on Player 1's phone and
 * does not reach the server until he submits it. Saving under a name already in the list
 * overwrites it; the phone asks him to confirm first.
 */
@Serializable @SerialName("save_setup_preset")
data class SaveSetupPreset(val name: String, val setup: SharedSetup) : GameAction

/** Replaces the whole setup with a saved one. Resolved against the store by the session manager. */
@Serializable @SerialName("load_setup_preset")
data class LoadSetupPreset(val id: String) : GameAction

@Serializable @SerialName("delete_setup_preset")
data class DeleteSetupPreset(val id: String) : GameAction

@Serializable @SerialName("save_profile")
data class SaveProfile(
    val answers: Map<String, Answer>,
    /** preference id -> MaybeCondition id, overriding the session default for that answer. */
    val conditions: Map<String, String> = emptyMap(),
    val complete: Boolean = false
) : GameAction

@Serializable @SerialName("proposal_response")
data class ProposalRespond(val response: ProposalResponse) : GameAction

@Serializable @SerialName("pick_amendment")
data class PickAmendment(val amendment: Amendment) : GameAction

@Serializable @SerialName("vote_amendment")
data class VoteAmendment(val amendment: Amendment) : GameAction

@Serializable @SerialName("approve_amended")
data class ApproveAmended(val approve: Boolean) : GameAction

@Serializable @SerialName("pick_bundle")
data class PickBundle(val termId: String) : GameAction

@Serializable @SerialName("vote_bundle")
data class VoteBundle(val approve: Boolean) : GameAction

@Serializable @SerialName("pick_consideration")
data class PickConsideration(val actionId: String) : GameAction

@Serializable @SerialName("consideration_performed")
data object ConsiderationPerformed : GameAction

@Serializable @SerialName("confirm_signature")
data class ConfirmSignature(val earned: Boolean) : GameAction

@Serializable @SerialName("pick_closing")
data class PickClosing(val termId: String) : GameAction

@Serializable @SerialName("vote_closing")
data class VoteClosing(val approve: Boolean) : GameAction

@Serializable @SerialName("pick_finale_order")
data class PickFinaleOrder(val order: FinaleOrder) : GameAction

@Serializable @SerialName("start_execution")
data object StartExecution : GameAction

/** Advance out of a confirmation-style state (term signed, final contract review). */
@Serializable @SerialName("continue")
data object ContinueFlow : GameAction

/** command: begin | complete | next | prev */
@Serializable @SerialName("exec")
data class ExecutionCommand(val command: String) : GameAction

/** command: start | pause | resume | reset */
@Serializable @SerialName("timer")
data class TimerCommand(val timerId: String, val command: String) : GameAction

@Serializable @SerialName("stop_all_timers")
data object StopAllTimers : GameAction

@Serializable @SerialName("global_pause")
data object GlobalPause : GameAction

@Serializable @SerialName("resume_vote")
data class ResumeVote(val confirm: Boolean) : GameAction

@Serializable @SerialName("end_session")
data object EndSession : GameAction

@Serializable @SerialName("back")
data object Back : GameAction

@Serializable @SerialName("open_draft")
data object OpenDraft : GameAction

@Serializable @SerialName("close_draft")
data object CloseDraft : GameAction

@Serializable @SerialName("save_contract")
data object SaveContract : GameAction

// ---------------------------------------------------------------------------- client → server

@Serializable
sealed interface ClientMessage

@Serializable @SerialName("HELLO")
data class Hello(val deviceId: String, val resumeToken: String? = null) : ClientMessage

@Serializable @SerialName("CLAIM_SLOT")
data class ClaimSlot(val deviceId: String) : ClientMessage

@Serializable @SerialName("RECONNECT")
data class Reconnect(val deviceId: String, val resumeToken: String, val slot: Slot) : ClientMessage

@Serializable @SerialName("PLAYER_ACTION")
data class PlayerActionMessage(
    val actionId: String,
    val expectedVersion: Long,
    val action: GameAction
) : ClientMessage

@Serializable @SerialName("PING")
data class Ping(val t: Long = 0) : ClientMessage

// ---------------------------------------------------------------------------- server → client

@Serializable
sealed interface ServerMessage

@Serializable @SerialName("HELLO_OK")
data class HelloOk(
    val sessionId: String,
    val slot: Slot,
    val resumeToken: String,
    val version: Long,
    val reclaimed: Boolean = false
) : ServerMessage

@Serializable @SerialName("SESSION_FULL")
data class SessionFull(val message: String) : ServerMessage

@Serializable @SerialName("RECLAIM_PENDING")
data class ReclaimPending(val requestId: String, val slot: Slot, val message: String) : ServerMessage

@Serializable @SerialName("STATE_SNAPSHOT")
data class StateSnapshot(val view: ClientView) : ServerMessage

@Serializable @SerialName("STATE_CHANGED")
data class StateChanged(val view: ClientView) : ServerMessage

@Serializable @SerialName("TIMER_UPDATE")
data class TimerUpdate(val version: Long, val timers: List<TimerView>) : ServerMessage

@Serializable @SerialName("ACTION_ACCEPTED")
data class ActionAccepted(val actionId: String, val version: Long) : ServerMessage

@Serializable @SerialName("ACTION_REJECTED")
data class ActionRejected(
    val actionId: String,
    val code: String,
    val message: String,
    val version: Long
) : ServerMessage

@Serializable @SerialName("GLOBAL_PAUSE")
data class GlobalPauseNotice(val paused: Boolean, val message: String) : ServerMessage

@Serializable @SerialName("ERROR")
data class ErrorMessage(val code: String, val message: String) : ServerMessage

@Serializable @SerialName("PONG")
data class Pong(val t: Long = 0) : ServerMessage

/** Shared JSON configuration: `type` discriminator, tolerant of unknown fields. */
val ProtocolJson: Json = Json {
    classDiscriminator = "type"
    encodeDefaults = true
    ignoreUnknownKeys = true
    explicitNulls = false
}
