package org.msi.ftx1.business.indicator

import org.msi.ftx1.business.BarChart


interface Indicator {
    fun internalGet(index: Int): Double

    val barChart: BarChart

    fun before(index: Int) {}
    fun after(index: Int, value: Double) {}

    operator fun get(index: Int): Double {
        before(index)
        val value = internalGet(index)
        after(index, value)
        return value
    }
}

/** Convenience getter for the latest indicator value.  */
val Indicator.latestValue: Double get() = get(0)

/** The value of the indicator delayed by the specified number of bars. */
fun Indicator.previousValue(bars: Int): Indicator = SimpleIndicator(this) { index -> get(index + bars) }

fun Indicator.simpleIndicator(getter: (Int) -> Double) = SimpleIndicator(this, getter)
fun BarChart.simpleIndicator(getter: (Int) -> Double) = SimpleCharIndicator(this, getter)

abstract class AbstractIndicator(
    override val barChart: BarChart
) : Indicator {

    constructor(indicator: Indicator) : this(indicator.barChart)

    override fun toString(): String = javaClass.simpleName
}

class SimpleCharIndicator(barChart: BarChart, val getter: (index: Int) -> Double) : AbstractIndicator(barChart) {
    override fun internalGet(index: Int) = getter(index)
}

open class SimpleIndicator(indicator: Indicator, val getter: (index: Int) -> Double) :
    AbstractIndicator(indicator.barChart) {
    override fun internalGet(index: Int) = getter(index)
}

class DecoratorIndicator(private val other: Indicator, private val afterAction: (index: Int, value: Double) -> Unit ) : AbstractIndicator(other.barChart) {
    var lastIndex = -1
    override fun internalGet(index: Int) = other[index]
    override fun after(index: Int, value: Double) {
        if (lastIndex != index) {
            lastIndex = index
            afterAction(index, value)
        }
    }
}
