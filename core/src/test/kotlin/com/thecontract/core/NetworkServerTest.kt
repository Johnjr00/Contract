package com.thecontract.core

import com.thecontract.core.engine.GameEngine
import com.thecontract.core.net.LocalInterface
import com.thecontract.core.net.InterfaceKind
import com.thecontract.core.persistence.InMemoryStateStore
import com.thecontract.core.protocol.ClientMessage
import com.thecontract.core.protocol.Hello
import com.thecontract.core.protocol.HelloOk
import com.thecontract.core.protocol.Ping
import com.thecontract.core.protocol.ProtocolJson
import com.thecontract.core.protocol.ServerMessage
import com.thecontract.core.protocol.SessionFull
import com.thecontract.core.protocol.StateSnapshot
import com.thecontract.core.server.ContractServer
import com.thecontract.core.server.SessionManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.WebSocket
import java.time.Duration
import java.util.concurrent.CompletionStage
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The embedded server over real sockets: HTTP asset serving, join-token gating, port fallback,
 * and the WebSocket protocol carrying real join and rejection flows.
 */
class NetworkServerTest {

    private val servers = mutableListOf<ContractServer>()
    private val http: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    @AfterEach
    fun tearDown() {
        servers.forEach { runCatching { it.stop() } }
        servers.clear()
    }

    private fun startServer(): Pair<SessionManager, ContractServer> {
        val manager = SessionManager(InMemoryStateStore(), GameEngine())
        manager.refreshInterfaces(
            listOf(
                LocalInterface(
                    "lo0@127.0.0.1", "Loopback", "127.0.0.1",
                    InterfaceKind.OTHER, privateAddress = true, linkLocal = false, priority = 60
                )
            )
        )
        val server = ContractServer.startWithFallback(manager)
        servers += server
        manager.startNewSession()
        return manager to server
    }

    private fun get(port: Int, path: String): HttpResponse<String> = http.send(
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port$path"))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString()
    )

    // ------------------------------------------------------------------ HTTP

    @Test
    fun `the controller page and its assets are served from the APK bundle`() {
        val (manager, server) = startServer()
        val token = manager.joinToken!!

        val page = get(server.boundPort, "/join/$token")
        assertEquals(200, page.statusCode())
        assertTrue(page.body().contains("The Contract"))
        assertTrue(page.body().contains("/assets/controller.js"))

        val js = get(server.boundPort, "/assets/controller.js")
        assertEquals(200, js.statusCode())
        assertTrue(js.body().contains("PLAYER_ACTION"))
        assertTrue(js.headers().firstValue("content-type").orElse("").contains("javascript"))

        val css = get(server.boundPort, "/assets/controller.css")
        assertEquals(200, css.statusCode())

        assertEquals(200, get(server.boundPort, "/api/health").statusCode())
    }

    @Test
    fun `an invalid or missing join token is refused and path traversal is blocked`() {
        val (_, server) = startServer()
        assertEquals(403, get(server.boundPort, "/join/not-a-real-token").statusCode())
        assertEquals(403, get(server.boundPort, "/assets/../../etc/passwd").statusCode())
        assertEquals(404, get(server.boundPort, "/assets/secrets.txt").statusCode())
        assertEquals(404, get(server.boundPort, "/nope").statusCode())
    }

    @Test
    fun `the server falls back to another port when the preferred one is taken`() {
        val (_, first) = startServer()
        val (secondManager, second) = startServer()
        assertTrue(second.boundPort != first.boundPort, "the second server reused a bound port")
        assertTrue(second.boundPort in SessionManager.FALLBACK_PORTS)
        // The advertised URL always carries the port actually bound.
        assertTrue(secondManager.joinUrl()!!.contains(":${second.boundPort}/"))
        assertEquals(200, get(second.boundPort, "/api/health").statusCode())
    }

    // ------------------------------------------------------------------ WebSocket

    private class TestSocket : WebSocket.Listener {
        val messages = CopyOnWriteArrayList<ServerMessage>()
        private val buffer = StringBuilder()
        val latch = CountDownLatch(2)
        lateinit var socket: WebSocket

