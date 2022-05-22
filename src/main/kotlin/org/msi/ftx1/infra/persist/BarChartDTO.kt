package org.msi.ftx1.infra.persist

import org.msi.ftx1.business.Bar
import org.msi.ftx1.business.BarChart
import org.msi.ftx1.business.TimeFrame
import java.time.ZonedDateTime

data class BarChartDTO(
    val symbol: String,
    val interval: TimeFrame,
    val startTime: ZonedDateTime,
    val data: List<BarDTO>
) {
    fun toBO() = BarChart(
        symbol = this.symbol,
        interval = this.interval,
        startTime = this.startTime,
        _data = this.data.map { it.toBO() }.toMutableList(),
    )
}

fun BarChart.toDTO() = BarChartDTO(
    symbol = this.symbol,
    interval = this.interval,
    startTime = this.startTime,
    data = this.data.map { it.toDTO() },
)

data class BarDTO(
    val interval: TimeFrame,
    val openTime: Long,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Double
) {
    fun toBO() = Bar(
        interval = interval,
        openTime = openTime,
        open = open,
        close = close,
        high = high,
        low = low,
        volume = volume
    )
}

fun Bar.toDTO() = BarDTO(
    interval = interval,
    openTime = openTime,
    open = open,
    close = close,
    high = high,
    low = low,
    volume = volume
)
