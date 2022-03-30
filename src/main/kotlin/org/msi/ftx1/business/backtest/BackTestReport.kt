package org.msi.ftx1.business.backtest

import kotlin.math.abs

class BackTestReport(
    private val spec: BackTestSpec,
    val tradeHistory: TradeHistory,
    private val startPrice: Double,
    private val endPrice: Double,
) {
    val tradeCount: Int
        get() = tradeHistory.trades.size

    val pyramidingLimit: Int
        get() = spec.pyramidingLimit

    val betSize: Double
        get() = spec.betSize

    val initialBalance: Double
        get() = spec.startingBalance

    val finalBalance: Double
        get() = tradeHistory.balance

    val maxDrawDown: Double
        get() = tradeHistory.maxDrawDown

    /**
     * The Sortino ratio with a risk-free rate of 0%.
     *
     * {@see https://www.investopedia.com/terms/s/sortinoratio.asp}
     */
    val sortinoRatio: Double
        get() = (profitability - RISK_FREE_RATE) / maxDrawDown

    val profitability: Double
        get() = profitLoss / initialBalance

    val profitLoss: Double
        get() = tradeHistory.trades.sumOf { it.profitLoss }

    val winRate: Double
        get() = winCount.toDouble() / tradeCount.toDouble()

    private val winCount: Int
        get() = tradeHistory.trades.count { it.isProfitable }

    val vsBuyAndHold: Double
        get() = profitability - buyAndHoldProfitability

    val buyAndHoldProfitability: Double
        get() = if (spec.tradeType === TradeType.LONG) (endPrice - startPrice) / startPrice else (startPrice - endPrice) / endPrice

    val riskReward: Double
        get() = tradeHistory.trades.sumOf { this.getRiskReward(it) } / tradeCount

    private fun getRiskReward(tradeRecord: TradeRecord): Double {
        val positionSize = tradeRecord.entryPrice * tradeRecord.amount
        val profitLoss = tradeRecord.profitLoss
        val percentageProfitLoss = 1 + profitLoss / (positionSize - abs(profitLoss))
        return percentageProfitLoss / maxDrawDown
    }
}

/**
 * The risk-free rate to use when calculating Sharpe and Sortino ratios.
 *
 * Current personal savings interest ratio is near zero or negative so this value is mostly included for clarity.
 */
private const val RISK_FREE_RATE = 0.0
