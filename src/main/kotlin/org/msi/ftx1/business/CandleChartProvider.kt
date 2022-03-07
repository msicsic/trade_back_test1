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

    fun getFor(
        symbol: String,
        interval: CandleChartInterval,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): CandleChart
}
