package org.msi.ftx1.infra.remote.ftx.ws

import com.fasterxml.jackson.databind.ObjectMapper
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage

// Le ticker est redondant avec l'orderbook, il ne sert donc pas a grand chose...
class FtxTickerHandler(val symbol: String, val objectMapper: ObjectMapper) : FtxSseHandler {
    var currentSecond = 0L

    override fun register(websocket: Websocket) {
        websocket.send(WsMessage("""{"op": "subscribe", "channel": "ticker", "market": "$symbol"}"""))
    }

    override fun unregister(websocket: Websocket) {
        websocket.send(WsMessage("""{"op": "unsubscribe", "channel": "ticker", "market": "$symbol"}"""))
    }

    override fun matchMessage(message: Map<String, Any>): Boolean {
        return message["channel"]?.equals("ticker") ?: false &&
                message["market"]?.equals(symbol) ?: false && !(message["type"]?.equals("subscribed") ?: false)
    }

    override fun handleMessage(message: Map<String, Any>) {
        val ticker = objectMapper.convertValue(message["data"], Ticker::class.java)
        val time = ticker.time.toLong()

        if (currentSecond != time) {
            currentSecond = time
            System.err.println("$currentSecond - ticker: $ticker")
        }
    }
}

data class Ticker(
    val bid: Double?,
    val ask: Double?,
    val last: Double?,
    val bidSize: Double?,
    val askSize: Double?,
    val time: Double,
)
