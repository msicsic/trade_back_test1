package org.msi.ftx1.infra.remote.ftx

import org.msi.ftx1.business.*
import org.msi.ftx1.infra.remote.ftx.ws.FtxSseClient

class FtxSymbolDataProvider(
    private val wsClient: FtxSseClient,
) : SymbolDataProvider {
    private var listener: SymbolDataConsumer? = null
    private var orderBooks = mutableMapOf<String, OrderBook2>()

    override fun start() {
        wsClient.start(
            orderBookConsumer = { ftxOrderBookResult ->
                val buys = ftxOrderBookResult.orderBook.asks.associate { it[0] to it[1] }
                val sells = ftxOrderBookResult.orderBook.asks.associate { it[0] to it[1] }
                val orderBook = orderBooks.getOrPut(ftxOrderBookResult.symbol) {
                    OrderBook2(ftxOrderBookResult.symbol)
                }
                if (ftxOrderBookResult.orderBook.action == "partial") {
                    orderBook.init(buys, sells)
                } else {
                    orderBook.merge(buys, sells)
                }
                listener?.orderBookUpdateReceived(orderBook)
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
                listener?.tradesUpdateReceived(TradeHistory(ftxTrades.symbol, trades))
            }
        )
    }

    override fun listenSymbol(symbol: String) {
        wsClient.registerSymbol(symbol)
    }

    override fun setListener(listener: SymbolDataConsumer) {
        this.listener = listener
    }

}
