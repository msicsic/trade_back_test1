package org.msi.ftx1.business

import java.util.*

/** A class that receives price information and generates time series at specific time frames. */
class TimeSeriesManager(
    /** The time series from which the others are downsampled from.  */
    private val inputTimeFrame: CandleChartInterval
) {
    private val series = EnumMap<CandleChartInterval, CandleChart>(CandleChartInterval::class.java)

    init {
        series[inputTimeFrame] = CandleChart(inputTimeFrame)
    }

    fun getTimeSeries(timeframe: CandleChartInterval): CandleChart {
        require(timeframe.seconds >= inputTimeFrame.seconds) {
            "Time series can only be downsampled from the base time frame."
        }
        return series.computeIfAbsent(timeframe) { timeFrame: CandleChartInterval ->
            baseTimeSeries.downSample(timeFrame)
        }
    }

    /** Merges the bar into all time series. */
    operator fun plusAssign(bar: CandleStick) {
        require(bar.interval === inputTimeFrame)
        series.values.forEach { s ->
            s += bar
        }
    }

    private val baseTimeSeries: CandleChart
        get() = series[inputTimeFrame] ?: error("Input timeFrame should always be available")

    // Convenience methods for retrieval of series in specific time frames.

    val m5: CandleChart
        get() = getTimeSeries(CandleChartInterval.MIN_5)
    val m15: CandleChart
        get() = getTimeSeries(CandleChartInterval.MIN_15)
    val h1: CandleChart
        get() = getTimeSeries(CandleChartInterval.HOUR_1)
    val h4: CandleChart
        get() = getTimeSeries(CandleChartInterval.HOUR_4)
    val d: CandleChart
        get() = getTimeSeries(CandleChartInterval.DAY_1)
}
