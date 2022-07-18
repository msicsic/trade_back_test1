package org.msi.ftx1.infra.remote.ftx.ws

import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class FtxPongHandler : FtxSseHandler {

    private lateinit var websocket: Websocket

    override fun register(websocket: Websocket) {
        this.websocket = websocket
        websocket.send(WsMessage("""{"op": "ping"}"""))
        thread {
            while (true) {
                sleep(10000)
                websocket.send(WsMessage("""{"op": "ping"}"""))
            }
        }
    }

    override fun matchMessage(message: Map<String, Any>) =
        message["type"]?.equals("pong") ?: false || message["type"]?.equals("ping") ?: false

    override fun handleMessage(message: Map<String, Any>) {
        if (message["type"]?.equals("ping") == true) {
            websocket.send(WsMessage("""{"op": "pong"}"""))
        }
    //System.err.println("Pong/Ping received: ${message["type"]}")
    }
}
