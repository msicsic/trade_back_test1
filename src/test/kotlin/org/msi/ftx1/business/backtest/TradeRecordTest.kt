package org.msi.ftx1.business.backtest

import org.junit.jupiter.api.Assertions.*
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
     * 1000x10=10000x0.1%x2(fees)=20$ de frais, ce qui fait un risque total de 40$ (les frais sont approximés car on ne connait pas le prix de sortie)
     *
     * TODO: faire une simu avec des frais important pour tester l'algo (ex 1% de frais)
     * TODO: calcul break-even price
     * TODO: calcul de prix cible en fonction d'un RR en param (prix nécessaire pour obtenir un RR de 3 par ex)
     */

    @Test
    fun `SL on a LONG must be correct`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.stopLoss eq 99.8)
    }

    @Test
    fun `SL on a SHORT must be correct`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.SHORT,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.stopLoss eq 100.2)
    }

    @Test
    fun `Lever cannot be max than max lever for LONG`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.theoriqTrade eq 25000.0)
        assertTrue(trade.realTrade eq 10000.0)
        assertTrue(trade.lever eq 10.0)
        assertTrue(trade.riskValue eq 20.0)
        assertTrue(trade.stopLossPercent eq 0.002)
    }

    @Test
    fun `Lever cannot be max than max lever for SHORT`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.SHORT,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.theoriqTrade eq 25000.0)
        assertTrue(trade.realTrade eq 10000.0)
        assertTrue(trade.lever eq 10.0)
        assertTrue(trade.riskValue eq 20.0)
        assertTrue(trade.stopLossPercent eq 0.002)
    }

    @Test
    fun `Lever for LONG`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 100.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.theoriqTrade eq 25000.0)
        assertTrue(trade.realTrade eq 25000.0)
        assertTrue(trade.lever eq 25.0)
        assertTrue(trade.riskValue eq 50.0)
        assertTrue(trade.stopLossPercent eq 0.002)
    }

    @Test
    fun `Lever for SHORT`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 100.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.SHORT,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.theoriqTrade eq 25000.0)
        assertTrue(trade.realTrade eq 25000.0)
        assertTrue(trade.lever eq 25.0)
        assertTrue(trade.riskValue eq 50.0)
        assertTrue(trade.stopLossPercent eq 0.002)
    }

    @Test
    fun `computed amount for LONG`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.quantity eq 100.0)
    }

    @Test
    fun `computed amount for SHORT`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.SHORT,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.quantity eq 100.0)
    }

    @Test
    fun `when SL is touched on a LONG, exitPrice must be equal to stopLoss`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(0.0, 2L)
        assertTrue(trade.exitPrice!! eq trade.stopLoss)
    }

    @Test
    fun `when SL is touched on a SHORT, exitPrice must be equal to stopLoss`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.SHORT,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(500.0, 2L)
        assertTrue(trade.exitPrice!! eq trade.stopLoss)
    }

    @Test
    fun `when SL is touched for LONG, exit reason should be SL`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(0.0, 2L)
        assertEquals(CloseReason.SL, trade.closeReason!!)
    }

    @Test
    fun `when SL is touched for SHORT, exit reason should be SL`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.SHORT,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(500.0, 2L)
        assertEquals(CloseReason.SL, trade.closeReason!!)
    }

    @Test
    fun `when SL is touched for LONG, the PnL should equals riskValue + fees`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(0.0, 2L)
        assertTrue(abs(trade.profitLoss) eq trade.riskValue + trade.fees)
    }

    @Test
    fun `when SL is touched for SHORT, the PnL should equals riskValue + fees`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.SHORT,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(500.0, 2L)
        assertTrue(abs(trade.profitLoss) eq trade.riskValue + trade.fees)
    }

    @Test
    fun `when trade is exited, reason should be TP for LONG`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(200.0, 2L)
        trade.exit()
        assertEquals(CloseReason.TP, trade.closeReason!!)
        assertFalse(trade.isOpen)
        assertTrue(trade.isProfitable)
        assertTrue(10.0 eq trade.entryFees)
        assertTrue(20.0 eq trade.exitFees)
        assertTrue(30.0 eq trade.fees)
        assertTrue(9970.0 eq trade.profitLoss)
    }

    @Test
    fun `when trade is exited, reason should be TP for SHORT`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.SHORT,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            initialStopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(0.0, 2L)
        trade.exit()
        assertEquals(CloseReason.TP, trade.closeReason!!)
        assertFalse(trade.isOpen)
        assertTrue(trade.isProfitable)
        assertTrue(10.0 eq trade.entryFees)
        assertTrue(0.0 eq trade.exitFees)
        assertTrue(10.0 eq trade.fees)
        assertTrue(9990.0 eq trade.profitLoss)
    }

}

private infix fun Double.eq(other: Double) = if (abs(this - other) < 0.001) true else { System.err.println("$this is not $other"); false }
