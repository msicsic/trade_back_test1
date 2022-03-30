package org.msi.ftx1.infra.remote.ftx

data class FtxOrderBook(
    val success: Boolean,
    val result: FtxOrderBookResult
)

data class FtxOrderBookResult(
    val asks: List<Array<Double>>,
    val bids: List<Array<Double>>
) {
    val buys = bids.map { FtxPriceAndSize(it[0], it[1]) }
    val sells = asks.map { FtxPriceAndSize(it[0], it[1]) }
}

data class FtxPriceAndSize(
    val price: Double,
    val size: Double
) {
    override fun toString() = """$price$ ($size)"""
}
