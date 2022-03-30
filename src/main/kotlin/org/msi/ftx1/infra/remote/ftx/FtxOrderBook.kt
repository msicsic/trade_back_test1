package org.msi.ftx1.infra.remote.ftx

data class FtxOrderBook(
    val success: Boolean,
    val result: FtxOrderBookResult
)

data class FtxOrderBookResult(
    val asks: List<Array<Double>>,
    val bids: List<Array<Double>>
) {
    val buys = bids.map { PriceAndSize(it[0], it[1]) }
    val sells = asks.map { PriceAndSize(it[0], it[1]) }
}

data class PriceAndSize(
    val price: Double,
    val size: Double
) {
    override fun toString() = """$price$ ($size)"""
}
