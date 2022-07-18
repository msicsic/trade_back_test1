package org.msi.ftx1.infra.remote.ftx

import org.msi.ftx1.business.Market
import org.msi.ftx1.business.MarketType

data class FtxFuturesMarket(
    val success: Boolean,
    val result: List<FtxFutureMarket>
)

data class FtxFutureMarket(
    val ask: Double,
    val bid: Double,
    val enabled: Boolean,
    val expired: Boolean,
    val name: String,
    val openInterest: Double,
    val openInterestUsd: Double,
    val perpetual: Boolean,
    val underlying: String,
    val type: String,
    val volumeUsd24h: Double
) {
    fun toMarket() = Market(name, MarketType.FUTURE, volumeUsd24h)
}
