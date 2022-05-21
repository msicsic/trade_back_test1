package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.BarChart

var currentTime: Long = System.currentTimeMillis()

object BackTester {

    fun run(spec: BackTestSpec, strategy: TradeStrategy): BackTestReport {
        val inputBars = spec.provider.getCandleChart(
            symbol = spec.symbol,
            interval = spec.runTimeFrame,
            startTime = spec.startTime,
            endTime = spec.endTime
        )
        val inputTimeFrame = inputBars.interval
        val timeSeriesManager = BarChart(spec.symbol, inputTimeFrame, spec.startTime)
        val runTimeSeries = timeSeriesManager.getDownSampledChart(spec.runTimeFrame)
        val tradeHistory = TradeHistory(
            initialBalance = spec.startingBalance,
            strategy = strategy
        )

        for (inputBar in inputBars._data) {
            timeSeriesManager += inputBar
            val currentPrice = inputBar.close
            currentTime = inputBar.openTime * 1000

            tradeHistory.updateCurrentPrice(timeSeriesManager, currentPrice, currentTime)

            // Avoids trading before the next close.
            // TODO: improve detection of new bars in the run time series.
            if (runTimeSeries.latest?.closeTime != inputBar.closeTime) {
                continue
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
