package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.BarChart
import org.msi.ftx1.business.BarChartProvider
import org.msi.ftx1.business.TimeFrame
import org.msi.ftx1.business.indicator.*
import org.msi.ftx1.business.signal.*
import java.time.LocalDateTime
import java.time.ZonedDateTime

/** A sample back tester and strategy implementation. */
class BackTestDemo(
    val symbol: String,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val provider: BarChartProvider
) {

    fun start() {

        // Sets up the backtest settings.
        val spec = BackTestSpec(
            symbol = symbol,
            provider = provider,
            startTime = startTime,
            endTime = endTime,
            tradeType = TradeType.LONG,
            // Defines the timeframe the strategy will run on. In this case the strategy will be evaluated every 1 hour,
            // even though the input data consists of 5-minute bars.
            runTimeFrame = TimeFrame.HOUR_1,
            // Starting account balance, in counter currency units. E.g. USD for a BTC/USD pair.
            startingBalance = 100000.0,
            // Risks 2% of the account in each trade.
            exposure = 2.0 / 100.0,
            // The % fee charged by the exchange in each trade. E.g. Binance charges 0.1% per trade.
            feePerTrade = 0.064 / 100.0,
            // Defines the factory method that builds the trading strategy when needed.
            strategyFactory = { seriesManager -> makeStrategy(seriesManager) },
        )

        // Runs the backtest over the whole test dataset.
        val report = BackTester.run(spec)

        // Prints out the report
        report.print()
    }

    private fun makeStrategy(chart: BarChart): Strategy {

        // The timeframes our indicators will use.
        val close = chart.closePrice.withLog("close")
        val ema8 = ema(close, 8).withLog("EMA8")

        return Strategy(
            // Identifies when to enter a trade.
            entrySignal = close crossedOver ema8,
            // Identifies when to exit active trades.
            exitSignal = close crossedUnder ema8,
        )
    }

}
