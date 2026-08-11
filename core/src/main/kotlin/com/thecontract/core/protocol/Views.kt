package com.thecontract.core.protocol

import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.Slot
import kotlinx.serialization.Serializable

/**
 * Server-driven views.
 *
 * The server decides what each audience is allowed to see and sends a finished description of
 * the screen. The phone and TV clients render what they are given and never infer private state,
 * which is what makes the privacy guarantees enforceable rather than merely conventional: a
 * player's profile answers and private selections are simply never serialised into anyone
 * else's view.
 */

@Serializable
data class TimerView(
    val id: String,
    val label: String,
    val parentInstruction: String,
    val totalSeconds: Int,
    val remainingMs: Long,
    val state: String
)

@Serializable
data class TimerControl(
    val mayControl: Boolean,
    val mayComplete: Boolean,
    val controllerName: String?,
    val showStopAll: Boolean
)

/** A button or option offered to this audience. */
@Serializable
data class Choice(
    val id: String,
    val label: String,
    val detail: String? = null,
    /** action | option | nav */
    val kind: String = "action",
    val danger: Boolean = false
)

@Serializable
data class TermCard(
    val termId: String,
    val title: String,
    val instruction: String,
    val level: Int,
    val actTitle: String,
    val giverName: String,
    val receiverName: String,
    val beneficiaryName: String?,
    val benefitExplanation: String,
    val equipment: List<String> = emptyList(),
    val conditions: List<String> = emptyList(),
    val amendments: List<String> = emptyList(),
    val timers: List<TimerView> = emptyList(),
    /** Suggested lines, shown under the instruction as suggestions rather than orders. */
    val examples: List<String> = emptyList(),
    val climax: Boolean = false,
    val mutual: Boolean = false
)

@Serializable
data class ConsiderationCard(
    val actionId: String,
    val title: String,
    val instruction: String,
    val performerName: String,
    val recipientName: String,
    val mutual: Boolean,
    val equipment: List<String> = emptyList(),
    val timers: List<TimerView> = emptyList(),
    /** Suggested lines, shown under the instruction as suggestions rather than orders. */
    val examples: List<String> = emptyList()
)

@Serializable
data class ProfileItem(
    val id: String,
    val label: String,
    val answer: String,
    val condition: String? = null
)

@Serializable
data class ProfileSection(val id: String, val title: String, val items: List<ProfileItem>)

@Serializable
data class ProfileForm(
    val sections: List<ProfileSection>,
    val complete: Boolean,
    val defaultConditionId: String,
    val conditionOptions: List<Choice>
)

@Serializable
data class SetupOption(val id: String, val label: String, val detail: String? = null)

@Serializable
data class SetupForm(
    val player1Name: String,
    val player2Name: String,
    val dominantSlot: String,
    val analRoles: Map<String, String>,
    val erectionDifficulty: Map<String, Boolean>,
    val sessionLength: String,
    val explicitness: String,
    val finaleFormat: String,
    val stopWord: String,
    val defaultMaybeCondition: String,
    val boundaries: List<String>,
    val equipment: List<String>,
    val options: Map<String, List<SetupOption>>,
    val errors: List<String> = emptyList()
)

@Serializable
data class SignedTermCard(
    val index: Int,
    val actTitle: String,
    val title: String,
    val instruction: String,
    val bundledTitle: String? = null,
    val bundledInstruction: String? = null,
    val conditions: List<String> = emptyList(),
    val amendments: List<String> = emptyList(),
    val beneficiaryName: String?,
    val considerationTitle: String?,
    val considerationInstruction: String?,
    val signedBy: List<String>,
    val closingForName: String? = null,
    val timers: List<TimerView> = emptyList(),
    val examples: List<String> = emptyList()
)

@Serializable
data class DraftView(
    val signedRegular: Int,
    val requiredRegular: Int,
    val signedClosing: Int,
    val receiptsCompleted: Int,
    val byAct: Map<String, List<SignedTermCard>>
)

@Serializable
data class ExecutionView(
    val stepIndex: Int,
    val stepCount: Int,
    val started: Boolean,
    val canPrevious: Boolean,
    val canNext: Boolean,
    val term: TermCard?,
    val stopWord: String
)

@Serializable
data class ProgressView(
    val signedRegular: Int,
    val requiredRegular: Int,
    val signedClosing: Int,
    val receipts: Int,
    val act: Int,
    val actTitle: String
)

@Serializable
data class JoinInfo(
    val url: String,
    val qrMatrix: List<String>,
    val interfaceName: String,
    val address: String,
    val port: Int,
    val availableInterfaces: List<Choice> = emptyList()
)

/** A pending slot takeover awaiting confirmation from the TV remote (section 8). */
@Serializable
data class ReclaimRequestView(
    val requestId: String,
    val slotName: String,
    val playerName: String
)

@Serializable
data class SavedContractSummary(
    val id: String,
    val title: String,
    val completedAtMs: Long,
    val termCount: Int
)

@Serializable
data class ClientView(
    /** "phone" or "tv" */
    val audience: String,
    val sessionId: String,
    val version: Long,
    val phase: GamePhase,
    val slot: Slot? = null,
    val names: Map<String, String> = emptyMap(),
    val roles: Map<String, String> = emptyMap(),
    val connections: Map<String, String> = emptyMap(),
    val paused: Boolean = false,
    val pauseMessage: String? = null,
    val heading: String = "",
    val body: String = "",
    val waiting: Boolean = false,
    val choices: List<Choice> = emptyList(),
    val term: TermCard? = null,
    val bundledTerm: TermCard? = null,
    val consideration: ConsiderationCard? = null,
    val considerationOptions: List<ConsiderationCard> = emptyList(),
    val termOptions: List<TermCard> = emptyList(),
    val timers: List<TimerView> = emptyList(),
    val timerControl: TimerControl? = null,
    val profile: ProfileForm? = null,
    val setup: SetupForm? = null,
    val draft: DraftView? = null,
    val execution: ExecutionView? = null,
    val progress: ProgressView? = null,
    val canGoBack: Boolean = false,
    val canOpenDraft: Boolean = false,
    val join: JoinInfo? = null,
    val blockedNotice: String? = null,
    val savedContracts: List<SavedContractSummary> = emptyList(),
    val stopWord: String? = null,
    /** TV only. A phone is asking to take over an occupied slot and needs remote confirmation. */
    val reclaimRequest: ReclaimRequestView? = null
)
