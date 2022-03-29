package org.msi.ftx1.infra.remote

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import org.msi.ftx1.business.*
import org.msi.ftx1.infra.remote.ftx.FtxClient
import java.time.LocalDateTime
import java.time.ZoneId

class CandleChartAdapterFTX(
    private val client: FtxClient
) : CandleChartProvider {

    override fun getTrades(symbol: String, startTime: LocalDateTime, endTime: LocalDateTime) = PriceChart(
        symbol = symbol,
        startTimeSeconds = startTime.epochSecond,
        endTimeSeconds = endTime.epochSecond,
        data = client.getTrades(
            symbol = symbol,
            startTimeSeconds = startTime.epochSecond,
            endTimeSeconds = endTime.epochSecond
        ).map {
            PriceEntry(it.timeAsSeconds, it.price, it.size)
        }
    )

    override fun processCharts(
        symbols: List<String>,
        interval: CandleChartInterval,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        candleChartConsumer: (CandleChart) -> Unit
    ): Unit = runBlocking(Dispatchers.IO) {
        val symbolsChannel = produceSymbols(symbols)
        val chartsChannel = produceCharts(symbolsChannel, interval, startTime, endTime, symbols.size / 2)
        consumeCharts(chartsChannel, candleChartConsumer, symbols.size / 2)
    }

    override fun getCandleChart(
        symbol: String,
        interval: CandleChartInterval,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): CandleChart =
        client.getHistory(
            symbol = symbol, resolution = interval.seconds,
            startSeconds = startTime.epochSecond,
            endSeconds = endTime.epochSecond
        ).map {
            CandleStick(
                timeSeconds = it.timeAsSeconds,
                interval = interval,
                open = it.open,
                close = it.close,
                high = it.high,
                low = it.low,
                volume = it.volume
            )
        }.let {
            CandleChart(
                symbol = symbol,
                interval = interval,
                startTimeSeconds = startTime.epochSecond,
                endTimeSeconds = endTime.epochSecond,
                data = it
            )
        }

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
        interval: CandleChartInterval,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        nbCoroutines: Int
    ): ReceiveChannel<CandleChart> {
        val channelOut = Channel<CandleChart>()
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
        chartsChannel: ReceiveChannel<CandleChart>,
        candleChartConsumer: (CandleChart) -> Unit,
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
