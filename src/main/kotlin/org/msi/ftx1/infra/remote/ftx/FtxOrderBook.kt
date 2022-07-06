package org.msi.ftx1.infra.remote.ftx

import org.msi.ftx1.business.OrderBook
import org.msi.ftx1.business.PriceAndSize

data class FtxOrderBook(
    val success: Boolean,
    val result: FtxOrderBookResult
) {
    fun toOrderBook() = result.toOrderBook()
}

data class FtxOrderBookResult(
    val asks: List<Array<Double>>,
    val bids: List<Array<Double>>
) {
    val buys = bids.map { FtxPriceAndSize(it[0], it[1]) }
    val sells = asks.map { FtxPriceAndSize(it[0], it[1]) }

    fun toOrderBook() = OrderBook(
        buys = buys.map { it.toPriceAndSize() },
        sells = sells.map { it.toPriceAndSize() }
    )
}

data class FtxPriceAndSize(
    val price: Double,
    val size: Double
) {
    override fun toString() = """$price$ ($size)"""

    fun toPriceAndSize() = PriceAndSize(price, size)
}
