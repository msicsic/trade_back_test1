package org.msi.ftx1.infra.remote.ftx

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.client.WebsocketClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.format.Jackson.asJsonObject
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.msi.ftx1.business.OrderBook2
import java.lang.Thread.sleep

interface FtxSseHandler {
    fun register(websocket: Websocket)
    fun unregister(websocket: Websocket) = Unit

    fun matchMessage(message: Map<String, Any>): Boolean

    fun handleMessage(message: Map<String, Any>)
}

class PongHandler : FtxSseHandler {

    override fun register(websocket: Websocket) {
        websocket.send(WsMessage("""{"op": "ping"}"""))
    }

    override fun matchMessage(message: Map<String, Any>) =
        message["type"]?.equals("pong") ?: false

    override fun handleMessage(message: Map<String, Any>) {
        System.err.println("Pong received")
    }
}

class OrderBookHandler(val symbol: String, val objectMapper: ObjectMapper) : FtxSseHandler {
    val orderBook = OrderBook2()
    var currentSecond = 0L

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
        val data = message["data"] as Map<String, Any>
        val bids = data["bids"] as ArrayList<ArrayList<Double>>
        val asks = data["asks"] as ArrayList<ArrayList<Double>>
        val time = (data["time"] as Double).toLong()
        if (message["type"] == "partial") {
            orderBook.buys.clear()
            orderBook.sells.clear()
        }
        // ask = vente
        asks.forEach {
            if (it[1] == 0.0) {
                orderBook.sells.remove(it[0])
            } else {
                orderBook.sells[it[0]] = it[1]
            }
        }
        bids.forEach {
            if (it[1] == 0.0) {
                orderBook.buys.remove(it[0])
            } else {
                orderBook.buys[it[0]] = it[1]
            }
        }
        if (currentSecond != time) {
            currentSecond = time
            //System.err.println("orderbook, size ${orderBook.sells.size+orderBook.buys.size}: $orderBook")

            val min = orderBook.buys.keys.minOf { it }
            val max = orderBook.sells.keys.maxOf { it }
            val totalBuys = orderBook.buys.values.sum()
            val totalSells = orderBook.sells.values.sum()
            val maxBuy = orderBook.buys.entries.maxByOrNull { it.value }
            val maxSell = orderBook.sells.entries.maxByOrNull { it.value }
            val message =
                "range: ${(max-min)/max*100.0}, buy: total: $totalBuys, resist: ${maxBuy?.key} ${maxBuy?.value} // sell: total: $totalSells, resist: ${maxSell?.key} ${maxSell?.value}"
            System.err.println(message)
        }
    }

}

// TODO: il faut récuperer le ticker
// TODO: il faut homogénéiser les modes backtest et live => eval strat a chaque tick, backtest = ticker virtuel
// TODO: il faut un historique du backlog => BDD

class FtxSSeClient(
    val objectMapper: ObjectMapper
) {
    lateinit var websocket: Websocket

    val handlers = mutableListOf<FtxSseHandler>(PongHandler())
    var connected = false

    fun registerOrderBook(symbol: String) {
        val handler = OrderBookHandler(symbol, objectMapper)
        handlers.add(handler)
        handler.register(websocket)
    }

    fun start() {
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
                .forEach { it.handleMessage(message) }
        }
        websocket.onClose {
            connected = false
            System.err.println("client disconnected $it")
        }
        while (!connected) {
            sleep(100)
        }
        handlers.forEach { it.register(websocket) }

//    it.send(WsMessage("""{"op": "ping"}"""))
//    it.send(WsMessage("""{"op": "subscribe", "channel": "trades", "market": "ETH-PERP"}"""))
//    it.send(WsMessage("""{"op": "subscribe", "channel": "ticker", "market": "ETH-PERP"}"""))
//}
//wsClient.onMessage {
//    println("non-blocking client received:$it")
//}
    }
}

