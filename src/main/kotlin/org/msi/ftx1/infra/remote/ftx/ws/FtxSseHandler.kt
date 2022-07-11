package org.msi.ftx1.infra.remote.ftx.ws

import org.http4k.websocket.Websocket

interface FtxSseHandler {
    fun register(websocket: Websocket)

    fun unregister(websocket: Websocket) = Unit

    fun matchMessage(message: Map<String, Any>): Boolean

    fun handleMessage(message: Map<String, Any>)
}
