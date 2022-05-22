package org.msi.ftx1.business

import java.time.ZonedDateTime

interface ChartsProcessor {
    fun processCharts(
        symbols: List<String>,
        interval: TimeFrame,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime,
        candleChartConsumer: (BarChart) -> Unit
    )
}

interface TradesProvider {
    fun getTrades(
        symbol: String,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ): TradeChart
}

interface CandleChartProvider {
    fun getCandleChart(
        symbol: String,
        interval: TimeFrame,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ): BarChart
}
