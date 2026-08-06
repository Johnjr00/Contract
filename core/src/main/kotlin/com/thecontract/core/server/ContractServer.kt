package com.thecontract.core.server

import com.thecontract.core.protocol.ProtocolJson
import com.thecontract.core.protocol.ServerMessage
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
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
         * Starts on the preferred port, falling back through a small range if it is taken.
         * The actual port is reported so the QR code always advertises the real address.
         */
        fun startWithFallback(
            manager: SessionManager,
            ports: List<Int> = SessionManager.FALLBACK_PORTS
        ): ContractServer {
            var lastError: Exception? = null
            for (port in ports) {
                val server = ContractServer(manager, port)
                try {
                    server.start(SOCKET_READ_TIMEOUT, false)
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
