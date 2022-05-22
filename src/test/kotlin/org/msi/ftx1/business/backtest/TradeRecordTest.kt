package org.msi.ftx1.business.backtest

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

internal class TradeRecordTest {

    /**
     * 1000$
     * 5%, soit 50$ max risk per trade
     * SL 0.2% => 50 / 0.002 = 25000$ de position theorique, ce qui est sup au max autorisé par le levier 10x (=10000$)
     * donc => diminuer le trade à 10000, soit 20$ de risque
     *
     * TODO: comment tenir compte des frais importants avec le levier ?
     * 1000x10=10000x0.1%x2(fees)=20$ de frais, ce qui fait un risque total de 40$
     *
     * TODO: faire une simu avec des frais important pour tester l'algo (ex 1% de frais)
     * TODO: calcul break-even price
     * TODO: calcul de prix cible en fonction d'un RR en param (prix nécessaire pour obtenir un RR de 3 par ex)
     */
    private fun createTrade() = TradeRecord(
        maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
        maxLever = 10.0, // max lever allowed by broker
        feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
        type = TradeType.LONG,
        timestamp = currentTime,
        balanceIn = 1000.0,
        entryPrice = 100.0, // open price
        initialStopLoss = 99.8 // 0.2% SL computed by the strategy
    )

    @Test
    fun `SL must be correct`() {
        val trade = createTrade()
        assertTrue(trade.stopLoss eq 99.8)
    }

    @Test
    fun `Lever computation`() {
        val trade = createTrade()
        assertTrue(trade.theoriqTrade eq 25000.0)
        assertTrue(trade.realTrade eq 10000.0)
        assertTrue(trade.lever eq 10.0)
        assertTrue(trade.riskValue eq 20.0)
        assertTrue(trade.stopLossPercent eq 0.002)
    }

    @Test
    fun `computed amount`() {
        val trade = createTrade()
        assertTrue(trade.quantity eq 100.0)
    }

    @Test
    fun `when SL is touched, exitPrice must be equal to stopLoss`() {
        val trade = createTrade()
        trade.updateCurrentPrice(0.0, 2L)
        assertTrue(trade.exitPrice!! eq trade.stopLoss)
    }

    @Test
    fun `when SL is touched, the PnL should equals riskValue + fees`() {
        val trade = createTrade()
        trade.updateCurrentPrice(0.0, 2L)
        assertTrue(abs(trade.profitLoss) eq trade.riskValue + trade.fees)
    }

}

private infix fun Double.eq(other: Double) = abs(this - other) < 0.001
