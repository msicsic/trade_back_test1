package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.BarChart

class TradeHistory(
    private val initialBalance: Double,
    private val strategy: TradeStrategy
) {
    val balance get() = initialBalance + trades.sumOf { it.profitLoss }
    val trades: MutableList<TradeRecord> = mutableListOf()
    val fees: Double get() = trades.sumOf { it.fees }

    fun maxDrawDown(): Double {
        var balance = initialBalance
        var min = initialBalance
        for (trade in trades) {
            balance += trade.profitLoss
            if (balance < min) {
                min = balance
            }
        }
        return 1.0 - min / initialBalance
    }

    operator fun plusAssign(trade: TradeRecord) {
        require(trade.quantity > 0.0)
        trades.add(trade)
    }

    val activeTrade: TradeRecord? get() = trades.firstOrNull { it.isOpen }

    val hasActiveTrade: Boolean get() = activeTrade != null

    fun updateCurrentPrice(chart: BarChart, close: Double, high: Double, low: Double, currentTime: Long) {
        setOf(close, high, low).forEach { price ->
            activeTrade?.updateCurrentPrice(currentTime, price)
            strategy.evaluateEntry(chart, this, currentTime)
            activeTrade?.let { strategy.evaluateTrade(chart, this, currentTime, it) }
        }
    }

    fun exitActiveTrade() {
        activeTrade?.exit()
    }

    fun openTrade(spec: BackTestSpec, tradeType: TradeType, currentPrice: Double, currentTime: Long, stopLoss: Double) {
        if (activeTrade != null || balance <= 0) return

        this += TradeRecord(
            maxBalanceExposurePercent = spec.exposurePercent,
            maxLever = spec.maxLever,
            feesPercentPerSide = spec.feePerTrade,
            type = tradeType,
            timestamp = currentTime,
            balanceIn = balance,
            entryPrice = currentPrice,
            stopLoss = stopLoss
        )
    }
}
