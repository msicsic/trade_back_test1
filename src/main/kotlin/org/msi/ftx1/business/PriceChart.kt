package org.msi.ftx1.business

data class PriceChart(
    val symbol: String,
    val startTimeSeconds: Long,
    val endTimeSeconds: Long,
    val data: List<PriceEntry>
)

data class PriceEntry(
    val timeSecondes: Long,
    val value: Float,
    val volume: Float
)
