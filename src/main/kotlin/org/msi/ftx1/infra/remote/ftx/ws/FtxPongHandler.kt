package org.msi.ftx1.infra.remote.ftx.ws

import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage

class FtxPongHandler : FtxSseHandler {

    override fun register(websocket: Websocket) {
        websocket.send(WsMessage("""{"op": "ping"}"""))
    }

    override fun matchMessage(message: Map<String, Any>) =
        message["type"]?.equals("pong") ?: false

    override fun handleMessage(message: Map<String, Any>) {
        System.err.println("Pong received")
    }
}
