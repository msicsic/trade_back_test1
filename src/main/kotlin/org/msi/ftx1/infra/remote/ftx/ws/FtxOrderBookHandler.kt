package org.msi.ftx1.infra.remote.ftx.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.msi.ftx1.business.OrderBook2
import org.msi.ftx1.infra.remote.ftx.FtxOrderBookResult
import org.msi.ftx1.infra.remote.ftx.FtxOrderBookWithSymbol

// sert plutot a scanner ce que font les gros, les prix attendus, pour se positionner comme eux (à valider)
class FtxOrderBookHandler(
    val symbol: String,
    val objectMapper: ObjectMapper,
    val listener: (FtxOrderBookWithSymbol) -> Unit
) : FtxSseHandler {


    override fun register(websocket: Websocket) {
        websocket.send(WsMessage("""{"op": "subscribe", "channel": "orderbook", "market": "$symbol"}"""))
    }

    override fun unregister(websocket: Websocket) {
        websocket.send(WsMessage("""{"op": "unsubscribe", "channel": "orderbook", "market": "$symbol"}"""))
    }

    override fun matchMessage(message: Map<String, Any>): Boolean {
        return message["channel"]?.equals("orderbook") ?: false &&
                message["market"]?.equals(symbol) ?: false && !(message["type"]?.equals("subscribed") ?: false)
    }

    override fun handleMessage(message: Map<String, Any>) {
        val partial: FtxOrderBookResult = objectMapper.convertValue(message["data"]!!)
        listener(FtxOrderBookWithSymbol(symbol, partial))
    }
}
