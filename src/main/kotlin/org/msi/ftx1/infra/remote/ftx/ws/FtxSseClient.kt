package org.msi.ftx1.infra.remote.ftx.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.Websocket
import org.msi.ftx1.infra.remote.ftx.FtxOrderBookWithSymbol
import org.msi.ftx1.infra.remote.ftx.FtxTradeBatch


class FtxSseClient(
    private val objectMapper: ObjectMapper
) {
    private lateinit var orderBookConsumer: (FtxOrderBookWithSymbol) -> Unit
    private lateinit var tradesConsumer: (FtxTradeBatch) -> Unit
    private lateinit var websocket: Websocket

    private val handlers = mutableListOf<FtxSseHandler>(FtxPongHandler())
    private var connected = false

    private fun addHandler(handler: FtxSseHandler) {
        handlers.add(handler)
        handler.register(websocket)
    }

    fun registerSymbol(symbol: String) {
        addHandler(FtxOrderBookHandler(symbol, objectMapper) { orderBook2 -> orderBookConsumer(orderBook2) })
        addHandler(FtxTradesHandler(symbol, objectMapper) { trades -> tradesConsumer(trades) })
    }

    fun unregisterSymbol(symbol: String) {
        handlers.forEach { it.unregister(websocket) }
    }

    fun start(
        orderBookConsumer: (FtxOrderBookWithSymbol) -> Unit,
        tradesConsumer: (FtxTradeBatch) -> Unit
    ) {
        this.orderBookConsumer = orderBookConsumer
        this.tradesConsumer = tradesConsumer

        websocket = WebsocketClient.nonBlocking(Uri.of("wss://ftx.com/ws/"),
            onError = {
                System.err.println("shit happened $it")
            },
            onConnect = {
                connected = true
            })
        websocket.onMessage { wsMessage ->
            val message: Map<String, Any> = objectMapper.readValue(wsMessage.bodyString())
            handlers
                .filter { it.matchMessage(message) }
                .forEach {
                    try {
                        it.handleMessage(message)
                    } catch (t: Throwable) {
                        System.err.println(t)
                    }
                }
        }
        websocket.onClose {
            connected = false
            System.err.println("client disconnected $it")
        }
        while (!connected) {
            Thread.sleep(100)
        }
        handlers.forEach { it.register(websocket) }
    }
}
