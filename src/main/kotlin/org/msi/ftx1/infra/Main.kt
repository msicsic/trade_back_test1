package org.msi.ftx1.infra

import org.msi.ftx1.business.*
import java.time.LocalDateTime
import java.util.*
import kotlin.system.measureTimeMillis

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

        val futures = marketProvider.getFutureMarkets()

        System.err.println("future markets: $futures")
        System.err.println("spot markets: " + marketProvider.getSpotMarkets())

        val results = Collections.synchronizedList(mutableListOf<Pair<String, Percent>>())
        val time = measureTimeMillis {
            candleChartProvider.processCharts(
                symbols = futures.map { it.name },
                interval = CandleChartInterval.MIN_5,
                startTime = LocalDateTime.now().minusMinutes(5),
                endTime = LocalDateTime.now()
            ) { c ->
                results.add(Pair(c.symbol, candleChartService.currentVolatility(c)))
            }
        }
        System.err.println("DONE in $time ms")
        System.err.println("result : " + results.sortedByDescending { it.second })

        // TODO: find max volatility for last X mins (use coroutines for fast calls)

        // TODO: implement very basic strategy to bootstrap the strategy engine
    }
}
