package org.msi.ftx1.business.indicator

import org.msi.ftx1.business.BarChart
import org.msi.ftx1.business.backtest.currentTime
import java.lang.Double.NaN
import java.time.Instant
import java.util.*

// ===========================================================
// Basic price indicators
// ===========================================================

val BarChart.openPrice: Indicator get() = simpleIndicator { index -> this[index].open }

val BarChart.highPrice: Indicator get() = simpleIndicator { index -> this[index].high }

val BarChart.lowPrice: Indicator get() = simpleIndicator { index -> this[index].low }

val BarChart.closePrice: Indicator get() = simpleIndicator { index -> this[index].close }

val BarChart.volume: Indicator get() = simpleIndicator { index -> this[index].volume }

val BarChart.hlc3Price: Indicator
    get() = simpleIndicator { index ->
        this[index].let { (it.high + it.low + it.close) / 3 }
    }

fun Indicator.withLog(name: String) = DecoratorIndicator(this) { index, value ->
    System.err.println("Indicator $name, index: $index, time: ${Instant.ofEpochMilli(currentTime)}, value : $value")
}

// ===========================================================
// Simple indicators
// ===========================================================

/**
 * Finds the lowest value of another indicator.
 *
 * Usage:
 *    val support = h1.closePrice.lowestValue(length = 20)
 */
fun Indicator.lowestValue(length: Int = 20) = simpleIndicator { index ->
    (index until index + length).filter { !this[it].isNaN() }.minOf { this[it] }
}

/**
 * Finds the highest value of another indicator.
 *
 * Usage:
 *    val resistance = h1.closePrice.highestValue(length = 20)
 */
fun Indicator.highestValue(length: Int = 20) = SimpleIndicator(this) { index ->
    (index until index + length).filter { !this[it].isNaN() }.maxOf { this[it] }
}

/**
 * Calculates the Simple Moving Average (SMA) of another indicator.
 *
 * Usage:
 *    val h1Sma30 = h1.closePrice.sma(length = 30)
 */
fun sma(indicator: Indicator, length: Int = 20) = SimpleIndicator(indicator) { index ->
    (index until index + length).map { indicator[it] }.average()
}

/**
 * Calculates the Exponential Moving Average (EMA) of another indicator.
 *
 * Usage:
 *    val h1ema30 = h1.closePrice.exponentialMovingAverage(length = 30);
 */
fun ema(indicator: Indicator, length: Int = 20): Indicator = object : AbstractIndicator(indicator) {
    val sma = sma(indicator, length)
    val k = 2.0 / (length.toDouble() + 1.0)
    var values: Array<Double> = Array(0) { NaN }

    private fun initialize() {
        System.err.println("init: ${barChart.size}")
        values = Array(barChart.size) { NaN }
        values[barChart.size - length] = sma[barChart.size - length]
        (barChart.size - length - 1 downTo 0).forEach {
            val previous = values[it + 1]
            val ema = (indicator[it] - previous) * k + previous
            values[it] = ema
        }
    }

    override fun internalGet(index: Int): Double {
        System.err.println("call with index $index, and chart size ${barChart.size}")
        if (index > barChart.size - length) return NaN
        if (barChart.size != values.size) initialize()
        return values[index]
    }
}


/**
 * Average True range (ATR) indicator.
 * {@see https://www.investopedia.com/terms/a/atr.asp}
 */
//fun BarChart.averageTrueRange(length: Int = 14): Indicator {
//    return this.trueRange().modifiedMovingAverage(length)
//}

/** True Range indicator. */
//fun BarChart.trueRange(): Indicator =
//    Indicator { index ->
//        val bar = this[index]
//        val high = bar.high
//        val low = bar.low
//        val close = this[index + 1].close
//        val ts = abs(high - low)
//        val ys = abs(high - close)
//        val yst = abs(close - low)
//        max(ts, max(ys, yst))
//    }

/**
 * The Stochastic %K price indicator.
 *
 * The %D indicator can be obtained from the %K one:
 * Indicator k = Stochastic.of(series).withLength(10);
 * Indicator d = k.smoothed();
 *
 * {@see https://www.investopedia.com/terms/s/stochasticoscillator.asp}
 */
//fun BarChart.stochastic(length: Int = 20): Indicator {
//    val closePrice = closePrice
//    val maxPrice = highPrice.highestValue(length)
//    val minPrice = lowPrice.lowestValue(length)
//
//    return Indicator { index ->
//        val price = closePrice[index]
//        val highestPrice = maxPrice[index]
//        if (highestPrice - price == 0.0) {
//            100.0
//        } else {
//            (100.0 * (price - minPrice[index]) / (highestPrice - price))
//                .coerceAtMost(100.0)
//        }
//    }
//}

/** The Stochastic Relative Strength Index (Stochastic RSI) indicator. */
//fun Indicator.stochasticRsi(length: Int = 14): Indicator {
//    val rsi = this.rsi(length = length)
//    val maxRsi = rsi.highestValue(length = length)
//    val minRsi = rsi.lowestValue(length = length)
//
//    return Indicator { index ->
//        val min = minRsi[index]
//        val max = maxRsi[index]
//        (rsi[index] - min) / (max - min)
//    }
//}

/**
 * Mean Deviation indicator.
 * {@see https://en.wikipedia.org/wiki/Average_absolute_deviation}
 */
//fun Indicator.meanDeviation(length: Int = 20): Indicator {
//    val sma = this.sma(length)
//    return Indicator { index ->
//        val average = sma[index]
//        val sumDeviations = (index until index + length)
//            .sumOf { i -> abs((this[i]) - average) }
//        return@Indicator sumDeviations / length
//    }
//}

// ===========================================================
// Stop loss indicators
// ===========================================================

/** Volatility (ATR) -based stop loss indicator.  */
//fun BarChart.volatilityStop(length: Int = 22, multiplier: Double = 2.0): Indicator {
//    val atr = this.averageTrueRange(length)
//    val price = this.lowPrice.lowestValue(length = length)
//
//    return Indicator { index ->
//        price[index] - atr[index] * multiplier
//    }
//}

/** Defines a stop loss some distance below the highest value of the previous [length] bars. */
//fun BarChart.chandelierStop(length: Int = 10, atrDistance: Double = 3.0): Indicator {
//    val atr = this.averageTrueRange(length)
//    val minPrice = highPrice.highestValue(length)
//
//    return Indicator { index ->
//        minPrice[index] - atr[index] * atrDistance
//    }
//}
