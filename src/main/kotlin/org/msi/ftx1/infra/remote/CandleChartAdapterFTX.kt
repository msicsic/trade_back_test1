package org.msi.ftx1.infra.remote

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import org.msi.ftx1.business.CandleChart
import org.msi.ftx1.business.CandleChartInterval
import org.msi.ftx1.business.CandleChartProvider
import org.msi.ftx1.business.CandleStick
import org.msi.ftx1.infra.remote.ftx.FtxClient
import java.time.LocalDateTime
import java.time.ZoneId

class CandleChartAdapterFTX(
    val client: FtxClient
) : CandleChartProvider {

    override fun processCharts(
        symbols: List<String>,
        interval: CandleChartInterval,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        candleChartConsumer: (CandleChart) -> Unit
    ): Unit = runBlocking(Dispatchers.IO) {
        val symbolsChannel = produceSymbols(symbols)
        val chartsChannel = produceCharts(symbolsChannel, interval, startTime, endTime, symbols.size/2)
        consumeCharts(chartsChannel, candleChartConsumer, symbols.size/2)
    }

    override fun getFor(
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
                    channelOut.send(getFor(symbol, interval, startTime, endTime))
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
