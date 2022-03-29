package org.msi.ftx1.infra.remote.ftx

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request

class FtxClient(
    private val client: HttpHandler,
    private val objectMapper: ObjectMapper
) {
    fun findArbitrableMarkets(): List<Pair<String, Float>> =
        get<FtxMarkets>("markets")
            .findArbitrable()

    fun getFutureMarkets(): List<String> =
        get<FuturesMarket>("futures").result
            .filter { it.enabled && !it.expired && it.perpetual }
            .map { it.name }

    fun getSpotMarkets(): List<String> =
        get<FtxMarkets>("markets").result
            .filter { it.enabled && it.type == "spot" && it.quoteCurrency == "USD" }
            .map { it.name }

    fun getHistory(
        symbol: String,
        resolution: Int = 300,
        startSeconds: Long = System.currentTimeMillis() / 1000 - 24 * 3600,
        endSeconds: Long = System.currentTimeMillis() / 1000
    ): List<History> {
        val result = mutableListOf<History>()
        val times = mutableSetOf<Long>()
        var partial: Histories? = null
        var cursorSeconds = endSeconds
        while (partial == null || partial.result.first().timeAsSeconds > startSeconds + resolution && partial.result.size > 1) {
            partial =
                get("/markets/$symbol/candles?resolution=${resolution}&start_time=${startSeconds}&end_time=${cursorSeconds}")
            System.err.println("get partial history $symbol for time $cursorSeconds, ${partial.result.size}")
            result.addAll(partial.result)
            val intersect = times.intersect(partial.result.map { it.timeAsSeconds }.toSet())
            if (intersect.isNotEmpty()) {
                System.err.println("doublons...")

            }
            if (partial.result.isNotEmpty()) {
                cursorSeconds = partial.result.first().timeAsSeconds
            }
        }
        return result
    }

    fun getOrderBooks(symbol: String): FtxOrderBookResult =
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

    fun getTrades(symbol: String, startTimeSeconds: Long, endTimeSeconds: Long): List<FtxTradeEntry> {
        val trades = mutableListOf<FtxTradeEntry>()
        var partialTrades: FtxTrades? = null
        var cursorSeconds = endTimeSeconds
        while (partialTrades == null || partialTrades.result.size >= 5000) {
            partialTrades = ftxTrades(symbol, startTimeSeconds, cursorSeconds)
            trades.addAll(partialTrades.result)
            System.err.println("get trades for time $cursorSeconds, ${partialTrades.result.size}")
            if (partialTrades.result.isNotEmpty()) {
                cursorSeconds = partialTrades.result.last().timeAsSeconds
            }
        }
        return trades
    }

    /**
     * @param startTimeSeconds:  la date la plus ancienne
     * @param endTimeSeconds: la date la plus recente
     *
     * Le trade le plus recent est retourné en premier (le plus proche de endTime),
     * Ordonnés de endDate à max(5000, startTime)
     */
    private fun ftxTrades(
        symbol: String,
        startTimeSeconds: Long,
        endTimeSeconds: Long
    ): FtxTrades = get("markets/$symbol/trades") {
        it
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
