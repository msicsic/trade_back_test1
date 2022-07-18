package org.msi.ftx1.infra.remote.ftx

import org.msi.ftx1.business.*
import org.msi.ftx1.infra.remote.ftx.ws.FtxSseClient

class FtxSymbolDataProvider(
    private val wsClient: FtxSseClient,
) : SymbolDataProvider {
    private var listeners: MutableList<SymbolDataConsumer> = mutableListOf()
    private var orderBooks = mutableMapOf<String, OrderBook2>()
    private var started = false

    override fun addListener(listener: SymbolDataConsumer) {
        if (! started) throw IllegalStateException("Provider must be started first")

        if (this.listeners.none { it.symbol == listener.symbol}) {
            wsClient.registerSymbol(listener.symbol)
        }
        this.listeners.add(listener)
    }

    override fun start() {
        this.started = true
        wsClient.start(
            orderBookConsumer = { ftxOrderBookResult ->
                val buys = ftxOrderBookResult.orderBook.bids.associate { it[0] to it[1] }
                val sells = ftxOrderBookResult.orderBook.asks.associate { it[0] to it[1] }
                val orderBook = orderBooks.getOrPut(ftxOrderBookResult.symbol) {
                    OrderBook2(ftxOrderBookResult.symbol)
                }
                if (ftxOrderBookResult.orderBook.action == "partial") {
                    orderBook.init(buys, sells)
                } else {
                    orderBook.merge(buys, sells)
                }
                this.listeners.filter { it.symbol == orderBook.symbol}.forEach { it.orderBookUpdateReceived(orderBook) }
            },
            tradesConsumer = { ftxTrades ->
                val trades = ftxTrades.trades.map { tradeEntry ->
                    Trade(
                        timeMs = tradeEntry.timeAsSeconds,
                        price = tradeEntry.price,
                        size = tradeEntry.size,
                        liquidation = tradeEntry.liquidation,
                        side = tradeEntry.side
                    )
                }
                val tradeHistory = TradeHistory(ftxTrades.symbol, trades)
                this.listeners.filter { it.symbol == tradeHistory.symbol}.forEach { it.tradesUpdateReceived(tradeHistory) }
            }
        )
    }
}
