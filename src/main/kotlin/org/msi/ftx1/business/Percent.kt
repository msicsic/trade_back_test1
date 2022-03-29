package org.msi.ftx1.business

class Percent(val value: Double) : Comparable<Percent> {
    infix operator fun plus(other: Percent) = value + other.value
    infix operator fun minus(other: Percent) = value - other.value
    infix operator fun div(other: Percent) = value / other.value

    override fun toString() = "${value*100}%"

    override fun compareTo(other: Percent) = this.value.compareTo(other.value)
}
