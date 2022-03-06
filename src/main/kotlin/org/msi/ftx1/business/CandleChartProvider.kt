package org.msi.ftx1.business

import java.time.LocalDateTime

interface CandleChartProvider {

    fun getFor(symbol: String, interval: CandleChartInterval, startTime: LocalDateTime, endTime: LocalDateTime): CandleChart
}
