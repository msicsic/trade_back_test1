package org.msi.ftx1.business

import java.time.LocalDateTime

interface MarketProvider {

    fun getSpotMarkets(): List<Market>
    fun getFutureMarkets(): List<Market>
}

enum class MarketType {
    SPOT, FUTURE
}

data class Market(
    val name: String,
    val type: MarketType
)
