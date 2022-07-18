package org.msi.ftx1.business

// 1 second resolution
class TradeHistory(
    val symbol: String,
    trades: List<Trade> = listOf()
) {
    val trades = trades.toMutableList()
    val startTimeSeconds: Long get() = trades.sortedBy { it.timeSeconds }.first().timeSeconds
    val endTimeSeconds: Long get() = trades.sortedByDescending { it.timeSeconds }.first().timeSeconds

    fun getTrades(second: Long): List<Trade> {
        return trades.filter { it.timeSeconds == second }
    }

    fun getBar(second: Long, previousBar: Bar): Bar {
        return getTrades(second).let { trades ->
            Bar(
                TimeFrame.SEC_1,
                second,
                open = trades.open ?: previousBar.open,
                close = trades.close ?: previousBar.close,
                high = trades.high ?: previousBar.high,
                low = trades.low ?: previousBar.low,
                volume = trades.volume
            )
        }
    }

    fun addTrades(trades: List<Trade>) {
        this.trades.addAll(trades)
    }

    override fun toString(): String {
        return "TradeHistory(symbol='$symbol', trades=$trades)"
    }

    private val List<Trade>.open get() = this.minByOrNull { it.timeMs }?.price
    private val List<Trade>.close get() = this.maxByOrNull { it.timeMs }?.price
    private val List<Trade>.high get() = this.maxOfOrNull { it.price }
    private val List<Trade>.low get() = this.minOfOrNull { it.price }
    private val List<Trade>.volume get() = this.sumOf { it.size }


}

data class Trade(
    val timeMs: Long,
    val price: Double,
    val size: Double,
    val liquidation: Boolean,
    val side: String,
) {
    val timeSeconds: Long get() = timeMs / 1000
    val sizeUsd get() = size*price
}
