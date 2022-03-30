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
