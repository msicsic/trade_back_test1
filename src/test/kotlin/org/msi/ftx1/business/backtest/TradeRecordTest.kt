package org.msi.ftx1.business.backtest

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs

internal class TradeRecordTest {

    /**
     * Example:
     * 1000$ sur le compte, levier max 10x
     * 5% par trade, soit 50$ max risk per trade
     * SL 0.2% => 50 / 0.002 = 25000$ de position theorique, ce qui est sup au max autorisé par le levier 10x (=10000$)
     * donc => diminuer le trade à 10000, soit 20$ de risque
     *
     * TODO: calcul break-even price
     * TODO: calcul de prix cible en fonction d'un RR en param (prix nécessaire pour obtenir un RR de 3 par ex)
     */

    @Test
    fun `high fees should lower the position`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 100.0, // max lever allowed by broker
            feesPercentPerSide = 1.0/100.0, // 0.2% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            stopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(2L, 0.0)
        assertTrue(trade.stopLoss eq 99.8)
        assertTrue(trade.theoriqTrade eq 25000.0)
        assertTrue(trade.realTrade eq 2272.727)
        assertTrue(trade.lever eq 100.0)
        assertTrue(trade.riskValue eq 4.545)
        assertTrue(trade.stopLossPercent eq 0.002)
        assertTrue(trade.fees eq 45.41)
        assertTrue(trade.profitLoss eq -(trade.riskValue+trade.fees))
    }

    @Test
    fun `high fees should lower the position, with low lever`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 2.0, // max lever allowed by broker
            feesPercentPerSide = 1.0/100.0, // 0.2% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            stopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(2L, 0.0)
        assertTrue(trade.stopLoss eq 99.8)
        assertTrue(trade.theoriqTrade eq 25000.0)
        assertTrue(trade.realTrade eq 2000.0)
        assertTrue(trade.lever eq 2.0)
        assertTrue(trade.riskValue eq 4.0)
        assertTrue(trade.stopLossPercent eq 0.002)
        assertTrue(trade.fees eq 39.96)
        assertTrue(trade.profitLoss eq -(trade.riskValue+trade.fees))
    }

    @Test
    fun `high fees low lever`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 5.0, // max lever allowed by broker
            feesPercentPerSide = 0.2/100.0, // 0.2% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            stopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(2L, 0.0)
        assertTrue(trade.stopLoss eq 99.8)
        assertTrue(trade.theoriqTrade eq 25000.0)
        assertTrue(trade.realTrade eq 5000.0)
        assertTrue(trade.lever eq 5.0)
        assertTrue(trade.riskValue eq 10.0)
        assertTrue(trade.stopLossPercent eq 0.002)
        assertTrue(trade.fees eq 19.98)
        assertTrue(trade.profitLoss eq -(trade.riskValue+trade.fees))
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
            stopLoss = 99.8 // 0.2% SL computed by the strategy
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
            stopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.theoriqTrade eq 25000.0)
        assertTrue(trade.realTrade eq 10000.0)
        assertTrue(trade.lever eq 10.0)
        assertTrue(trade.riskValue eq 20.0)
        assertTrue(trade.stopLossPercent eq 0.002)
    }

    @Test
    fun `Lever for LONG should be max allowed to free available money`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 10000 = 500$ max risk per trade
            maxLever = 100.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 10000.0,
            entryPrice = 100.0, // open price
            stopLoss = 95.0 // 5% SL computed by the strategy
        )
        trade.updateCurrentPrice(2L, 0.0)
        assertTrue(trade.theoriqTrade eq 10000.0)
        assertTrue(trade.realTrade eq 9615.3846)
        assertTrue(trade.riskValue eq 480.769)
        assertTrue(trade.stopLossPercent eq 0.05)
        assertTrue(trade.lever eq 100.0)
        assertTrue(trade.locked eq 96.1538)
        assertTrue(trade.fees eq 18.75)
        assertTrue(trade.profitLoss eq -499.519)
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
            stopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.theoriqTrade eq 25000.0)
        assertTrue(trade.realTrade eq 12500.0)
        assertTrue(trade.lever eq 100.0)
        assertTrue(trade.locked eq 125.0)
        assertTrue(trade.riskValue eq 25.0)
        assertTrue(trade.stopLossPercent eq 0.002)
    }

    // TODO: check with small max lever
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
            stopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.theoriqTrade eq 25000.0)
        assertTrue(trade.realTrade eq 12500.0)
        assertTrue(trade.lever eq 100.0)
        assertTrue(trade.locked eq 125.0)
        assertTrue(trade.riskValue eq 25.0)
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
            stopLoss = 99.8 // 0.2% SL computed by the strategy
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
            stopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        assertTrue(trade.quantity eq 100.0)
    }

    @Test
    fun `when SL is touched on a LONG`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.LONG,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            stopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(2L, 0.0)
        assertTrue(trade.exitPrice!! eq trade.stopLoss)
        assertEquals(CloseReason.SL, trade.closeReason!!)
        assertFalse(trade.isProfitable)
        assertTrue(abs(trade.profitLoss) eq trade.riskValue + trade.fees)
        assertTrue(10.0 eq trade.entryFees)
        assertTrue(9.98 eq trade.exitFees)
        assertTrue(19.98 eq trade.fees)
        assertTrue(-20.0 eq trade.rawProfitLoss)
        assertTrue(-39.98 eq trade.profitLoss)
    }

    @Test
    fun `when SL is touched on a SHORT`() {
        val trade = TradeRecord(
            maxBalanceExposurePercent = 0.05, // 5% of 1000 = 50$ max risk per trade
            maxLever = 10.0, // max lever allowed by broker
            feesPercentPerSide = 0.1/100.0, // 0.1% fee of current price
            type = TradeType.SHORT,
            timestamp = currentTime,
            balanceIn = 1000.0,
            entryPrice = 100.0, // open price
            stopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(2L, 200.0)
        assertTrue(trade.exitPrice!! eq trade.stopLoss)
        assertFalse(trade.isProfitable)
        assertEquals(CloseReason.SL, trade.closeReason!!)
        assertTrue(abs(trade.profitLoss) eq trade.riskValue + trade.fees)
        assertTrue(10.0 eq trade.entryFees)
        assertTrue(10.02 eq trade.exitFees)
        assertTrue(20.02 eq trade.fees)
        assertTrue(-20.0 eq trade.rawProfitLoss)
        assertTrue(-40.02 eq trade.profitLoss)
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
            stopLoss = 99.8 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(2L, 200.0)
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
            stopLoss = 100.2 // 0.2% SL computed by the strategy
        )
        trade.updateCurrentPrice(2L, 0.0)
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

// TODO: le gain ou perte ne doit pas dépasser le % configuré
