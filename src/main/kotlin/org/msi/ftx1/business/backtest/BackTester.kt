package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.BarChart
import org.msi.ftx1.business.signal.SignalType
import java.time.Instant

var currentTime: Long = System.currentTimeMillis()

object BackTester {

    fun run(spec: BackTestSpec): BackTestReport {
        val inputBars = spec.provider.getCandleChart(
            symbol = spec.symbol,
            interval = spec.runTimeFrame,
            startTime = spec.startTime,
            endTime = spec.endTime
        )
        val inputTimeFrame = inputBars.interval
        val timeSeriesManager = BarChart(spec.symbol, inputTimeFrame, spec.startTime)
        val strategy = spec.strategyFactory.buildStrategy(timeSeriesManager)
        val runTimeSeries = timeSeriesManager.getDownSampledChart(spec.runTimeFrame)
        val tradeHistory = TradeHistory(
            initialBalance = spec.startingBalance
        )

        for (inputBar in inputBars._data) {
            timeSeriesManager += inputBar
            val currentPrice = inputBar.close
            currentTime = inputBar.closeTime*1000

            tradeHistory.updateCurrentPrice(currentPrice)

            // Avoids trading before the next close.
            // TODO: improve detection of new bars in the run time series.
            if (runTimeSeries.latest?.closeTime != inputBar.closeTime) {
                continue
            }

            val signal = strategy.get(0)
            if (signal === SignalType.EXIT_TAKE_PROFIT) {
                tradeHistory.exitActiveTrade()

            } else if (signal === SignalType.ENTRY && tradeHistory.activeTrade == null && tradeHistory.balance > 0) {

                System.err.println("signal entry ${Instant.ofEpochMilli(inputBar.closeTime*1000)}")

                // Computes risk factor. Risks max betSize% of the account on each trade.
                val amount = tradeHistory.balance / currentPrice
                val exposure = tradeHistory.balance * spec.exposure

                tradeHistory +=
                    TradeRecord(
                        feesPercent = spec.feePerTrade,
                        type = spec.tradeType,
                        timestamp = inputBar.closeTime,
                        entryPrice = currentPrice,
                        amount = amount,
                        exposure = exposure
                    )
            }
        }

        // Closes trades that remain open in the end.
        val lastPrice: Double = runTimeSeries.latest?.close ?: Double.NaN
        tradeHistory.exitActiveTrade()
        val startPrice: Double = runTimeSeries.oldest.open

        return BackTestReport(
            spec = spec,
            tradeHistory = tradeHistory,
            startPrice = startPrice,
            endPrice = lastPrice,
        )
    }
}
