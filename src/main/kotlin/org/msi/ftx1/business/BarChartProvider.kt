package org.msi.ftx1.business

import java.time.LocalDateTime
import java.time.ZonedDateTime

interface BarChartProvider {

    fun processCharts(
        symbols: List<String>,
        interval: TimeFrame,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime,
        candleChartConsumer: (BarChart) -> Unit
    )

    fun getCandleChart(
        symbol: String,
        interval: TimeFrame,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ): BarChart

    fun getTrades(
        symbol: String,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ): TradeChart
}
