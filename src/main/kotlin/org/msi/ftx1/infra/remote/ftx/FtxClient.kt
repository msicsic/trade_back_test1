package org.msi.ftx1.infra.remote.ftx

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class FtxClient(
    private val client: HttpHandler,
    private val objectMapper: ObjectMapper
) {
    private val dateParser: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'+00:00'")

    fun findArbitrableMarkets(): List<Pair<String, Float>> =
        get<FtxMarkets>("markets")
            .findArbitrable()

    fun getFutureMarkets(): List<String> =
        get<FuturesMarket>("futures").result
            .filter { it.enabled && !it.expired && it.perpetual}
            .map { it.name }

    fun getSpotMarkets(): List<String> =
        get<FtxMarkets>("markets").result
            .filter { it.enabled && it.type == "spot" && it.quoteCurrency == "USD"}
            .map { it.name }

    fun getHistory(symbol: String, resolution: Int = 300, startSeconds: Long = System.currentTimeMillis()/1000 - 24*3600, endSeconds: Long = System.currentTimeMillis()/1000): List<History> =
        get<Histories>("/markets/$symbol/candles?resolution=$resolution&start_time=$startSeconds&end_time=$endSeconds")
            .result

    fun getOrderBooks(symbol: String): FtxOrderBookResult =
        get<FtxOrderBook>("markets/$symbol/orderbook?depth=100").also {
            val totalBuys = it.result.buys.sumOf { it.size }
            val totalSells = it.result.sells.sumOf { it.size }
            val averageBuy = it.result.buys.sumOf { it.size * it.price } / totalBuys
            val averageSell = it.result.sells.sumOf { it.size * it.price } / totalSells
            val maxBuy = it.result.buys.maxByOrNull { it.size }
            val maxSell = it.result.sells.maxByOrNull { it.size }
            val message =
                "buy: total: $totalBuys, avg: $averageBuy, resist: $maxBuy // sell: total: $totalSells, avg: $averageSell, resist: $maxSell"
            System.err.println(message)
        }.result

    fun getTrades(symbol: String, startTimeSeconds: Long, endTimeSeconds: Long): List<FtxTradeEntry> {
        val trades = mutableListOf<FtxTradeEntry>()
        var partialTrades: FtxTrades? = null
        var cursorSeconds = endTimeSeconds
        while (partialTrades == null || partialTrades.result.size >= 5000) {
            partialTrades = get("markets/$symbol/trades") {
                query("start_time", startTimeSeconds.toString())
                query("end_time", cursorSeconds.toString())
            }
            trades.addAll(partialTrades.result)
            System.err.println("get trades for time $cursorSeconds, ${partialTrades.result.size}")
            if (partialTrades.result.isNotEmpty()) {
                cursorSeconds = partialTrades.result.last()
                    .let { LocalDateTime.parse(it.time, dateParser).toEpochSecond(ZoneOffset.UTC) }
            }
        }
        return trades
    }

    private inline fun <reified T : Any> get(uri: String, request: Request.() -> Request = { this }): T {
        val req = request(Request(Method.GET, "https://ftx.com/api/$uri"))
        return client.get(req)
    }

    private inline fun <reified T : Any> HttpHandler.get(req: Request): T =
        objectMapper.readValue(this(req).bodyString())
}
