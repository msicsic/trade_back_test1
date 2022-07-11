package org.msi.ftx1.business

// entréé, branchée sur le WS ou bien data histo pour le replay
interface SymbolDataConsumer {
    val symbol: String

    fun orderBookUpdateReceived(orderBook: OrderBook2)
    fun tradesUpdateReceived(tradeHistory: TradeHistory)
}

class DefaultSymbolDataConsumer(
    override val symbol: String
) : SymbolDataConsumer {
    val tradeHistory = TradeHistory(symbol)
    var orderBook: OrderBook2 = OrderBook2(symbol)

    override fun orderBookUpdateReceived(orderBook: OrderBook2) {
        this.orderBook = orderBook
    }

    override fun tradesUpdateReceived(tradeHistory: TradeHistory) {
        this.tradeHistory.addTrades(tradeHistory.trades)
    }
}
