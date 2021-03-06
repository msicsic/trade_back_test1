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

interface TradesHistoryProvider {
    fun getTrades(
        symbol: String,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ): TradeHistory
}

interface CandleChartProvider {
    fun getCandleChart(
        symbol: String,
        interval: TimeFrame,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ): BarChart
}

interface OrderBookProvider {
    fun getOrderBook(
        symbol: String
    ): OrderBook
}