class FtxClient(
    private val client: HttpHandler,
    private val objectMapper: ObjectMapper
) {
    fun findArbitrableMarkets(): List<Pair<String, Double>> =
        get<FtxMarkets>("markets")
            .findArbitrable()

    fun getFutureMarkets(): List<String> =
        get<FtxFuturesMarket>("futures").result
            .filter { it.enabled && !it.expired && it.perpetual }
            .map { it.name }

    fun getSpotMarkets(): List<String> =
        get<FtxMarkets>("markets").result
            .filter { it.enabled && it.type == "spot" && it.quoteCurrency == "USD" }
            .map { it.name }

    fun getOrderBook(symbol: String): FtxOrderBookResult =
        get<FtxOrderBook>("markets/$symbol/orderbook?depth=100").also { orderBook ->
            val totalBuys = orderBook.result.buys.sumOf { it.size }
            val totalSells = orderBook.result.sells.sumOf { it.size }
            val averageBuy = orderBook.result.buys.sumOf { it.size * it.price } / totalBuys
            val averageSell = orderBook.result.sells.sumOf { it.size * it.price } / totalSells
            val maxBuy = orderBook.result.buys.maxByOrNull { it.size }
            val maxSell = orderBook.result.sells.maxByOrNull { it.size }
            val message =
                "buy: total: $totalBuys, avg: $averageBuy, resist: $maxBuy // sell: total: $totalSells, avg: $averageSell, resist: $maxSell"
            System.err.println(message)
        }.result

    fun getHistory(
        symbol: String,
        resolution: Int = 300,
        startSeconds: Long = System.currentTimeMillis() / 1000 - 24 * 3600,
        endSeconds: Long = System.currentTimeMillis() / 1000
    ): List<FtxHistory> {
        val result = mutableListOf<FtxHistory>()
        var partial: FtxHistories? = null
        var cursorSeconds = endSeconds
        while (partial == null || partial.result.first().timeAsSeconds > startSeconds + resolution && partial.result.size > 1) {
            partial = _ftxHistory(symbol, resolution, startSeconds, cursorSeconds)
            System.err.println("get partial history $symbol for time $cursorSeconds, ${partial.result.size}")
            result.addAll(0, partial.result)
            if (partial.result.isNotEmpty()) {
                cursorSeconds = partial.result.first().timeAsSeconds
            }
        }
        checkHistories(result)
        return result
    }

    fun getTrades(symbol: String, startTimeSeconds: Long, endTimeSeconds: Long): List<FtxTradeEntry> {
        val trades = mutableListOf<FtxTradeEntry>()
        var partialTrades: FtxTrades? = null
        var cursorSeconds = endTimeSeconds
        while (partialTrades == null || partialTrades.result.size >= 5000) {
            partialTrades = _ftxTrades(symbol, startTimeSeconds, cursorSeconds)
            trades.addAll(0, partialTrades.result)
            System.err.println("get trades for time $cursorSeconds, ${partialTrades.result.size}")
            if (partialTrades.result.isNotEmpty()) {
                cursorSeconds = partialTrades.result.last().timeAsSeconds
            }
        }
        trades.sortBy { it.time }
        checkTrades(trades)
        return trades
    }

    private fun checkTrades(trades: List<FtxTradeEntry>) {
        (1 until trades.size).forEach { index ->
            if (trades[index - 1].time > trades[index].time) throw IllegalStateException("Unordered trades: $index")
        }
        System.err.println("check ok")
    }

    private fun checkHistories(trades: List<FtxHistory>) {
        (1 until trades.size).forEach { index ->
            if (trades[index - 1].startTime > trades[index].startTime) throw IllegalStateException("Unordered trades: $index")
        }
        System.err.println("check ok")
    }

    /**
     * @param startTimeSeconds:  la date la plus ancienne
     * @param endTimeSeconds: la date la plus recente
     *
     * Le trade le plus recent est retourné EN PREMIER (index 0) (le plus proche de endTime),
     * Ordonnés de endDate à max(5000, startTime)
     *
     * Ex: END, END-1, END-2, ..., cursor
     */
    private fun _ftxTrades(
        symbol: String,
        startTimeSeconds: Long,
        endTimeSeconds: Long
    ): FtxTrades = get<FtxTrades>("markets/$symbol/trades") {
        it
            .query("start_time", startTimeSeconds.toString())
            .query("end_time", endTimeSeconds.toString())
    }.let { ftxTrades ->
        FtxTrades(ftxTrades.success, ftxTrades.result.sortedBy { it.time })
    }

    /**
     * @param startTimeSeconds:  la date la plus ancienne
     * @param endTimeSeconds: la date la plus recente
     *
     * Le trade le plus recent est retourné EN DERNIER (index 1499) (le plus proche de endTime),
     * Ordonnés de endDate à max(1500, startTime)
     *
     * Ex: START, ..., END-2, END-1, END
     */
    private fun _ftxHistory(
        symbol: String,
        resolution: Int,
        startTimeSeconds: Long,
        endTimeSeconds: Long
    ): FtxHistories = get<FtxHistories>("/markets/$symbol/candles") {
        it
            .query("resolution", resolution.toString())
            .query("start_time", startTimeSeconds.toString())
            .query("end_time", endTimeSeconds.toString())
    }

    private inline fun <reified T : Any> get(uri: String, request: (Request) -> Request = { it }): T {
        val req = request(Request(Method.GET, "https://ftx.com/api/$uri"))
        return client.get(req)
    }


    private inline fun <reified T : Any> HttpHandler.get(req: Request): T =
        objectMapper.readValue(this(req).bodyString())
}
