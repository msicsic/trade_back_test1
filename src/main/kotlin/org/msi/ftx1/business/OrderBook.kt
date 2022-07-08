package org.msi.ftx1.business

data class OrderBook(
    val buys: List<PriceAndSize>,
    val sells: List<PriceAndSize>
)

data class PriceAndSize(
    val price: Double,
    val size: Double
) {
    override fun toString() = """$price$ ($size)"""
}

data class OrderBook2(
    val buys: MutableMap<Double, Double> = mutableMapOf(),
    val sells: MutableMap<Double, Double> = mutableMapOf(),
)
