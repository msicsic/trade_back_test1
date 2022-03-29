package org.msi.ftx1.business

import java.time.LocalDateTime

interface CandleChartProvider {

    fun processCharts(
        symbols: List<String>,
        interval: CandleChartInterval,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        candleChartConsumer: (CandleChart) -> Unit
    )

    fun getCandleChart(
        symbol: String,
        interval: CandleChartInterval,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): CandleChart

    fun getTrades(
        symbol: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): PriceChart
}
