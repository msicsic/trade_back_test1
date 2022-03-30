package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.BarChart
import org.msi.ftx1.business.indicator.latestValue
import org.msi.ftx1.business.signal.SignalType

object BackTester {

    fun run(spec: BackTestSpec): BackTestReport {
        val inputBars = spec.provider.getCandleChart(
            symbol = spec.symbol,
            interval = spec.runTimeFrame,
            startTime = spec.startTime,
            endTime = spec.endTime
        )
        val inputTimeFrame = inputBars[0].interval
        val timeSeriesManager = BarChart(spec.symbol, inputTimeFrame, spec.startTime)
        val strategy = spec.strategyFactory.buildStrategy(timeSeriesManager)
        val stopLoss = spec.stopLoss.buildIndicator(timeSeriesManager)
        val runTimeSeries = timeSeriesManager.getDownSampledChart(spec.runTimeFrame)
        val tradeHistory = TradeHistory(
            balance = spec.startingBalance,
            feePerTrade = spec.feePerTrade
        )

        for (inputBar in inputBars._data) {
            timeSeriesManager += inputBar
            val currentPrice = inputBar.close

            // Closes stopped trades
            tradeHistory.closeStoppedTrades(currentPrice)

            // Updates trade history equity, drawdown.
            tradeHistory.updateEquity(currentPrice)

            // Avoids trading before the next close.
            // TODO: improve detection of new bars in the run time series.
            if (runTimeSeries.latest.closeTime != inputBar.closeTime) {
                continue
            }
            // Moves trailing stops.
            if (spec.trailingStops) {
                tradeHistory.trailStops(stopLoss.latestValue)
            }
            val signal = strategy.signal
            if (signal === SignalType.EXIT) {
                tradeHistory.exitActiveTrades(currentPrice)
            } else if (signal === SignalType.ENTRY
                && tradeHistory.activeTradeCount < spec.pyramidingLimit
                && tradeHistory.balance > 0
            ) {

                // Calculates 1R risk value.
                val stopLossPrice = stopLoss.latestValue
                val risk = currentPrice - stopLossPrice

                // Computes risk factor. Risks max betSize% of the account on each trade.
                val maxBalanceRisk = tradeHistory.balance * spec.betSize
                var amount = maxBalanceRisk / risk

                // Avoids spending more than the available balance.
                if (amount > tradeHistory.balance / currentPrice) {
                    amount = tradeHistory.balance / currentPrice
                }
                tradeHistory +=
                    TradeRecord(
                        type = spec.tradeType,
                        timestamp = inputBar.closeTime,
                        entryPrice = currentPrice,
                        amount = amount,
                        stopLossPrice = stopLossPrice,
                        trailingStopDistance = currentPrice - stopLossPrice,
                    )
            }
        }

        // Closes trades that remain open in the end.
        val lastPrice: Double = runTimeSeries.latest.close
        tradeHistory.exitActiveTrades(lastPrice)
        val startPrice: Double = runTimeSeries.oldest.open
        return BackTestReport(
            spec = spec,
            tradeHistory = tradeHistory,
            startPrice = startPrice,
            endPrice = lastPrice,
        )
    }
}
