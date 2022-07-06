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
class TradeRecord(
    var open: Boolean = false,
    val maxBalanceExposurePercent: Double,
    val maxLever: Double,
    val feesPercentPerSide: Double,
    val type: TradeType,
    val timestamp: Long,
    val balanceIn: Double,
    val entryPrice: Double,
    stopLoss: Double?,
) {
    private var minPrice: Double = entryPrice
    private var maxPrice: Double = entryPrice
    private var currentClose: Double = entryPrice
    private var currentTime: Long = timestamp
    var closeReason: CloseReason? = null
    var exitPrice: Double? = null
    var exitTimestamp: Long? = null

    val feesPercent: Double = feesPercentPerSide
    var stopLoss: Double = stopLoss ?: maxStopLoss
    val thoriqRiskValue = maxBalanceExposurePercent * balanceIn

    val stopLossPercent get() = abs(entryPrice - this.stopLoss) / entryPrice
    val theoriqTrade get() = thoriqRiskValue / stopLossPercent
    val totalRisk: Double
    val realTrade: Double
    val riskValue: Double
    val quantity: Double
    val lever: Double

    val entryFees: Double get() = quantity * entryPrice * feesPercent
    val exitFees: Double get() = quantity * currentClose * feesPercent
    val volatility: Double get() = ((exitPrice ?: entryPrice) - entryPrice) / entryPrice
    val exposure: Double get() = realTrade
    val isOpen: Boolean get() = closeReason == null
    val isProfitable: Boolean get() = profitLoss > 0.0
    val fees: Double get() = entryFees + exitFees
    val locked: Double get() = quantity * entryPrice / lever
    val balanceOut: Double get() = balanceIn + profitLoss
    val balanceProfitPercent: Double get() = 1 - (balanceIn / balanceOut)

    private val maxStopLoss
        get() = when (type) {
            LONG -> entryPrice * (1 - maxBalanceExposurePercent)
            SHORT -> entryPrice * (1 + maxBalanceExposurePercent)
        }

    val profitLoss get() = profitLoss(currentClose)
    val maxProfitLoss get() = when (type) {
        LONG -> profitLoss(maxPrice)
        SHORT -> profitLoss(minPrice)
    }
    val minProfitLoss get() = when (type) {
        LONG -> profitLoss(minPrice)
        SHORT -> profitLoss(maxPrice)
    }

    private fun profitLoss(price: Double) = rawProfitLoss(price) - fees

    private fun rawProfitLoss(price: Double) = when (type) {
        LONG -> quantity * (price - entryPrice)
        SHORT -> quantity * (entryPrice - price)
    }

    val riskRatio get() = profitLoss / totalRisk
    val maxRiskRatio get() = maxProfitLoss / totalRisk
    val minRiskRatio get() = minProfitLoss / totalRisk

    val rawProfitLoss get() = rawProfitLoss(currentClose)

    init {
        val approxTotalFeesPercent = 2.0 * feesPercentPerSide
        totalRisk = maxBalanceExposurePercent * balanceIn
        realTrade = min(balanceIn * maxLever, totalRisk / (stopLossPercent + approxTotalFeesPercent))
        riskValue = stopLossPercent * realTrade
        quantity = realTrade / entryPrice
        lever = maxLever

        if (type == LONG && this.stopLoss > entryPrice || type == SHORT && this.stopLoss < entryPrice)
            throw java.lang.IllegalArgumentException("wrong SL parameter")
        if (stopLossPercent > maxBalanceExposurePercent && abs(stopLossPercent - maxBalanceExposurePercent) > 0.001)
            throw java.lang.IllegalArgumentException("SL cannot be > to $maxBalanceExposurePercent")
    }

    // comment prendre en compte l'evolution du prix en temps reel (entre chaque close le prix fluctue)
    //
    private fun applyStrategy() {

    }

    fun updateCurrentPrice(time: Long, currentPrice: Double) {
        currentClose = currentPrice
        currentTime = time
        minPrice = min(minPrice, currentPrice)
        maxPrice = max(maxPrice, currentPrice)

        applyStrategy()

        if (stopLossTouched()) {
            currentClose = stopLoss
            when (type) {
                LONG -> minPrice = stopLoss
                SHORT -> maxPrice = stopLoss
            }
            exit(true)
        }
    }

    fun exit() {
        exit(false)
    }

    private fun exit(stopLoss: Boolean) {
        if (!isOpen) return
        this.closeReason = if (stopLoss) CloseReason.SL else CloseReason.TP
        this.exitPrice = currentClose
        this.exitTimestamp = currentTime
    }

    private fun stopLossTouched(): Boolean {
        if (!isOpen) return false
        return when (type) {
            LONG -> minPrice <= stopLoss
            SHORT -> maxPrice >= stopLoss
        }
    }
}
