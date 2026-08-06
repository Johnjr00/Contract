package com.thecontract.core.devserver

import com.thecontract.core.engine.GameEngine
import com.thecontract.core.persistence.JsonFileStateStore
import com.thecontract.core.server.ContractServer
import com.thecontract.core.server.SessionManager
import java.io.File

/**
 * Standalone entry point for running the embedded server outside Android — the same
 * [ContractServer] and [SessionManager] the TV app hosts inside its foreground service, with no
 * Android dependency at all. Useful for manual testing of the server and the phone-controller
 * pages from a desktop browser: `./gradlew :core:run`.
 *
 * Not part of the shipped app; the Android module has its own entry point via
 * `com.thecontract.tv.service.ContractService`.
 */
fun main() {
    val dataDir = File(System.getProperty("java.io.tmpdir"), "the-contract-devserver")
    val store = JsonFileStateStore(dataDir)
    val manager = SessionManager(store, GameEngine())

    manager.refreshInterfaces(com.thecontract.core.net.NetworkScanner.scan())
    val server = ContractServer.startWithFallback(manager)
    manager.startNewSession()

    println("== The Contract dev server ==")
    println("Listening on port ${server.boundPort}")
    println("Join URL: ${manager.joinUrl()}")
    println("Data dir: ${dataDir.absolutePath}")
    println("Press Ctrl+C to stop.")

    Runtime.getRuntime().addShutdownHook(Thread { runCatching { server.stop() } })

    while (true) {
        Thread.sleep(500)
        manager.tick()
    }
}
