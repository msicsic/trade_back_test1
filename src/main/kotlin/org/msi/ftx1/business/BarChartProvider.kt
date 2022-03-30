package org.msi.ftx1.business

import java.time.LocalDateTime

interface BarChartProvider {

    fun processCharts(
        symbols: List<String>,
        interval: TimeFrame,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        candleChartConsumer: (BarChart) -> Unit
    )

    fun getCandleChart(
        symbol: String,
        interval: TimeFrame,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): BarChart

    fun getTrades(
        symbol: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): TradeChart
}
