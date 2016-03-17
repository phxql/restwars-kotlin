package restwars.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient
import java.net.URI
import java.util.concurrent.CopyOnWriteArraySet

interface WebSocketCallback<T> {
    fun call(response: T)
}

class WebSocket<T>(
        private val mapper: ObjectMapper,
        private val responseClass: Class<T>,
        private val url: String
) {
    private val lock = Object()
    private val callbacks: MutableSet<WebSocketCallback<T>> = CopyOnWriteArraySet()
    private var client: WebSocketClient? = null

    fun addCallback(callback: WebSocketCallback<T>) {
        callbacks.add(callback)

        synchronized(lock) {
            if (client == null && !callbacks.isEmpty()) {
                startClient()
            }
        }
    }

    fun removeCallback(callback: WebSocketCallback<T>) {
        if (callbacks.remove(callback)) {
            synchronized(lock) {
                if (callbacks.isEmpty()) {
                    stopClient()
                }
            }
        }
    }

    private fun startClient() {
        val handler = WebsocketHandler(mapper, responseClass, callbacks)

        val newClient = WebSocketClient()
        newClient.start()
        val request = ClientUpgradeRequest()

        newClient.connect(handler, URI.create(url), request)

        client = newClient
    }

    private fun stopClient() {
        client?.stop()
        client?.destroy()
        client = null
    }

    @WebSocket
    class WebsocketHandler<T>(
            private val mapper: ObjectMapper,
            private val responseClazz: Class<T>,
            private val callbacks: Iterable<WebSocketCallback<T>>
    ) {
        @OnWebSocketMessage
        fun onMessage(message: String) {
            val response = mapper.readValue(message, responseClazz)
            for (callback in callbacks) {
                callback.call(response)
            }
        }
    }
}