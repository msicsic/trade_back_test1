package org.msi.ftx1.infra.remote

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import org.msi.ftx1.business.*
import org.msi.ftx1.infra.remote.ftx.FtxClient
import java.time.ZonedDateTime

class CandleChartAdapterFTX(
    private val client: FtxClient
) : CandleChartProvider, ChartsProcessor, TradesProvider, OrderBookProvider {

    override fun getTrades(symbol: String, startTime: ZonedDateTime, endTime: ZonedDateTime) = TradeChart(
        symbol = symbol,
        startTimeSeconds = startTime.seconds,
        endTimeSeconds = endTime.seconds,
        data = client.getTrades(
            symbol = symbol,
            startTimeSeconds = startTime.seconds,
            endTimeSeconds = endTime.seconds
        ).map {
            Trade(it.timeAsSeconds, it.price, it.size)
        }
    )

    override fun processCharts(
        symbols: List<String>,
        interval: TimeFrame,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime,
        candleChartConsumer: (BarChart) -> Unit
    ): Unit = runBlocking(Dispatchers.IO) {
        val symbolsChannel = produceSymbols(symbols)
        val chartsChannel = produceCharts(symbolsChannel, interval, startTime, endTime, symbols.size / 2)
        consumeCharts(chartsChannel, candleChartConsumer, symbols.size / 2)
    }

    override fun getCandleChart(
        symbol: String,
        interval: TimeFrame,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ) = BarChart(
        symbol = symbol,
        interval = interval,
        startTime = startTime,
        _data = client.getHistory(
            symbol = symbol,
            resolution = interval.seconds,
            startSeconds = startTime.seconds,
            endSeconds = endTime.seconds
        ).map {
            Bar(
                interval = interval,
                openTimeSeconds = it.timeAsSeconds,
                open = it.open,
                close = it.close,
                high = it.high,
                low = it.low,
                volume = it.volume,
            )
        }.toMutableList()
    )

    override fun getOrderBook(symbol: String): OrderBook =
        client.getOrderBook(symbol).toOrderBook()

    private fun CoroutineScope.produceSymbols(symbols: List<String>): ReceiveChannel<String> {
        val channelOut = Channel<String>()
        launch {
            symbols.forEach { channelOut.send(it) }
            channelOut.close()
        }
        return channelOut
    }

    private fun CoroutineScope.produceCharts(
        symbolsChannel: ReceiveChannel<String>,
        interval: TimeFrame,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime,
        nbCoroutines: Int
    ): ReceiveChannel<BarChart> {
        val channelOut = Channel<BarChart>()
        val jobs = mutableListOf<Job>()
        repeat(nbCoroutines) {
            jobs.add(launch {
                for (symbol in symbolsChannel) {
                    channelOut.send(getCandleChart(symbol, interval, startTime, endTime))
                }
            })
        }
        launch {
            jobs.forEach { it.join() }
            channelOut.close()
        }
        return channelOut
    }

    private fun CoroutineScope.consumeCharts(
        chartsChannel: ReceiveChannel<BarChart>,
        candleChartConsumer: (BarChart) -> Unit,
        nbCoroutines: Int
    ) {
        repeat(nbCoroutines) {
            launch {
                for (chart in chartsChannel) {
                    candleChartConsumer(chart)
                }
            }
        }
    }


}
