package org.msi.ftx1.business.backtest

class TradeHistory(
    private val initialBalance: Double
) {
    val balance get() = initialBalance + trades.sumOf { it.profitLoss }
    val trades: MutableList<TradeRecord> = mutableListOf()
    val fees: Double get() = trades.sumOf { it.fees }

    // TODO
    val maxDrawDown get(): Double = initialBalance + trades.sumOf { it.drawDown }

    operator fun plusAssign(trade: TradeRecord) {
        require(trade.amount > 0.0)
        trades.add(trade)
    }

    val activeTrade: TradeRecord? get() = trades.firstOrNull { it.isOpen }

    fun updateCurrentPrice(currentPrice: Double) {
        activeTrade?.updateCurrentPrice(currentPrice)
    }

    fun exitActiveTrade() {
        activeTrade?.exit()
    }
}
