package org.msi.ftx1.infra.remote.ftx

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
