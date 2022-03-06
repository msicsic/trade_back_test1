package org.msi.ftx1.business

enum class CandleChartInterval(val seconds: Int) {
    SEC_15(15),
    MIN_1(60),
    MIN_5(5 * 60),
    MIN_10(10 * 60),
    MIN_15(15 * 60),
    HOUR_1(3600),
    HOUR_4(4 * 3600),
    DAY_1(86400),
    WEEK_1(7 * 86400)
}

data class CandleChart(
    val symbol: String,
    val interval: CandleChartInterval,
    val startTimeSeconds: Long,
    val endTimeSeconds: Long,
    val data: List<CandleStick>
) {
    val min get(): Float = data.minOf { it.low }
    val max get(): Float = data.maxOf { it.high }
    val mean get(): Float = (data.sumOf { it.close.toDouble() } / data.size).toFloat()
    val last get(): Float = data.last().close
}

data class CandleStick(
    val open: Float,
    val close: Float,
    val high: Float,
    val low: Float,
    val volume: Float
) {
    constructor(
        open: Double,
        close: Double,
        high: Double,
        low: Double,
        volume: Double
    ) : this(open.toFloat(), close.toFloat(), high.toFloat(), low.toFloat(), volume.toFloat())
}
