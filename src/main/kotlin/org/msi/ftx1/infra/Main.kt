package org.msi.ftx1.infra

import org.msi.ftx1.business.CandleChartInterval
import org.msi.ftx1.business.CandleChartProvider
import org.msi.ftx1.business.CandleChartService
import org.msi.ftx1.business.MarketProvider
import java.time.LocalDateTime

fun main() {
    Config().configure().apply {
        Main(
            candleChartProvider = candleChartProvider,
            candleChartService = candleChartService,
            marketProvider = marketProvider
        ).start()
    }
}

class Main(
    private val candleChartProvider: CandleChartProvider,
    private val candleChartService: CandleChartService,
    private val marketProvider: MarketProvider
) {

    fun start() {

        val chart = candleChartProvider.getFor(
            symbol = "BTC-PERP",
            interval = CandleChartInterval.DAY_1,
            startTime = LocalDateTime.now().minusDays(360),
            endTime = LocalDateTime.now()
        )

        val volatility = candleChartService.currentVolatility(chart)
        System.err.println("volatility: $volatility, min / max / mean : ${chart.min} / ${chart.max} / ${chart.mean}")

        System.err.println("future markets: "+marketProvider.getFutureMarkets())
        System.err.println("spot markets: "+marketProvider.getSpotMarkets())

        // TODO: find max volatility for last X mins (use coroutines for fast calls)

        // TODO: implement very basic strategy to bootstrap the strategy engine
    }
}
