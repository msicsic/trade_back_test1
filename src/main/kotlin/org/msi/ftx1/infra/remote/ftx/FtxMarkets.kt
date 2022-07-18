package org.msi.ftx1.infra.remote.ftx

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.msi.ftx1.business.Market
import org.msi.ftx1.business.MarketType
import kotlin.math.abs

@JsonIgnoreProperties(ignoreUnknown = true)
data class FtxMarkets(
    val success: Boolean,
    val result: List<FtxMarketInfo>
) {
    fun allSpotTokens() = result.filter { it.type == "spot" }.map { it.name }

    fun tokensInBTC() = result.filter { it.currency == "BTC" && it.token !in listOf("BVOL", "IBVOL") }.map { it.token }

    fun findArbitrable(): List<Pair<String, Double>> =
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
    val price: Double,
    val type: String,
    val quoteCurrency: String?,
    val baseCurrency: String?,
    val isEtfMarket: Boolean,
    val volumeUsd24h: Double
) {
    @JsonIgnore
    val token: String = name.split("/").get(0)

    @JsonIgnore
    val currency: String = name.split("/").getOrElse(1) { "NA" }

    fun toMarket() = Market(name, if (type == "spot") MarketType.SPOT else MarketType.FUTURE, volumeUsd24h)
}
