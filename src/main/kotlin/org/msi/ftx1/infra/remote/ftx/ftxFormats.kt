package org.msi.ftx1.infra.remote.ftx

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlin.math.abs


@JsonIgnoreProperties(ignoreUnknown = true)
data class FtxMarkets(
    val success: Boolean,
    val result: List<FtxMarketInfo>
) {
    fun allSpotTokens() = result.filter { it.type == "spot" }.map { it.name }

    fun tokensInBTC() = result.filter { it.currency == "BTC" && it.token !in listOf("BVOL", "IBVOL") }.map { it.token }

    fun findArbitrable(): List<Pair<String, Float>> =
        result.find { it.token == "BTC" && it.currency == "USD" }!!.let { btc ->
            tokensInBTC().map { token ->
                val inUSD = result.find { it.token == token && it.currency == "USD" }!!.price
                val inBTC = result.find { it.token == token && it.currency == "BTC" }!!.price
                val arbitraged = inBTC * btc.price
                val ecart = 100f * abs((inUSD - arbitraged) / inUSD)
                token to ecart
            }.filter {
                it.second > 0.1
            }.sortedByDescending { it.second }
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class FtxMarketInfo(
    val name: String,
    val enabled: Boolean,
    val price: Float,
    val type: String,
    val quoteCurrency: String?,
    val baseCurrency: String?
) {
    @JsonIgnore
    val token: String = name.split("/").get(0)

    @JsonIgnore
    val currency: String = name.split("/").getOrElse(1) { "NA" }
}

data class FtxOrderBook(
    val success: Boolean,
    val result: FtxOrderBookResult
)

data class FtxOrderBookResult(
    val asks: List<Array<Double>>,
    val bids: List<Array<Double>>
) {
    val buys = bids.map { PriceAndSize(it[0], it[1]) }
    val sells = asks.map { PriceAndSize(it[0], it[1]) }
}

data class PriceAndSize(
    val price: Double,
    val size: Double
) {
    override fun toString() = """$price$ ($size)"""
}

data class FtxTrades(
    val success: Boolean,
    val result: List<FtxTradeEntry>
)

data class FtxTradeEntry(
    val id: Long,
    val liquidation: Boolean,
    val price: Float,
    val side: String,
    val size: Float,
    val time: String
)

data class Histories(
    val success: Boolean,
    val result: List<History>
)

data class History(
    val startTime: String,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Double
)

data class FuturesMarket(
    val success: Boolean,
    val result: List<FutureMarket>
)

data class FutureMarket(
    val ask: Double,
    val bid: Double,
    val enabled: Boolean,
    val expired: Boolean,
    val name: String,
    val openInterest: Double,
    val openInterestUsd: Double,
    val perpetual: Boolean,
    val underlying: String,
    val type: String
)
