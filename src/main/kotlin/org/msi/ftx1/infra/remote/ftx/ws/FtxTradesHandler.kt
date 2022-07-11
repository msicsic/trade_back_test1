package org.msi.ftx1.infra.remote.ftx.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.msi.ftx1.infra.remote.ftx.FtxTrade
import org.msi.ftx1.infra.remote.ftx.FtxTradeBatch

class FtxTradesHandler(
    val symbol: String,
    val objectMapper: ObjectMapper,
    val listener: (FtxTradeBatch) -> Unit
) : FtxSseHandler {

    override fun register(websocket: Websocket) {
        websocket.send(WsMessage("""{"op": "subscribe", "channel": "trades", "market": "$symbol"}"""))
    }

    override fun unregister(websocket: Websocket) {
        websocket.send(WsMessage("""{"op": "unsubscribe", "channel": "trades", "market": "$symbol"}"""))
    }

    override fun matchMessage(message: Map<String, Any>): Boolean {
        return message["channel"]?.equals("trades") ?: false &&
                message["market"]?.equals(symbol) ?: false && !(message["type"]?.equals("subscribed") ?: false)
    }

    override fun handleMessage(message: Map<String, Any>) {
        val trades: List<FtxTrade> = objectMapper.convertValue(message["data"]!!)
        listener(FtxTradeBatch(symbol, trades))
    }
}
