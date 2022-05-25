package org.msi.ftx1.business.backtest

import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

class BackTestReport(
    private val spec: BackTestSpec,
    val tradeHistory: TradeHistory,
    private val startPrice: Double,
    private val endPrice: Double,
) {
    val duration: String
        get() =
            ChronoUnit.DAYS.between(spec.startTime.toLocalDate(), spec.endTime.toLocalDate()).toString()

    val tradeCount: Int
        get() = tradeHistory.trades.size

    val exposure: Double
        get() = spec.exposurePercent

    val initialBalance: Double
        get() = spec.startingBalance

    val finalBalance: Double
        get() = tradeHistory.balance

    val maxDrawDown: Double
        get() = tradeHistory.maxDrawDown()

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
        get() = (endPrice - startPrice) / startPrice

    val riskReward: Double
        get() = tradeHistory.trades.sumOf { this.getRiskReward(it) } / tradeCount

    private fun getRiskReward(tradeRecord: TradeRecord): Double {
        val positionSize = tradeRecord.entryPrice * tradeRecord.quantity
        val profitLoss = tradeRecord.profitLoss
        val percentageProfitLoss = 1 + profitLoss / (positionSize - abs(profitLoss))
        return percentageProfitLoss / maxDrawDown
    }

    fun print() {
        println("---------- TRADES ----------------")
        tradeHistory.trades.forEach { print(it) }

        println("----------------------------------")
        println("Finished backtest with ${tradeCount} trades, period: ${duration} days")
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
        println("----------------------------------")

        // Financial result
        println(String.format("Start balance      : $%,.2f", initialBalance))
        println(String.format("Profit/loss        : $%,.2f", profitLoss))
        println(String.format("Final balance      : $%,.2f", finalBalance))
        println("----------------------------------")

        println("---------- BEST TRADE ----------------")
        tradeHistory.trades.maxByOrNull { it.profitLoss }?.let { print(it) }
        println("---------- WORST TRADE ----------------")
        tradeHistory.trades.minByOrNull { it.profitLoss }?.let { print(it) }
    }

    private fun print(trade: TradeRecord) {
        println(String.format("Result                    : ${if (trade.isProfitable) "WIN" else "LOST"} ${trade.type} (Close ${trade.closeReason}) ${Date(trade.timestamp)} to ${Date(trade.exitTimestamp ?: 0L)}, entry $%,.2f, exit $%,.2f, volat %.2f%%", trade.entryPrice, trade.exitPrice, 100.0 * trade.volatility))
        println(String.format("  Trade                   : %,.2f at $%,.2f with SL $%,.2f, locked $%,.2f with lever x%,.2f (total expo. $%,.2f)", trade.quantity, trade.entryPrice, trade.stopLoss, trade.locked, trade.lever, trade.exposure))
        println(String.format("  Balance                 : $%,.2f => $%,.2f (%.2f%%)", trade.balanceIn, trade.balanceOut, 100.0*trade.balanceProfitPercent))
        println(String.format("  PnL (inc. Fees)         : $%,.2f (fees $%,.2f), RR %,.2f", trade.profitLoss, trade.fees, trade.riskRatio))
        println("----------------------------------")
    }
}
