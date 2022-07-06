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
