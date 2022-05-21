package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.backtest.TradeType.LONG
import org.msi.ftx1.business.backtest.TradeType.SHORT
import java.lang.Double.max
import java.lang.Double.min

enum class TradeType {
    LONG, SHORT
}

enum class CloseReason {
    TP, SL
}

/** A simulated trade record. */
data class TradeRecord(
    val balanceExposurePercent: Double,
    val maxLever: Double,
    val feesPercentPerSide: Double,
    val type: TradeType,
    val timestamp: Long,
    val balanceIn: Double,
    val entryPrice: Double,
    val initialStopLoss: Double?,
) {
    private var currentPrice: Double = entryPrice
    private var currentTime: Long = timestamp
    var minPrice: Double = entryPrice
    var maxPrice: Double = entryPrice
    var closeReason: CloseReason? = null
    var exitPrice: Double? = null
    var exitTimestamp: Long? = null
    val amount: Double = min(balanceIn, maxLever*stopLossPercent*balanceIn) / currentPrice
    val feesPercent: Double = feesPercentPerSide

    val entryFees: Double get() = amount * entryPrice * feesPercent
    val exitFees: Double get() = amount * currentPrice * feesPercent
    val stopLossPercent: Double get() = balanceExposurePercent
    val volatility: Double get() = ((exitPrice ?: 0.0) - entryPrice) / entryPrice
    val exposure: Double get() = balanceExposurePercent*balanceIn*maxLever
    val isOpen: Boolean get() = closeReason == null
    val isProfitable: Boolean get() = profitLoss > 0.0
    val fees: Double get() = entryFees + exitFees
    val locked: Double get() = amount * entryPrice / maxLever
    val balanceOut: Double get() = balanceIn + profitLoss
    val stopLoss: Double get() = initialStopLoss ?: when(type) {
        LONG -> entryPrice*(1-stopLossPercent)
        SHORT -> entryPrice*(1+stopLossPercent)
    }

    val lever: Double get() = exposure / locked
    val tradeProfitPercent: Double get() = profitLoss / locked
    val balanceProfitPercent: Double get() = 1- (balanceIn / balanceOut)
    val profitLoss: Double
        get() = when (type) {
            LONG -> amount * (currentPrice - entryPrice) - fees
            SHORT -> amount * (entryPrice - currentPrice) - fees
        }

    val riskRatio: Double
        get() = profitLoss / exposure * maxLever

    val rawProfitLoss: Double
        get() = when (type) {
            LONG -> amount * (currentPrice - entryPrice)
            SHORT -> amount * (entryPrice - currentPrice)
        }

    val drawDown: Double
        get() = when (type) {
            LONG -> amount * (entryPrice - minPrice) - fees
            SHORT -> amount * (entryPrice - minPrice) - fees
        }

    fun updateCurrentPrice(price: Double, time: Long) {
        currentPrice = price
        currentTime = time
        if (shouldStop()) {
            System.err.println("STOP LOSS")
            currentPrice = stopLoss
            exit(true)
        }
        minPrice = min(minPrice, currentPrice)
        maxPrice = max(maxPrice, currentPrice)
    }

    fun exit(stopLoss: Boolean) {
        require(isOpen)
        this.closeReason = if (stopLoss) CloseReason.SL else CloseReason.TP
        this.exitPrice = currentPrice
        this.exitTimestamp = currentTime
    }

    private fun shouldStop(): Boolean {
        require(isOpen)
        return currentPrice <= stopLoss
    }
}
