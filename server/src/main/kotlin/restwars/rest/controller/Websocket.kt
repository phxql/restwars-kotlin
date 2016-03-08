package restwars.rest.controller

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.slf4j.LoggerFactory
import restwars.rest.base.Json
import java.util.concurrent.ConcurrentLinkedQueue

@WebSocket
abstract class AbstractWebsocketController {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val sessions = ConcurrentLinkedQueue<Session>()

    @OnWebSocketConnect
    open fun onConnect(session: Session) {
        logger.debug("Websocket {} connected: {}", session.localAddress, session.remoteAddress)
        sessions.add(session)
    }

    @OnWebSocketClose
    open fun onClose(session: Session, statusCode: Int, reason: String) {
        logger.debug("Websocket {} closed: {}, {} {}", session.localAddress, session.remoteAddress, statusCode, reason)
        sessions.remove(session)
    }

    protected fun broadcastJson(obj: Any) {
        val json = Json.toJson(obj)
        for (session in sessions) {
            session.remote.sendStringByFuture(json)
        }
    }

    protected fun sendJson(obj: Any, session: Session) {
        val json = Json.toJson(obj)
        session.remote.sendStringByFuture(json)
    }
}