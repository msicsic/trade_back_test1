package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.backtest.TradeType.LONG
import org.msi.ftx1.business.backtest.TradeType.SHORT
import java.lang.Double.max
import java.lang.Double.min

enum class TradeType {
    LONG, SHORT
}

enum class CloseReason {
    EXIT, STOP_LOSS
}

/** A simulated trade record. */
data class TradeRecord(
    var feesPercent: Double,
    val type: TradeType,
    val timestamp: Long,
    val entryPrice: Double,
    val amount: Double,
    val exposure: Double,
    private var currentPrice: Double = entryPrice,
    var minPrice: Double = entryPrice,
    var maxPrice: Double = entryPrice,
    var closeReason: CloseReason? = null,
    var exitPrice: Double? = null,
) {

    val isOpen: Boolean get() = closeReason == null
    val isProfitable: Boolean get() = profitLoss > 0.0
    val fees: Double get() = feesPercent * amount * (entryPrice + currentPrice)

    val profitLoss: Double
        get() = when (type) {
            LONG -> amount * (currentPrice - entryPrice) - fees
            SHORT -> amount * (entryPrice - currentPrice) - fees
        }

    val drawDown: Double
        get() = when (type) {
            LONG -> amount * (entryPrice - minPrice) - fees
            SHORT -> amount * (entryPrice - minPrice) - fees
        }

    fun updateCurrentPrice(price: Double) {
        currentPrice = price
        minPrice = min(minPrice, price)
        maxPrice = max(maxPrice, price)
        if (shouldStop()) {
            stop()
        }
    }

    fun exit() {
        require(isOpen)
        this.closeReason = CloseReason.EXIT
        this.exitPrice = currentPrice
    }

    private fun shouldStop(): Boolean {
        require(isOpen)
        return exposure + profitLoss <= 0.0
    }

    private fun stop() {
        this.closeReason = CloseReason.STOP_LOSS
        this.exitPrice = currentPrice
    }

}
