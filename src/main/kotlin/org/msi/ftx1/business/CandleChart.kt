package org.msi.ftx1.business

enum class CandleChartInterval(val seconds: Int) {
    SEC_15(15),
    MIN_1(60),
    MIN_5(5 * 60),
    MIN_15(15 * 60),
    HOUR_1(3600),
    HOUR_4(4 * 3600),
    DAY_1(86400),
    WEEK_1(7 * 86400);

    fun canBeDownSampledTo(other: CandleChartInterval) =
        other.seconds > this.seconds && other.seconds % this.seconds == 0
}

data class CandleChart(
    val symbol: String,
    val interval: CandleChartInterval,
    val data: MutableList<CandleStick> = mutableListOf()
) {
    constructor(interval: CandleChartInterval) : this("BACKTEST", interval)

    val min get(): Double = data.minOf { it.low }
    val max get(): Double = data.maxOf { it.high }
    val mean get(): Double = data.sumOf { it.close } / data.size
    val latest get(): CandleStick = get(0)
    val oldest get(): CandleStick = get(data.size - 1)

    operator fun get(index: Int) =
        data.getOrNull(data.size - 1 - index) ?: CandleStick(latest.openTime - index * interval.seconds, interval)

    operator fun plusAssign(candle: CandleStick) {
        this.data.add(candle)
    }

    fun downSample(interval: CandleChartInterval) = if (this.interval == interval) this else CandleChart(
        symbol = this.symbol,
        interval = interval,
        data = downSampleData(interval).toMutableList()
    )

    private fun downSampleData(interval: CandleChartInterval): List<CandleStick> {
        if (!this.interval.canBeDownSampledTo(interval)) throw java.lang.IllegalArgumentException("This chart cannot be downsampled to $interval")
        val step = interval.seconds / this.interval.seconds
        return data.chunked(step).map { chunk ->
            computeBar(interval, chunk)
        }
    }

    private fun computeBar(interval: CandleChartInterval, chunk: List<CandleStick>) = CandleStick(
        openTime = chunk.first().openTime,
        volume = chunk.sumOf { it.volume.toDouble() }.toDouble(),
        interval = interval,
        open = chunk.first().open,
        close = chunk.last().close,
        high = chunk.maxOf { it.high },
        low = chunk.minOf { it.low }
    )
}

data class CandleStick(
    val openTime: Long,
    val interval: CandleChartInterval,
    var open: Double,
    var close: Double,
    var high: Double,
    var low: Double,
    var volume: Double,
    val valid: Boolean = true
) {
    val closeTime = openTime + interval.seconds

    constructor(timeSeconds: Long, interval: CandleChartInterval) : this(
        openTime = timeSeconds,
        interval = interval,
        open = 0.0,
        close = 0.0,
        high = 0.0,
        low = 0.0,
        volume = 0.0,
        valid = false
    )

    operator fun plusAssign(otherBar: CandleStick) {
        close = otherBar.close
        volume += otherBar.volume
        if (otherBar.high > high) {
            high = otherBar.high
        }
        if (otherBar.low < low) {
            low = otherBar.low
        }
    }

    operator fun plusAssign(price: Double) {
        close = price
        if (open == 0.0) {
            open = price
        }
        if (price > high) {
            high = price
        }
        if (price < low) {
            low = price
        }
    }

}
