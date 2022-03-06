package org.msi.ftx1.infra.remote

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
}

private val LocalDateTime.epochSecond: Long get() = atZone(ZoneId.systemDefault()).toInstant().epochSecond
