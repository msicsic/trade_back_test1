package org.msi.ftx1.business.indicator

import org.msi.ftx1.business.BarChart
import org.msi.ftx1.business.backtest.currentTime
import java.time.Instant
import kotlin.math.abs
import kotlin.math.max

// ===========================================================
// Basic price indicators
// ===========================================================

// TODO: use NaN instead of null
// TODO: UT for EMA (compare with tradingView)

val BarChart.openPrice: Indicator
    get() = Indicator { index -> this[index]?.open }

val BarChart.highPrice: Indicator
    get() = Indicator { index -> this[index]?.high }

val BarChart.lowPrice: Indicator
    get() = Indicator { index -> this[index]?.low }

val BarChart.closePrice: Indicator
    get() = Indicator { index -> this[index]?.close }

val BarChart.volume: Indicator
    get() = Indicator { index -> this[index]?.volume }

val BarChart.hlc3Price: Indicator
    get() = typicalPrice

val BarChart.typicalPrice: Indicator
    get() = Indicator { index ->
        this[index]?.let {
            (it.high + it.low + it.close) / 3
        }
    }

fun Indicator.withLog(name: String) = Indicator { index ->
    val res = this[index]
    if (index == 0) { System.err.println("Indicator $name, index: $index, time: ${Instant.ofEpochMilli(currentTime)}, value : $res") }
    res
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
fun Indicator.lowestValue(length: Int = 20): Indicator =
    Indicator { index ->
        (index until index + length).mapNotNull { this[it] }.minOrNull()
    }

/**
 * Finds the highest value of another indicator.
 *
 * Usage:
 *    val resistance = h1.closePrice.highestValue(length = 20)
 */
fun Indicator.highestValue(length: Int = 20): Indicator =
    Indicator { index ->
        (index until index + length).mapNotNull { this[it] }.maxOrNull()
    }

/**
 * Calculates the Simple Moving Average (SMA) of another indicator.
 *
 * Usage:
 *    val h1Sma30 = h1.closePrice.sma(length = 30)
 */
fun Indicator.simpleMovingAverage(length: Int = 20) =
    Indicator { index ->
        (index until index + length).mapNotNull { i -> this[i] }.average()
    }

/** Alias to Indicator.simpleMovingAverage(length). */
fun Indicator.sma(length: Int = 20) = simpleMovingAverage(length)

/**
 * Calculates the Exponential Moving Average (EMA) of another indicator.
 *
 * Usage:
 *    val h1ema30 = h1.closePrice.exponentialMovingAverage(length = 30);
 */
fun Indicator.exponentialMovingAverage(length: Int = 20): Indicator {
    val sma = this.simpleMovingAverage(length)
    val multiplier = 2.0 / (length.toDouble() + 1.0)

    fun valueAt(index: Int, relevantBars: Int): Double? {
        if (relevantBars == 0) {
            return sma[index]
        }
        val smaAtIndex = sma[index] ?: return null
        // TODO: Implement memoization of the previous value (however the index moves with every new bar)
        val previousValue = valueAt(index = index + 1, relevantBars = relevantBars - 1) ?: return null
        return (smaAtIndex - previousValue) * multiplier + previousValue
    }

    return Indicator { index ->
        valueAt(index, (length * 2).coerceAtLeast(30))
    }
}

/** Alias to Indicator.exponentialMovingAverage(length). */
fun Indicator.ema(length: Int = 20) = exponentialMovingAverage(length)

/**
 * Calculates the Modified Moving Average (MMA, RMA, or SMMA) of another indicator.
 *
 * Usage:
 *    val mma30 = h1.closePrice.modifiedMovingAverage(length = 30);
 */
fun Indicator.modifiedMovingAverage(length: Int = 14): Indicator {
    val sma = this.simpleMovingAverage(length)
    val multiplier = 1.0 / length.toDouble()

    fun valueAt(index: Int, relevantBars: Int): Double? {
        if (relevantBars == 0) {
            return sma[index]
        }
        val smaAtIndex = sma[index] ?: return null
        // TODO: Implement memoization of the previous value (however the index moves with every new bar)
        val previousValue = valueAt(index = index + 1, relevantBars = relevantBars - 1) ?: return null
        return (smaAtIndex - previousValue) * multiplier + previousValue
    }

    return Indicator { index ->
        valueAt(index, (length * 2).coerceAtLeast(30))
    }
}

/**
 * Average True range (ATR) indicator.
 * {@see https://www.investopedia.com/terms/a/atr.asp}
 */
fun BarChart.averageTrueRange(length: Int = 14): Indicator {
    return this.trueRange().modifiedMovingAverage(length)
}

/** True Range indicator. */
fun BarChart.trueRange(): Indicator =
    Indicator { index ->
        val bar = this[index] ?: return@Indicator null
        val high = bar.high
        val low = bar.low
        val close = this[index + 1]?.close ?: return@Indicator null
        val ts = abs(high - low)
        val ys = abs(high - close)
        val yst = abs(close - low)
        return@Indicator max(ts, max(ys, yst))
    }

/**
 * The Stochastic %K price indicator.
 *
 * The %D indicator can be obtained from the %K one:
 * Indicator k = Stochastic.of(series).withLength(10);
 * Indicator d = k.smoothed();
 *
 * {@see https://www.investopedia.com/terms/s/stochasticoscillator.asp}
 */
fun BarChart.stochastic(length: Int = 20): Indicator {
    val closePrice = closePrice
    val maxPrice = highPrice.highestValue(length)
    val minPrice = lowPrice.lowestValue(length)

    return Indicator { index ->
        val price = closePrice[index] ?: return@Indicator null
        val highestPrice = maxPrice[index] ?: return@Indicator null
        if (highestPrice - price == 0.0) {
            100.0
        } else {
            val minPrice = minPrice[index] ?: return@Indicator null
            (100.0 * (price - minPrice) / (highestPrice - price))
                .coerceAtMost(100.0)
        }
    }
}

/** The Stochastic Relative Strength Index (Stochastic RSI) indicator. */
fun Indicator.stochasticRsi(length: Int = 14): Indicator {
    val rsi = this.rsi(length = length)
    val maxRsi = rsi.highestValue(length = length)
    val minRsi = rsi.lowestValue(length = length)

    return Indicator { index ->
        val min = minRsi[index] ?: return@Indicator null
        val max = maxRsi[index] ?: return@Indicator null
        val rsi = rsi[index] ?: return@Indicator null
        (rsi - min) / (max - min)
    }
}

/**
 * Mean Deviation indicator.
 * {@see https://en.wikipedia.org/wiki/Average_absolute_deviation}
 */
fun Indicator.meanDeviation(length: Int = 20): Indicator {
    val sma = this.simpleMovingAverage(length)
    return Indicator { index ->
        val average = sma[index] ?: return@Indicator null
        val sumDeviations = (index until index + length)
            .sumOf { i -> abs((this[i] ?: return@Indicator null) - average) }
        return@Indicator sumDeviations / length
    }
}

// ===========================================================
// Stop loss indicators
// ===========================================================

/** Volatility (ATR) -based stop loss indicator.  */
fun BarChart.volatilityStop(length: Int = 22, multiplier: Double = 2.0): Indicator {
    val atr = this.averageTrueRange(length)
    val price = this.lowPrice.lowestValue(length = length)

    return Indicator { index ->
        (price[index] ?: return@Indicator null) - (atr[index] ?: return@Indicator null) * multiplier
    }
}

/** Defines a stop loss some distance below the highest value of the previous [length] bars. */
fun BarChart.chandelierStop(length: Int = 10, atrDistance: Double = 3.0): Indicator {
    val atr = this.averageTrueRange(length)
    val minPrice = highPrice.highestValue(length)

    return Indicator { index ->
        (minPrice[index]?: return@Indicator null) - (atr[index]?: return@Indicator null) * atrDistance
    }
}
