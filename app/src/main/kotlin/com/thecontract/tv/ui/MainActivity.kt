package com.thecontract.tv.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.Slot
import com.thecontract.core.protocol.ContinueFlow
import com.thecontract.core.protocol.EndSession
import com.thecontract.core.protocol.ExecutionCommand
import com.thecontract.core.protocol.GameAction
import com.thecontract.core.protocol.GlobalPause
import com.thecontract.core.protocol.OpenDraft
import com.thecontract.core.protocol.ProposalRespond
import com.thecontract.core.protocol.ResumeVote
import com.thecontract.core.protocol.SaveContract
import com.thecontract.core.protocol.StopAllTimers
import com.thecontract.core.protocol.TimerCommand
import com.thecontract.tv.ServerHolder
import com.thecontract.tv.service.ContractService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The television activity.
 *
 * It owns no state. The foreground service owns the server and publishes a finished view; this
 * activity renders it and posts remote actions back. That is what makes activity recreation
 * free: rotating, backgrounding or being rebuilt by the system costs nothing, because there was
 * never anything here to lose.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        ContractService.start(this)

        setContent {
            val view by ServerHolder.tvView.collectAsState()
            Box(Modifier.fillMaxSize()) {
                TvApp(
                    view = view,
                    joinPort = ServerHolder.boundPort,
                    serverRunning = ServerHolder.serverRunning,
                    onChoice = ::onChoice,
                    onRemoteAction = ::onRemoteAction
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Being backgrounded is a transition: make sure everything is safely on disk first.
        offMain { ServerHolder.manager?.persistNow() }
    }

    /**
     * Remote keys that must work wherever focus happens to be. Media play/pause and the TV
     * remote's stop button are wired to the immediate global pause, so the control is genuinely
     * always available from the sofa.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val manager = ServerHolder.manager
        if (manager != null) {
            when (keyCode) {
                KeyEvent.KEYCODE_MEDIA_PAUSE,
                KeyEvent.KEYCODE_MEDIA_STOP,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    offMain { manager.applyAction(null, GlobalPause) }
                    return true
                }

                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    offMain { manager.applyAction(null, ExecutionCommand("next")) }
                    return true
                }

                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    offMain { manager.applyAction(null, ExecutionCommand("prev")) }
                    return true
                }

                KeyEvent.KEYCODE_INFO, KeyEvent.KEYCODE_GUIDE -> {
                    offMain { manager.applyAction(null, OpenDraft) }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    // ------------------------------------------------------------------ actions

    private fun onChoice(id: String) {
        val manager = ServerHolder.manager ?: return
        val head = id.substringBefore(':')
        val arg = id.substringAfter(':', "")
        offMain {
            when (head) {
                "resume_session" -> manager.resumeSavedSession()
                "new_game" -> manager.startNewSession()
                "save_contract" -> manager.applyAction(null, SaveContract)
                "tv_resume" -> manager.applyAction(null, ResumeVote(true))
                "end" -> manager.applyAction(null, EndSession)
                "continue" -> manager.applyAction(null, ContinueFlow)
                "open_draft" -> manager.applyAction(null, OpenDraft)
                "close_draft" -> manager.applyAction(null, com.thecontract.core.protocol.CloseDraft)
                "global_pause" -> manager.applyAction(null, GlobalPause)
                "exec" -> manager.applyAction(null, ExecutionCommand(arg))
                "pick_finale_order" -> Unit // never chosen from the TV: it is a private selection
                else -> Unit
            }
        }
    }

    private fun onRemoteAction(action: RemoteAction) {
        val manager = ServerHolder.manager ?: return
        offMain {
            when (action) {
                RemoteAction.Pause -> manager.applyAction(null, GlobalPause)
                RemoteAction.Resume -> manager.applyAction(null, ResumeVote(true))
                RemoteAction.StopAllTimers -> manager.applyAction(null, StopAllTimers)
                is RemoteAction.Timer -> manager.applyAction(null, TimerCommand(action.timerId, action.command))
                is RemoteAction.Exec -> manager.applyAction(null, ExecutionCommand(action.command))
                RemoteAction.OpenDraft -> manager.applyAction(null, OpenDraft)
                RemoteAction.CloseDraft -> manager.applyAction(null, com.thecontract.core.protocol.CloseDraft)
                RemoteAction.SaveContract -> manager.applyAction(null, SaveContract)
                RemoteAction.NewGame -> manager.startNewSession()
                RemoteAction.ResumeSession -> manager.resumeSavedSession()
                RemoteAction.AbandonSession -> manager.abandonSession()
                RemoteAction.EndSession -> manager.applyAction(null, EndSession)
                RemoteAction.RestartPairing -> manager.restartPairing()
                is RemoteAction.ReleaseSlot -> manager.releaseSlot(action.slot)
                is RemoteAction.ConfirmReclaim -> manager.confirmReclaim(action.requestId, action.approve)
                is RemoteAction.SelectInterface -> manager.selectInterface(action.id)
                is RemoteAction.SignFor -> actFor(action.slot, ProposalRespond(com.thecontract.core.model.ProposalResponse.SIGN))
                is RemoteAction.RejectFor -> actFor(action.slot, ProposalRespond(com.thecontract.core.model.ProposalResponse.REJECT))
            }
        }
    }

    /** The remote may only stand in for a player during a public step. */
    private fun actFor(slot: Slot, action: GameAction) {
        val manager = ServerHolder.manager ?: return
        val phase = ServerHolder.tvView.value.phase
        if (phase != GamePhase.PROPOSAL && phase != GamePhase.WAITING_FOR_PROPOSAL_RESPONSES) return
        manager.applyAction(slot, action)
    }

    private fun offMain(block: () -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) { runCatching { block() } }
    }
}
