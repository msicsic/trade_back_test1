package org.msi.ftx1.business

data class Percent(val value: Float) {
    infix operator fun plus(other: Percent) = value + other.value
    infix operator fun minus(other: Percent) = value - other.value
    infix operator fun div(other: Percent) = value / other.value

    override fun toString() = "${value*100}%"
}
