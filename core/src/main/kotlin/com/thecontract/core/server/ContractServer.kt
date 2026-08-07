package com.thecontract.core.server

import com.thecontract.core.protocol.ProtocolJson
import com.thecontract.core.protocol.ServerMessage
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * The embedded HTTP + WebSocket server hosted inside the Android TV app (section 5).
 *
 * Serves the phone-controller page and its assets from resources bundled in the APK, exposes a
 * small REST surface, and carries the authoritative protocol over a WebSocket. Nothing is
 * fetched from the internet: there are no CDN references, no remote fonts and no outbound calls.
 *
 * The server binds to all local interfaces for reliability, but the join URL encoded into the QR
 * code only ever advertises the interface the TV user selected.
 */
class ContractServer(
    private val manager: SessionManager,
    val boundPort: Int
) : NanoWSD(boundPort) {

    private val sockets = ConcurrentHashMap<String, ContractSocket>()
    private val clientIds = AtomicLong(0)
    private val PING_PAYLOAD = byteArrayOf()

    init {
        manager.transport = object : SessionManager.Transport {
            override fun send(clientId: String, message: ServerMessage) {
                val socket = sockets[clientId] ?: return
                runCatching { socket.send(ProtocolJson.encodeToString(message)) }
            }

            override fun close(clientId: String, reason: String) {
                sockets[clientId]?.let { socket ->
                    runCatching { socket.close(WebSocketFrame.CloseCode.NormalClosure, reason, false) }
                }
            }
        }
        manager.setPort(boundPort)
    }

    // ------------------------------------------------------------------ websocket

    inner class ContractSocket(
        private val clientId: String,
        handshake: IHTTPSession
    ) : WebSocket(handshake) {

        override fun onOpen() {
            sockets[clientId] = this
            manager.onClientConnected(clientId)
        }

        override fun onClose(code: WebSocketFrame.CloseCode?, reason: String?, initiatedByRemote: Boolean) {
            sockets.remove(clientId)
            manager.onClientDisconnected(clientId)
        }

        override fun onMessage(message: WebSocketFrame) {
            manager.onMessage(clientId, message.textPayload)
        }

        override fun onPong(pong: WebSocketFrame?) = Unit

        override fun onException(exception: IOException?) {
            sockets.remove(clientId)
            manager.onClientDisconnected(clientId)
        }
    }

    override fun openWebSocket(handshake: IHTTPSession): WebSocket {
        val clientId = "c${clientIds.incrementAndGet()}"
        return ContractSocket(clientId, handshake)
    }

    // ------------------------------------------------------------------ keep-alive

    private val keepAlive = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "contract-ws-keepalive").apply { isDaemon = true }
    }

    /**
     * Pings every open socket on a fixed schedule so no connection ever sits silent long enough
     * to hit the socket read timeout. A ping that fails means the phone is already gone; the
     * read side raises that independently, so it is enough to drop the socket here and let the
     * normal disconnect path run.
     */
    private fun startKeepAlive() {
        keepAlive.scheduleWithFixedDelay({
            sockets.values.forEach { socket ->
                runCatching { socket.ping(PING_PAYLOAD) }
                    .onFailure { runCatching { socket.close(WebSocketFrame.CloseCode.GoingAway, "ping failed", false) } }
            }
        }, WS_PING_INTERVAL_MS, WS_PING_INTERVAL_MS, TimeUnit.MILLISECONDS)
    }

    override fun stop() {
        keepAlive.shutdownNow()
        super.stop()
    }

    // ------------------------------------------------------------------ http

    override fun serveHttp(session: IHTTPSession): Response {
        val uri = session.uri ?: "/"
        val remote = session.headers["remote-addr"] ?: session.remoteIpAddress ?: "unknown"

        if (manager.isRateLimited(remote)) {
            return text(Response.Status.TOO_MANY_REQUESTS, "Too many attempts. Wait a minute and try again.")
        }

        return when {
            uri == "/" || uri == "/index.html" -> landing()

            uri.startsWith("/join/") -> {
                val token = uri.removePrefix("/join/").substringBefore('?').trim('/')
                if (!manager.authorizeJoin(token, remote)) {
                    text(Response.Status.FORBIDDEN, "This link is not valid for the game running on the TV.")
                } else {
                    asset("controller.html", "text/html; charset=utf-8")
                }
            }

            uri.startsWith("/assets/") -> {
                val path = uri.removePrefix("/assets/")
                if (path.contains("..") || path.startsWith("/")) {
                    text(Response.Status.FORBIDDEN, "No.")
                } else {
                    asset(path, mimeFor(path))
                }
            }

            uri == "/api/health" -> json("""{"ok":true,"port":$boundPort}""")

            uri == "/api/session" -> {
                val token = session.parms["token"]
                if (!manager.authorizeJoin(token, remote)) {
                    json("""{"ok":false,"error":"invalid_token"}""", Response.Status.FORBIDDEN)
                } else {
                    json("""{"ok":true,"sessionId":"${manager.sessionRecord?.sessionId.orEmpty()}"}""")
                }
            }

            else -> text(Response.Status.NOT_FOUND, "Not found.")
        }
    }

    private fun landing(): Response = text(
        Response.Status.OK,
        "The Contract is running on this TV. Scan the code shown on the screen to join."
    )

    private fun asset(path: String, mime: String): Response {
        val stream = WebAssets.open(path)
            ?: return text(Response.Status.NOT_FOUND, "Missing asset: $path")
        val bytes = stream.use { it.readBytes() }
        return newFixedLengthResponse(Response.Status.OK, mime, bytes.inputStream(), bytes.size.toLong()).apply {
            addHeader("Cache-Control", "no-store")
            addHeader("X-Content-Type-Options", "nosniff")
        }
    }

    private fun text(status: Response.Status, body: String): Response =
        newFixedLengthResponse(status, NanoHTTPD.MIME_PLAINTEXT, body)

    private fun json(body: String, status: Response.Status = Response.Status.OK): Response =
        newFixedLengthResponse(status, "application/json", body)

    private fun mimeFor(path: String): String = when (path.substringAfterLast('.', "")) {
        "html" -> "text/html; charset=utf-8"
        "css" -> "text/css; charset=utf-8"
        "js" -> "application/javascript; charset=utf-8"
        "json" -> "application/json"
        "svg" -> "image/svg+xml"
        "png" -> "image/png"
        "ico" -> "image/x-icon"
        "woff2" -> "font/woff2"
        else -> "application/octet-stream"
    }

    companion object {
        /**
         * How long an accepted socket may stay silent before the read times out and the
         * connection is torn down.
         *
         * NanoHTTPD applies this as `SO_TIMEOUT` to *every* accepted socket, and a WebSocket
         * keeps using the same socket after the upgrade. Its default, [SOCKET_READ_TIMEOUT],
         * is 5 seconds — far shorter than any sane heartbeat — so an idle phone was guaranteed
         * to be disconnected every five seconds and reconnect in a loop. This must always stay
         * comfortably larger than [WS_PING_INTERVAL_MS]; the assertion below enforces that.
         *
         * It is deliberately still finite: a value of 0 would block forever, so a half-open
         * connection would pin its handler thread for the lifetime of the process.
         */
        const val SOCKET_TIMEOUT_MS = 60_000

        /**
         * How often the server pings each open socket.
         *
         * The heartbeat is driven from the server rather than relying on the phone's own timer,
         * because mobile browsers throttle (and eventually suspend) `setInterval` when the tab
         * is backgrounded or the screen locks — exactly when a phone is sitting idle mid-scene.
         * The browser answers a ping frame with a pong automatically, without waking any page
         * script, and that inbound pong is what resets the socket's read timer.
         */
        const val WS_PING_INTERVAL_MS = 20_000L

        /**
         * Starts on the preferred port, falling back through a small range if it is taken.
         * The actual port is reported so the QR code always advertises the real address.
         */
        fun startWithFallback(
            manager: SessionManager,
            ports: List<Int> = SessionManager.FALLBACK_PORTS
        ): ContractServer {
            check(SOCKET_TIMEOUT_MS > WS_PING_INTERVAL_MS * 2) {
                "the socket timeout must outlast at least two missed pings"
            }
            var lastError: Exception? = null
            for (port in ports) {
                val server = ContractServer(manager, port)
                try {
                    server.start(SOCKET_TIMEOUT_MS, false)
                    server.startKeepAlive()
                    return server
                } catch (e: IOException) {
                    lastError = e
                    runCatching { server.stop() }
                }
            }
            throw IllegalStateException("No usable port in $ports", lastError)
        }
    }
}