        override fun onOpen(webSocket: WebSocket) {
            webSocket.request(1)
        }

        override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
            buffer.append(data)
            if (last) {
                runCatching { ProtocolJson.decodeFromString<ServerMessage>(buffer.toString()) }
                    .onSuccess { messages += it; latch.countDown() }
                buffer.setLength(0)
            }
            webSocket.request(1)
            return null
        }

        fun send(message: ClientMessage) {
            socket.sendText(ProtocolJson.encodeToString(message), true).join()
        }

        fun await(seconds: Long = 5) = latch.await(seconds, TimeUnit.SECONDS)
    }

    private fun openSocket(port: Int, token: String): TestSocket {
        val listener = TestSocket()
        listener.socket = http.newWebSocketBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .buildAsync(URI.create("ws://127.0.0.1:$port/ws?token=$token"), listener)
            .join()
        return listener
    }

    @Test
    fun `two phones join over a real websocket and a third is told the session is full`() {
        val (manager, server) = startServer()
        val token = manager.joinToken!!

        val one = openSocket(server.boundPort, token)
        one.send(Hello("device-one"))
        assertTrue(one.await(), "the first phone never got a reply")

        val two = openSocket(server.boundPort, token)
        two.send(Hello("device-two"))
        assertTrue(two.await(), "the second phone never got a reply")

        val helloOne = one.messages.filterIsInstance<HelloOk>().firstOrNull()
        val helloTwo = two.messages.filterIsInstance<HelloOk>().firstOrNull()
        assertNotNull(helloOne)
        assertNotNull(helloTwo)
        assertEquals(com.thecontract.core.model.Slot.PLAYER_1, helloOne.slot)
        assertEquals(com.thecontract.core.model.Slot.PLAYER_2, helloTwo.slot)
        assertTrue(helloOne.resumeToken.length >= 40)
        assertTrue(one.messages.any { it is StateSnapshot })

        val three = openSocket(server.boundPort, token)
        three.send(Hello("device-three"))
        val full = (1..40).firstNotNullOfOrNull {
            Thread.sleep(50)
            three.messages.filterIsInstance<SessionFull>().firstOrNull()
        }
        assertNotNull(full, "the third device was not refused")
        assertTrue(three.messages.none { it is StateSnapshot }, "the third device received game state")

        listOf(one, two, three).forEach { it.socket.abort() }
    }

    @Test
    fun `a heartbeat is answered so a phone can detect a dead link`() {
        val (manager, server) = startServer()
        val socket = openSocket(server.boundPort, manager.joinToken!!)
        socket.send(Ping(42))
        val pong = (1..40).firstNotNullOfOrNull {
            Thread.sleep(50)
            socket.messages.filterIsInstance<com.thecontract.core.protocol.Pong>().firstOrNull()
        }
        assertNotNull(pong)
        assertEquals(42, pong.t)
        socket.socket.abort()
    }

    @Test
    fun `a phone that drops its socket keeps its slot and reclaims it on reconnect`() {
        val (manager, server) = startServer()
        val token = manager.joinToken!!

        val one = openSocket(server.boundPort, token)
        one.send(Hello("device-one"))
        assertTrue(one.await())
        val resume = one.messages.filterIsInstance<HelloOk>().first().resumeToken

        // Simulate the phone locking / losing Wi-Fi.
        one.socket.abort()
        Thread.sleep(300)
        assertEquals("DISCONNECTED", manager.tvView().connections["PLAYER_1"])

        val back = openSocket(server.boundPort, token)
        back.send(Hello("device-one", resume))
        assertTrue(back.await(), "the reconnecting phone never got a reply")
        assertEquals(
            com.thecontract.core.model.Slot.PLAYER_1,
            back.messages.filterIsInstance<HelloOk>().first().slot
        )
        assertEquals("CONNECTED", manager.tvView().connections["PLAYER_1"])
        back.socket.abort()
    }
}
