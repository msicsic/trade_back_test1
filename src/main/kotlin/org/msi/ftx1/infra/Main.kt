package org.msi.ftx1.infra

import org.msi.ftx1.business.*
import org.msi.ftx1.business.backtest.BackTestDemo
import java.time.LocalDateTime

fun main() {
    Config().configure().apply {
        Main(
            barChartProvider = barChartProvider,
            barChartService = barChartService,
            marketProvider = marketProvider
        ).start()
    }
}

class Main(
    private val barChartProvider: BarChartProvider,
    private val barChartService: BarChartService,
    private val marketProvider: MarketProvider
) {

    fun start() {
//
//        val fullChart = barChartProvider.getTrades(
//            symbol = "BTC-PERP",
//            startTime = LocalDateTime.now().minusDays(1),
//            endTime = LocalDateTime.now()
//        )
//
        val demo = BackTestDemo("BTC-PERP", LocalDateTime.now().minusDays(100), LocalDateTime.now(), barChartProvider)
        demo.start()

//
//        val chart = barChartProvider.getCandleChart(
//            symbol = "BTC-PERP",
//            interval = TimeFrame.MIN_15,
//            startTime = LocalDateTime.now().minusDays(360),
//            endTime = LocalDateTime.now()
//        )
//
//        val volatility = candleChartService.currentVolatility(chart)
//        System.err.println("volatility: $volatility, min / max / mean : ${chart.min} / ${chart.max} / ${chart.mean}")
//
//        val futures = marketProvider.getFutureMarkets()
//
//        System.err.println("future markets: $futures")
//        System.err.println("spot markets: " + marketProvider.getSpotMarkets())
//
//        val results = Collections.synchronizedList(mutableListOf<Pair<String, Percent>>())
//        val results2 = Collections.synchronizedList(mutableListOf<Pair<String, CandleChart>>())
//        val time = measureTimeMillis {
//            candleChartProvider.processCharts(
//                symbols = futures.map { it.name },
//                interval = CandleChartInterval.MIN_15,
//                startTime = LocalDateTime.now().minusDays(360),
//                endTime = LocalDateTime.now()
//            ) { c ->
//                results.add(Pair(c.symbol, candleChartService.currentVolatility(c)))
//                results2.add(Pair(c.symbol, c))
//            }
//        }
//        System.err.println("result : " + results.sortedByDescending { it.second })
//        System.err.println("DONE in $time ms")

        // TODO: candleChart: il faut les TS de chaque candle et une Map pour eliminer les doublons
        // TODO: coRoutines pour chargement parallele des tranches de 1500 candles

        // TODO: volatility graph (absolute volat high-low for each candle) => pour repérer les squizes
        // TODO: volatility graph (relative volat open-close for each candle)
        // TODO: generate html page with graphs (JOCL?, JCuda?, JavaFX?)

        // TODO: implement very basic strategy to bootstrap the strategy engine
    }
}
