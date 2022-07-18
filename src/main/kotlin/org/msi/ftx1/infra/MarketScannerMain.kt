package org.msi.ftx1.infra

import org.msi.ftx1.business.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap

fun main() {
    MarketScannerConfig().configure().apply {
        Main2(
            candleChartProvider = candleChartProvider,
            orderBookProvider = orderBookProvider,
            provider = provider,
            marketProvider = marketProvider
        ).start()
    }
}


class Main2(
    private val candleChartProvider: CandleChartProvider,
    private val orderBookProvider: OrderBookProvider,
    private val provider: SymbolDataProvider,
    // private val barChartService: BarChartService,
    private val marketProvider: MarketProvider
) {

    fun start() {
        val spots = marketProvider.getSpotMarkets()
        val futures = marketProvider.getFutureMarkets()

        val allMarkets = (spots + futures)
//            .filter { it.name in listOf("SHIB-PERP") }
            .filter { it.name !in listOf("USDT-PERP", "USDT/USD", "KSHIB-PERP") }
            .filter { it.volumeUsd24h >= 500000 }
            .sortedByDescending { it.volumeUsd24h }
        System.err.println("All markets by decreasing volume (${allMarkets.size}\n${allMarkets.joinToString(",\n")}")

        val registry = OrderBookRegistry(allMarkets)
        registry.start()
        provider.start()
        allMarkets.forEach { market ->
            provider.addListener(DefaultOrderBookDataConsumer(market.name,
                { orderBook -> registry.updateOrderBook(orderBook) },
                { tradeHistory -> registry.updateTrades(tradeHistory) }
            ))
        }

    }
}

enum class LevelKeySide {
    BUY, SELL
}

data class LevelKey(
    val symbol: String,
    val price: Double,
    val side: LevelKeySide
) {
    override fun toString(): String {
        return "$symbol\t\tprice: $price"
    }
}

data class LevelData(
    val startTime: Long,
    var volume: Double,
    var trades: MutableList<Trade> = mutableListOf()
) {
    fun duration(reference: Long) = (reference - startTime) / 1000

    val totalTraded get() = trades.sumOf { it.sizeUsd }

    override fun toString(): String {
        return "Vol: $volume\tDuration: ${duration(System.currentTimeMillis())}\ttraded: $totalTraded\tpercent: ${
            BigDecimal(
                100.0 * totalTraded / volume
            ).setScale(2, RoundingMode.HALF_EVEN)
        }%"
    }

}

class OrderBookRegistry(
    markets: List<Market>
) {
    val orderbooks: MutableMap<String, OrderBook2> = mutableMapOf()
    val markets: Map<String, Market> = markets.associateBy { it.name }
    val _registeredLevels = ConcurrentHashMap<LevelKey, LevelData>()

    fun getRegisteredLevelsFor(symbol: String) = _registeredLevels.filter { it.key.symbol == symbol }
    fun getValidatedLevelsFor(symbol: String) =
        getRegisteredLevelsFor(symbol).filter { it.value.duration(System.currentTimeMillis()) >= 10 }

    fun updateOrderBook(orderBook2: OrderBook2) {
        orderbooks[orderBook2.symbol] = orderBook2
        scanOrderBook(orderBook2)
    }

    fun updateTrades(tradeHistory: TradeHistory) {
        tradeHistory.trades.forEach { trade -> registerTrade(tradeHistory.symbol, trade) }
    }

    private fun registerTrade(symbol: String, trade: Trade) {

        val tradeKey = LevelKey(symbol, trade.price, if (trade.side == "buy") LevelKeySide.SELL else LevelKeySide.BUY)
        getValidatedLevelsFor(symbol)[tradeKey]?.let { data ->
            data.trades.add(trade)
            System.err.println("Trade on a level (${tradeKey.side})\t: $tradeKey\t$data\t${trade.sizeUsd}")
        }

    }

    private fun scanOrderBook(orderBook2: OrderBook2) {
        markets[orderBook2.symbol]?.let { market ->
            val seuil = 0.1 / 100.0 * market.volumeUsd24h
            val orderBookLevels =
                orderBook2.buys.map { (price, volume) ->
                    LevelKey(market.name, price, LevelKeySide.BUY) to volume
                } + orderBook2.sells.map { (price, volume) ->
                    LevelKey(market.name, price, LevelKeySide.SELL) to volume
                }

            val levelsInOrderBook = orderBookLevels.map { it.first }.toSet()

            val registeredLevels = getRegisteredLevelsFor(orderBook2.symbol)
            val levelsToRemove = registeredLevels.map { it.key }.filter { !levelsInOrderBook.contains(it) }
            levelsToRemove.forEach {
                val existingLevelData = _registeredLevels[it]
                if (existingLevelData != null) {
                    if (existingLevelData.totalTraded > 0) {
                        System.err.println("******** LEVEL REMOVED ! :$it, $existingLevelData")
                    }
                    _registeredLevels.remove(it)
                }
            }

            orderBookLevels.forEach { (levelKey, volume) ->
                val existingLevel = registeredLevels[levelKey]
                if (existingLevel != null && existingLevel.totalTraded == 0.0 && volume * levelKey.price < seuil) {
                    _registeredLevels.remove(levelKey)
                } else {
                    if (existingLevel != null) {
                        _registeredLevels[levelKey] = LevelData(existingLevel.startTime, volume * levelKey.price, existingLevel.trades)
                    } else {
                        _registeredLevels[levelKey] = LevelData(System.currentTimeMillis(), volume * levelKey.price)
                    }
                }
            }

            //System.err.println("validated levels for ${orderBook2.symbol}: ${validatedLevels.size}")
        }
    }

    fun start() {
//        thread {
//
//            while (true) {
//                val currentTime = System.currentTimeMillis()
//
//                val added = mutableSetOf<LevelKey>()
//                registeredLevels.toMap().forEach { (levelKey, levelData) ->
//                    if (levelData.duration(currentTime) > 2) {
//                        added.add(levelKey)
//                    }
//                }
//                touchedLevels.clear()
//                touchedLevels.addAll(added)
//                sleep(1000)
//            }
//        }
    }
}

/*

TODO

- il faut une IHM pour afficher les resultats, la console qui scrolle n'est pas exploitable
- il faut pouvoir filtrer sur un symbol
- les niveaux retenus doivent etre mis à jour en temps reel (evolution du temps et du volume)
- il faut séparer les buys et sells (resistances et supports)
- il faut pouvoir ajuster le seuil apres coup, ainsi que le pourcentage d'écart avec le prix courant
- il faut le total des trades qui s'executent sur chaque niveau enregistré

- trouver des placements de target et SL en fonction des volumes dans l'OB
- plus tard: il faudrait enregistrer l'orderbook et déterminer automatiquement les quantités de prix qui provoquent des rebonds

- envoyer des 'pong' sinon le client s'arrete

 */
