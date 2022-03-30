package org.msi.ftx1.business

data class TradeChart(
    val symbol: String,
    val startTimeSeconds: Long,
    val endTimeSeconds: Long,
    val data: List<Trade>
)

data class Trade(
    val timeSecondes: Long,
    val value: Double,
    val volume: Double
)
