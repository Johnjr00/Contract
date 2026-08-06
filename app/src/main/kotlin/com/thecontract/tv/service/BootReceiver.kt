package com.thecontract.tv.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.thecontract.tv.ContractLog
import com.thecontract.tv.data.RoomStateStore

/**
 * Brings the local server back after a television reboot, but only when there is an unfinished
 * session to bring back (section 5).
 *
 * The service is started, it restores the saved session, and every running timer comes back
 * paused. Nothing is revealed on screen until someone opens the app, and the notification says
 * nothing about the contents of the game.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val pending = goAsync()
        Thread {
            try {
                val store = RoomStateStore(context.applicationContext)
                val saved = runCatching { store.loadActiveSession() }.getOrNull()
                if (saved != null && !saved.finished) {
                    ContractLog.i("Unfinished session found after boot; restoring the local server")
                    ContractService.start(context.applicationContext, resumeUnfinished = true)
                }
            } catch (t: Throwable) {
                ContractLog.w("Boot restore failed: ${t.message}")
            } finally {
                pending.finish()
            }
        }.start()
    }
}
