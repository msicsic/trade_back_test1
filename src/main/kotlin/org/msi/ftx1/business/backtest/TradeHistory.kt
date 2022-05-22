package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.BarChart

class TradeHistory(
    private val initialBalance: Double,
    private val strategy: TradeStrategy
) {
    val balance get() = initialBalance + trades.sumOf { it.profitLoss }
    val trades: MutableList<TradeRecord> = mutableListOf()
    val fees: Double get() = trades.sumOf { it.fees }

    // TODO
    val maxDrawDown get(): Double = initialBalance + trades.sumOf { it.drawDown }

    operator fun plusAssign(trade: TradeRecord) {
        require(trade.quantity > 0.0)
        trades.add(trade)
    }

    val activeTrade: TradeRecord? get() = trades.firstOrNull { it.isOpen }

    fun updateCurrentPrice(chart: BarChart, currentPrice: Double, currentTime: Long) {
        activeTrade?.updateCurrentPrice(currentPrice, currentTime)
        strategy.evaluateEntry(chart, this, currentTime)
        activeTrade?.let { strategy.evaluateTrade(chart, this, currentTime, it) }
    }

    fun exitActiveTrade() {
        activeTrade?.exit(false)
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
            initialStopLoss = stopLoss
        )
    }
}
