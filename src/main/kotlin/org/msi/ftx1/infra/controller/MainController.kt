package org.msi.ftx1.infra.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ServerFilters
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.long
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.websockets
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.PolyHandler
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.msi.ftx1.infra.remote.WsConnection
import org.msi.ftx1.infra.remote.ftx.FtxClient
import java.util.*

class MainController(
    private val ftxClient: FtxClient,
    private val objectMapper: ObjectMapper
) {
    private var server: Http4kServer
    private val connections = mutableListOf<WsConnection>()

    init {

        val http = routes(
            "/ping" bind GET to {
                OK(pong())
            },

            "/ftx/orderbook/{market}" bind GET to {
                val market = Path.of("market")(it)

                val orderBook = ftxClient.getOrderBook(market)

                OK(orderBook)
            },

            "/ftx/trades/{market}" bind GET to {
                val market = Path.of("market")(it)
                val startTime = Query.long().defaulted("start_time", (Date().time - 3600*1000)/1000)(it)
                val endTime = Query.long().defaulted("end_time", Date().time / 1000)(it)

                val trades = ftxClient.getTrades(market, startTime, endTime)

                System.err.println("trades: ${trades.size}")
                OK(trades)
            }
        )

        val ws = websockets(
            "/ws/ftx/orderbook" bind { ws: Websocket ->
                //val name = namePath(ws.upgradeRequest)
                System.err.println("new WS: $ws")
                val name = "toto"
                ws.send(WsMessage("hello $name"))
                ws.onMessage {
                    ws.send(WsMessage("$name is responding"))
                }
                ws.onClose {
                    println("$name is closing")
                    removeWs(ws)
                }
                registerWS(ws)

                //https://github.com/d3fc/d3fc/blob/master/examples/simple-chart/index.js
            }
        )

        server = PolyHandler(ServerFilters.CatchLensFailure(http), ws).asServer(Netty(9000)).start()

        System.err.println("server started on port ${server.port()}")
    }

    private fun pong() = "pong"

    private fun registerWS(ws: Websocket) {
        connections.add(WsConnection(ws))
    }

    private fun removeWs(ws: Websocket) {
        connections.removeIf { it.ws === ws }
    }

    private fun sendMsg(message: String) {
        connections.forEach { c ->
            c.ws.send(WsMessage(message))
        }
    }

    operator fun Status.invoke(body: Any) = Response(OK).body(objectMapper.writeValueAsString(body))
}


//var orderBook: FtxOrderBook? = null
//
//
//
//
//
//val client: HttpHandler = JavaHttpClient()
//val ftxClient = FtxClient(client)
//
//val wsClient = WebsocketClient.nonBlocking(Uri.of("wss://ftx.com/ws/")) {
//    it.send(WsMessage("""{"op": "ping"}"""))
//    it.send(WsMessage("""{"op": "subscribe", "channel": "trades", "market": "ETH-PERP"}"""))
//    it.send(WsMessage("""{"op": "subscribe", "channel": "ticker", "market": "ETH-PERP"}"""))
//}
//wsClient.onMessage {
//    println("non-blocking client received:$it")
//}
//
//wsClient.onClose {
//    println("non-blocking client closing")
//}
//
//var orderBook: FtxOrderBook? = null
//
//val connections = mutableListOf<WsConnection>()
//
//fun registerWS(ws: Websocket) {
//    connections.add(WsConnection(ws))
//}
//
//fun removeWs(ws: Websocket) {
//    connections.removeIf { it.ws === ws }
//}
//
//fun sendMsg(message: String) {
//    connections.forEach { c ->
//        c.ws.send(WsMessage(message))
//    }
//}
