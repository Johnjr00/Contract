package com.thecontract.core.engine

import com.thecontract.core.model.GameTimer
import com.thecontract.core.model.RenderedTimer
import com.thecontract.core.model.Slot
import com.thecontract.core.model.TimerBoard
import com.thecontract.core.model.TimerRunState

/**
 * Server-owned timers (section 40).
 *
 * The TV server is the only clock. Phones send commands and render whatever the server
 * broadcasts, so a phone whose clock is minutes out cannot change the result.
 *
 * Persistence rule: `remainingMs` is only rewritten on a transition (start, pause, reset,
 * complete, board teardown). While a timer runs, the remaining time is derived from
 * `runningSinceMs`, so the per-second countdown is never written to storage. After a process
 * death or reboot a running timer is restored **paused**, at the last safely persisted value —
 * a sexual-action timer must never keep counting while the app is unavailable.
 */
object TimerEngine {

    fun board(
        context: String,
        timers: List<RenderedTimer>,
        controller: Slot?,
        bothMayControl: Boolean,
        completer: Slot?
    ): TimerBoard = TimerBoard(
        timers = timers.map {
            GameTimer(
                id = it.id,
                label = it.label,
                parentInstruction = context,
                totalSeconds = it.seconds,
                remainingMs = it.seconds * 1000L,
                state = TimerRunState.IDLE
            )
        },
        controllerSlot = controller,
        bothMayControl = bothMayControl,
        completerSlot = completer,
        context = context
    )

    val empty: TimerBoard = TimerBoard()

    fun mayControl(board: TimerBoard, slot: Slot?): Boolean = when {
        slot == null -> true // the TV remote is always allowed as backup
        board.bothMayControl -> true
        board.controllerSlot == null -> true
        else -> board.controllerSlot == slot
    }

    fun mayComplete(board: TimerBoard, slot: Slot?): Boolean = when {
        slot == null -> true
        board.bothMayControl -> true
        board.completerSlot == null -> true
        else -> board.completerSlot == slot
    }

    private fun map(board: TimerBoard, id: String, f: (GameTimer) -> GameTimer): TimerBoard =
        board.copy(timers = board.timers.map { if (it.id == id) f(it) else it })

    fun start(board: TimerBoard, id: String, nowMs: Long): TimerBoard = map(board, id) { t ->
        when (t.state) {
            TimerRunState.RUNNING -> t
            TimerRunState.COMPLETED -> t
            else -> t.copy(state = TimerRunState.RUNNING, runningSinceMs = nowMs)
        }
    }

    fun pause(board: TimerBoard, id: String, nowMs: Long): TimerBoard = map(board, id) { t ->
        if (t.state != TimerRunState.RUNNING) {
            t
        } else {
            t.copy(state = TimerRunState.PAUSED, remainingMs = t.remainingAt(nowMs), runningSinceMs = null)
        }
    }

    fun reset(board: TimerBoard, id: String): TimerBoard = map(board, id) { t ->
        t.copy(state = TimerRunState.IDLE, remainingMs = t.totalSeconds * 1000L, runningSinceMs = null)
    }

    fun pauseAll(board: TimerBoard, nowMs: Long): TimerBoard =
        board.timers.fold(board) { acc, t -> pause(acc, t.id, nowMs) }

    fun resetAll(board: TimerBoard): TimerBoard =
        board.timers.fold(board) { acc, t -> reset(acc, t.id) }

    /** Called on the server tick; flips expired timers to COMPLETED and freezes their value. */
    fun settle(board: TimerBoard, nowMs: Long): TimerBoard = board.copy(
        timers = board.timers.map { t ->
            if (t.state == TimerRunState.RUNNING && t.remainingAt(nowMs) <= 0L) {
                t.copy(state = TimerRunState.COMPLETED, remainingMs = 0, runningSinceMs = null)
            } else {
                t
            }
        }
    )

    /**
     * Restore after a process restart. Anything that was running comes back paused at the last
     * persisted remaining time rather than silently continuing.
     */
    fun restoreAfterRestart(board: TimerBoard): TimerBoard = board.copy(
        timers = board.timers.map { t ->
            if (t.state == TimerRunState.RUNNING) {
                t.copy(state = TimerRunState.PAUSED, runningSinceMs = null)
            } else {
                t
            }
        }
    )

    /** Snapshot for persistence: freezes running timers at their current value. */
    fun freeze(board: TimerBoard, nowMs: Long): TimerBoard = board.copy(
        timers = board.timers.map { t ->
            if (t.state == TimerRunState.RUNNING) {
                t.copy(remainingMs = t.remainingAt(nowMs))
            } else {
                t
            }
        }
    )

    val TimerBoard.allCompleted: Boolean
        get() = timers.isNotEmpty() && timers.all { it.state == TimerRunState.COMPLETED }
}
