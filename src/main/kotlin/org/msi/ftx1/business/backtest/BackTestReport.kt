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

    val exposure: Double
        get() = spec.exposure

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

    fun print() {
        println("----------------------------------")
        println("Finished backtest with ${tradeCount} trades")
        println("----------------------------------")

        // Input parameters
        println(String.format("Account risk/trade : %.1f%%", 100.0 * exposure))
        println("----------------------------------")

        // Analysis
        println(String.format("Profitability      : %.2f%%", 100.0 * profitability))
        println(String.format("Buy-and-hold       : %.2f%%", 100.0 * buyAndHoldProfitability))
        println(String.format("Vs buy-and-hold    : %.2f%%", 100.0 * vsBuyAndHold))
        println(String.format("Total fees         : %.2f $", tradeHistory.fees))
        println(String.format("Win rate           : %.2f%%", 100.0 * winRate))
        println(String.format("Max drawdown       : %.2f%%", 100.0 * maxDrawDown))
        println(String.format("Risk/reward ratio  : %.2f", riskReward))
        println(String.format("Sortino ratio      : %.2f", sortinoRatio))
        println("----------------------------------")

        // Financial result
        println(String.format("Start balance      : $%,.2f", initialBalance))
        println(String.format("Profit/loss        : $%,.2f", profitLoss))
        println(String.format("Final balance      : $%,.2f", finalBalance))
        println("----------------------------------")
    }

}

/**
 * The risk-free rate to use when calculating Sharpe and Sortino ratios.
 *
 * Current personal savings interest ratio is near zero or negative so this value is mostly included for clarity.
 */
private const val RISK_FREE_RATE = 0.0
