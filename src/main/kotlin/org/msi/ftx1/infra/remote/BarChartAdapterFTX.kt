package org.msi.ftx1.infra.remote

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import org.msi.ftx1.business.*
import org.msi.ftx1.infra.remote.ftx.FtxClient
import java.time.LocalDateTime
import java.time.ZoneId

class BarChartAdapterFTX(
    private val client: FtxClient
) : BarChartProvider {

    override fun getTrades(symbol: String, startTime: LocalDateTime, endTime: LocalDateTime) = TradeChart(
        symbol = symbol,
        startTimeSeconds = startTime.epochSecond,
        endTimeSeconds = endTime.epochSecond,
        data = client.getTrades(
            symbol = symbol,
            startTimeSeconds = startTime.epochSecond,
            endTimeSeconds = endTime.epochSecond
        ).map {
            Trade(it.timeAsSeconds, it.price, it.size)
        }
    )

    override fun processCharts(
        symbols: List<String>,
        interval: TimeFrame,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        candleChartConsumer: (BarChart) -> Unit
    ): Unit = runBlocking(Dispatchers.IO) {
        val symbolsChannel = produceSymbols(symbols)
        val chartsChannel = produceCharts(symbolsChannel, interval, startTime, endTime, symbols.size / 2)
        consumeCharts(chartsChannel, candleChartConsumer, symbols.size / 2)
    }

    override fun getCandleChart(
        symbol: String,
        interval: TimeFrame,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ) = BarChart(
        symbol = symbol,
        interval = interval,
        startTime = startTime,
        _data = client.getHistory(
            symbol = symbol,
            resolution = interval.seconds,
            startSeconds = startTime.epochSecond,
            endSeconds = endTime.epochSecond
        ).map {
            Bar(
                interval = interval,
                openTime = it.timeAsSeconds,
                open = it.open,
                close = it.close,
                high = it.high,
                low = it.low,
                volume = it.volume,
                valid = true
            )
        }.toMutableList()
    )

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
        startTime: LocalDateTime,
        endTime: LocalDateTime,
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

private val LocalDateTime.epochSecond: Long get() = atZone(ZoneId.systemDefault()).toInstant().epochSecond
