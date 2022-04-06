package org.msi.ftx1.business.indicator

import org.msi.ftx1.business.BarChart
import java.util.*


fun interface Indicator {
    fun getValue(index: Int): Double?

    //val barChart: BarChart

    operator fun get(index: Int) = getValue(index)
}

/** Convenience getter for the latest indicator value.  */
val Indicator.latestValue: Double? get() = getValue(0)

/** The value of the indicator delayed by the specified number of bars. */
fun Indicator.previousValue(bars: Int): Indicator = Indicator { index -> getValue(index + bars) }
//
//abstract class AbstractIndicator(
//    override val barChart: BarChart
//) : Indicator {
//
//    override fun toString(): String = javaClass.simpleName
//}
//
//abstract class CachedIndicator(barChart: BarChart) : AbstractIndicator(barChart) {
//    /**
//     * List of cached results
//     */
//    private var results: MutableList<Double> = mutableListOf()
//
//    /**
//     * Should always be the index of the last result in the results list. I.E. the
//     * last calculated result.
//     */
//    protected var highestResultIndex = -1
//
//
//    protected constructor(indicator: Indicator) : this(indicator.barChart)
//
//    /**
//     * @param index the bar index
//     * @return the value of the indicator
//     */
//    protected abstract fun calculate(index: Int): Double?
//
//    override fun getValue(index: Int): T? {
//        val series: BarSeries = getBarSeries()
//        if (series == null) {
//            // Series is null; the indicator doesn't need cache.
//            // (e.g. simple computation of the value)
//            // --> Calculating the value
//            val result: T = calculate(index)
//            log.trace("{}({}): {}", this, index, result)
//            return result
//        }
//
//        // Series is not null
//        val removedBarsCount: Int = series.getRemovedBarsCount()
//        val maximumResultCount: Int = series.getMaximumBarCount()
//        var result: T
//        if (index < removedBarsCount) {
//            // Result already removed from cache
//            log.trace(
//                "{}: result from bar {} already removed from cache, use {}-th instead",
//                javaClass.simpleName, index, removedBarsCount
//            )
//            increaseLengthTo(removedBarsCount, maximumResultCount)
//            highestResultIndex = removedBarsCount
//            result = results!![0]
//            if (result == null) {
//                // It should be "result = calculate(removedBarsCount);".
//                // We use "result = calculate(0);" as a workaround
//                // to fix issue #120 (https://github.com/mdeverdelhan/ta4j/issues/120).
//                result = calculate(0)
//                results!![0] = result
//            }
//        } else {
//            if (index == series.getEndIndex()) {
//                // Don't cache result if last bar
//                result = calculate(index)
//            } else {
//                increaseLengthTo(index, maximumResultCount)
//                if (index > highestResultIndex) {
//                    // Result not calculated yet
//                    highestResultIndex = index
//                    result = calculate(index)
//                    results!![results!!.size - 1] = result
//                } else {
//                    // Result covered by current cache
//                    val resultInnerIndex = results!!.size - 1 - (highestResultIndex - index)
//                    result = results!![resultInnerIndex]
//                    if (result == null) {
//                        result = calculate(index)
//                        results!![resultInnerIndex] = result
//                    }
//                }
//            }
//        }
//        log.trace("{}({}): {}", this, index, result)
//        return result
//    }
//
//    /**
//     * Increases the size of cached results buffer.
//     *
//     * @param index     the index to increase length to
//     * @param maxLength the maximum length of the results buffer
//     */
//    private open fun increaseLengthTo(index: Int, maxLength: Int) {
//        if (highestResultIndex > -1) {
//            val newResultsCount = Math.min(index - highestResultIndex, maxLength)
//            if (newResultsCount == maxLength) {
//                results!!.clear()
//                results!!.addAll(Collections.nCopies(maxLength, null))
//            } else if (newResultsCount > 0) {
//                results!!.addAll(Collections.nCopies(newResultsCount, null))
//                removeExceedingResults(maxLength)
//            }
//        } else {
//            // First use of cache
//            assert(results!!.isEmpty()) { "Cache results list should be empty" }
//            results!!.addAll(Collections.nCopies(Math.min(index + 1, maxLength), null))
//        }
//    }
//
//    /**
//     * Removes the N first results which exceed the maximum bar count. (i.e. keeps
//     * only the last maximumResultCount results)
//     *
//     * @param maximumResultCount the number of results to keep
//     */
//    private open fun removeExceedingResults(maximumResultCount: Int) {
//        val resultCount = results!!.size
//        if (resultCount > maximumResultCount) {
//            // Removing old results
//            val nbResultsToRemove = resultCount - maximumResultCount
//            for (i in 0 until nbResultsToRemove) {
//                results!!.removeAt(0)
//            }
//        }
//    }
//}
