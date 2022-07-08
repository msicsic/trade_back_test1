package org.msi.ftx1.infra

import com.fasterxml.jackson.databind.ObjectMapper
import org.msi.ftx1.business.BarChartService
import org.msi.ftx1.business.CandleChartProvider
import org.msi.ftx1.business.MarketProvider
import org.msi.ftx1.business.OrderBookProvider
import org.msi.ftx1.business.backtest.BackTestDemo
import org.msi.ftx1.infra.remote.ftx.FtxSSeClient
import java.time.ZoneId
import java.time.ZonedDateTime

fun main() {
    Config().configure().apply {
        Main(
            candleChartProvider = candleChartProvider,
            orderBookProvider = orderBookProvider,
            objectMapper = objectMapper
        ).start()
    }
}

class Main(
    private val candleChartProvider: CandleChartProvider,
    private val orderBookProvider: OrderBookProvider,
    private val objectMapper: ObjectMapper
    // private val barChartService: BarChartService,
    // private val marketProvider: MarketProvider
) {

    fun start() {
//
//        val fullChart = barChartProvider.getTrades(
//            symbol = "BTC-PERP",
//            startTime = LocalDateTime.now().minusDays(1),
//            endTime = LocalDateTime.now()
//        )
//
        val symbol = "BTC-PERP"

        val recentTime = ZonedDateTime.of(2022, 5, 15, 0, 0, 0, 0, ZoneId.systemDefault())
        val fromTime = recentTime.minusDays(1)
        val demo = BackTestDemo(symbol, fromTime, recentTime, candleChartProvider, orderBookProvider)
        val ftxSSeClient = FtxSSeClient(objectMapper)
        ftxSSeClient.start()
        ftxSSeClient.registerOrderBook(symbol)
        demo.start()

//
//        val chart = barChartProvider.getCandleChart(
//            symbol = "BTC-PERP",
//            interval = TimeFrame.MIN_15,
//            startTime = LocalDateTime.now().minusDays(360),
//            endTime = LocalDateTime.now()
//        )
//
//        val volatility = candleChartServFice.currentVolatility(chart)
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
    }
}
