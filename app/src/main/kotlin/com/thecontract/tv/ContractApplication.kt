package com.thecontract.tv

import android.app.Application
import android.util.Log

/**
 * Logging policy (section 11/42).
 *
 * Nothing that could carry a private profile answer, a boundary, an equipment list or the text
 * of a contract term is ever logged. In release builds these calls compile to nothing at all —
 * `android.util.Log` is stripped by the ProGuard rules — so even a lifecycle message cannot leak
 * from a shipped APK.
 */
object ContractLog {
    private const val TAG = "TheContract"

    fun i(message: String) {
        if (BuildConfig.VERBOSE_LOGGING) Log.i(TAG, message)
    }

    fun w(message: String) {
        if (BuildConfig.VERBOSE_LOGGING) Log.w(TAG, message)
    }
}

class ContractApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ContractLog.i("The Contract starting")
    }
}
