package org.msi.ftx1.business

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

data class OrderBook(
    val buys: List<PriceAndSize>,
    val sells: List<PriceAndSize>
)

data class PriceAndSize(
    val price: Double,
    val size: Double
) {
    override fun toString() = """$price$ ($size)"""
}

class OrderBook2(
    val symbol: String,
    buys: Map<Double, Double> = mutableMapOf(),
    sells: Map<Double, Double> = mutableMapOf(),
) {
    val buys = ConcurrentHashMap(buys)
    val sells = ConcurrentHashMap(sells)

    val lastBuys = ConcurrentHashMap<Double, Double>()
    val lastSells = ConcurrentHashMap<Double, Double>()

    val buysMin get() = buys.keys.minOrNull() ?: 0.0
    val buysMax get() = buys.keys.maxOrNull() ?: 0.0
    val sellsMin get() = sells.keys.minOrNull() ?: 0.0
    val sellsMax get() = sells.keys.maxOrNull() ?: 0.0
    val spread get() = sellsMin - buysMax
    val minPriceInBook get() = buys.keys.minOf { it }
    val maxPriceInBook get() = sells.keys.maxOf { it }
    val buysTotal get() = buys.values.sum()
    val sellsTotal get() = sells.values.sum()
    val buysMaxLevel get() = buys.entries.maxByOrNull { it.value }?.toPair() ?: (0.0 to 0.0)
    val sellsMaxLevel get() = sells.entries.maxByOrNull { it.value }?.toPair()?: (0.0 to 0.0)
    val rangeBuysPercent get() = (buysMax - buysMin) / buysMin
    val rangeSellsPercent get() = (sellsMax - sellsMin) / sellsMin
    val deltaUpDown get() = BigDecimal(liquidityBuys() - liquiditySells()).setScale(2, RoundingMode.HALF_EVEN)

    fun percentageFromCurrentPrice(price: Double) = abs(sellsMin-price) /sellsMin

    fun maxBuyLevels(nb: Int, minSize: Double): List<Pair<Double, Double>> {
        return buys.entries.filter{it.value >= minSize}.sortedByDescending { it.value }.takeLast(nb).map { it.toPair()}
    }

    fun maxSellLevels(nb: Int, minSize: Double): List<Pair<Double, Double>> {
        return sells.entries.filter{it.value >= minSize}.sortedByDescending { it.value }.takeLast(nb).map { it.toPair()}
    }

    fun maxBuyLevelsString(): String {
        return maxBuyLevels(3, 80.0).map { """[${it.first}|${it.second}]""" }.joinToString(", ")
    }
    fun maxSellLevelsString(): String {
        return maxSellLevels(3, 80.0).map { """[${it.first}|${it.second}]""" }.joinToString(", ")
    }

    fun liquidityBuys(): Double {
        val buyLow = buysMax*(1.0-0.1/100)
        var total = 0.0
        for (price: Double in buys.keys.sortedDescending()) {
            if (price < buyLow) break
            total += buys[price] ?: 0.0
        }
        return total
    }

    fun liquiditySells(): Double {
        val sellHigh = sellsMin*(1.0+0.1/100)
        var total = 0.0
        for (price: Double in sells.keys.sorted()) {
            if (price > sellHigh) break
            total += sells[price] ?: 0.0
        }
        return total
    }

    fun init(buys: Map<Double, Double>, sells:Map<Double, Double>) {
        this.buys.clear()
        this.sells.clear()
        this.buys.putAll(buys)
        this.sells.putAll(sells)
        this.lastBuys.clear()
        this.lastSells.clear()
    }

    fun merge(buys: Map<Double, Double>, sells:Map<Double, Double>) {
        this.buys.putAll(buys)
        this.sells.putAll(sells)
        buys.filter  { it.value == 0.0 }.forEach { this.buys.remove(it.key) }
        sells.filter  { it.value == 0.0 }.forEach { this.sells.remove(it.key) }
        this.lastBuys.clear()
        this.lastSells.clear()
        this.lastBuys.putAll(buys)
        this.lastSells.putAll(sells)
    }

    override fun toString(): String {
        return "OrderBook2(buys=$buys, sells=$sells)"
    }


}
