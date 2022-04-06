package org.msi.ftx1.business

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

data class BarChart(
    val symbol: String,
    val interval: TimeFrame,
    val startTime: LocalDateTime,
    val _data: MutableList<Bar> = mutableListOf()
) {
    private val chartsCache = EnumMap<TimeFrame, BarChart>(TimeFrame::class.java)

    init {
        chartsCache[interval] = this
    }

    constructor(interval: TimeFrame, startTime: LocalDateTime) : this("BACKTEST", interval, startTime)

    val baseBarChart get(): BarChart = chartsCache[interval]!!
    val data get() = baseBarChart._data
    val min get(): Double = data.minOf { it.low }
    val max get(): Double = data.maxOf { it.high }
    val mean get(): Double = data.sumOf { it.close } / data.size
    val latest get(): Bar = data[data.size-1]
    val oldest get(): Bar = data[0]

    fun getDownSampledChart(timeframe: TimeFrame): BarChart {
        return chartsCache.computeIfAbsent(timeframe) { timeFrame: TimeFrame ->
            baseBarChart.downSample(timeFrame)
        }
    }

    /** Merges the bar into all time series. */
    operator fun plusAssign(candle: Bar) {
        require(candle.interval === interval)
        chartsCache.values.forEach { chart ->
            chart.addCandleStick(candle)
        }
    }

    /** Merges the bar from a lower time frame into this time series  */
    fun addCandleStick(candle: Bar) {
        // Adds a new bar if the open time is after the latest bar
        when (val currentBar = getCurrentBar(candle.openTime)) {
            null -> baseBarChart._data.add(Bar(interval, candle))
            else -> currentBar += candle
        }
    }

    private fun getCurrentBar(timestamp: Long): Bar? = latest.takeIf { it.includesTimestamp(timestamp) }

    operator fun get(index: Int) =
        data.getOrNull(data.size - 1 - index) ?: Bar(interval, latest.openTime - index * interval.seconds)

    private fun downSample(interval: TimeFrame) = if (this.interval == interval) this else BarChart(
        symbol = this.symbol,
        interval = interval,
        startTime = startTime,
        _data = downSampleData(interval).toMutableList()
    )

    private fun downSampleData(interval: TimeFrame): List<Bar> {
        if (!this.interval.canBeDownSampledTo(interval)) throw java.lang.IllegalArgumentException("This chart cannot be downsampled to $interval")
        val step = interval.seconds / this.interval.seconds
        return data.chunked(step).map { chunk ->
            computeBar(interval, chunk)
        }
    }

    private fun computeBar(interval: TimeFrame, chunk: List<Bar>) = Bar(
        openTime = chunk.first().openTime,
        volume = chunk.sumOf { it.volume },
        interval = interval,
        open = chunk.first().open,
        close = chunk.last().close,
        high = chunk.maxOf { it.high },
        low = chunk.minOf { it.low }
    )

    val m5: BarChart
        get() = getDownSampledChart(TimeFrame.MIN_5)
    val m15: BarChart
        get() = getDownSampledChart(TimeFrame.MIN_15)
    val h1: BarChart
        get() = getDownSampledChart(TimeFrame.HOUR_1)
    val h4: BarChart
        get() = getDownSampledChart(TimeFrame.HOUR_4)
    val d: BarChart
        get() = getDownSampledChart(TimeFrame.DAY_1)
}

