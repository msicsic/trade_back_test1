package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.backtest.TradeType.LONG
import org.msi.ftx1.business.backtest.TradeType.SHORT
import java.lang.Double.max
import java.lang.Double.min
import kotlin.math.abs

enum class TradeType {
    LONG, SHORT
}

enum class CloseReason {
    TP, SL
}

/** A simulated trade record. */
data class TradeRecord(
    val maxBalanceExposurePercent: Double,
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
    val feesPercent: Double = feesPercentPerSide

    val stopLoss: Double get() = initialStopLoss ?: when(type) {
        LONG -> entryPrice*(1-maxBalanceExposurePercent)
        SHORT -> entryPrice*(1+maxBalanceExposurePercent)
    }
    val stopLossPercent = abs(entryPrice-stopLoss)/entryPrice
    val thoriqRiskValue = maxBalanceExposurePercent*balanceIn
    val theoriqTrade = thoriqRiskValue / stopLossPercent
    val realTrade = min(balanceIn*maxLever, theoriqTrade)
    val riskValue = stopLossPercent*realTrade
    val quantity = realTrade / entryPrice
    val lever = realTrade / balanceIn
    val entryFees: Double get() = quantity * entryPrice * feesPercent
    val exitFees: Double get() = quantity * currentPrice * feesPercent
    val volatility: Double get() = ((exitPrice ?: entryPrice) - entryPrice) / entryPrice

    val exposure: Double get() = realTrade
    val isOpen: Boolean get() = closeReason == null
    val isProfitable: Boolean get() = profitLoss > 0.0
    val fees: Double get() = entryFees + exitFees
    val locked: Double get() = quantity * entryPrice / maxLever
    val balanceOut: Double get() = balanceIn + profitLoss


    val tradeProfitPercent: Double get() = profitLoss / locked
    val balanceProfitPercent: Double get() = 1- (balanceIn / balanceOut)
    val profitLoss: Double
        get() = when (type) {
            LONG -> quantity * (currentPrice - entryPrice) - fees
            SHORT -> quantity * (entryPrice - currentPrice) - fees
        }

    val riskRatio: Double
        get() = profitLoss / exposure * maxLever

    val rawProfitLoss: Double
        get() = when (type) {
            LONG -> quantity * (currentPrice - entryPrice)
            SHORT -> quantity * (entryPrice - currentPrice)
        }

    val drawDown: Double
        get() = when (type) {
            LONG -> quantity * (entryPrice - minPrice) - fees
            SHORT -> quantity * (entryPrice - minPrice) - fees
        }

    fun updateCurrentPrice(price: Double, time: Long) {
        currentPrice = price
        currentTime = time
        if (shouldStop()) {
            currentPrice = stopLoss
            exit(true)
        }
        minPrice = min(minPrice, currentPrice)
        maxPrice = max(maxPrice, currentPrice)
    }

    fun exit() {
        exit(false)
    }

    private fun exit(stopLoss: Boolean) {
        require(isOpen)
        this.closeReason = if (stopLoss) CloseReason.SL else CloseReason.TP
        this.exitPrice = currentPrice
        this.exitTimestamp = currentTime
    }

    private fun shouldStop(): Boolean {
        require(isOpen)
        return when(type) {
            LONG -> currentPrice <= stopLoss
            SHORT -> currentPrice >= stopLoss
        }
    }
}
