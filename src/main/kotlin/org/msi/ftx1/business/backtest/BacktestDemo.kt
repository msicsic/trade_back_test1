package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.BarChart
import org.msi.ftx1.business.BarChartProvider
import org.msi.ftx1.business.TimeFrame
import org.msi.ftx1.business.indicator.*
import org.msi.ftx1.business.signal.*
import java.time.LocalDateTime

/** A sample back tester and strategy implementation. */
class BackTestDemo(
    val symbol: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
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

    private fun makeStrategy(seriesManager: BarChart): Strategy {

        // The timeframes our indicators will use.
        val h1 = seriesManager.h1 // 1 hour
        val h4 = seriesManager.h4 // 4 hours

        // Creates the 1-hour indicators.
        val close = h1.closePrice
        val high = h1.highPrice
        val low = h1.lowPrice
        val h1ema4 = close.ema(4)
        val h1ema8 = close.ema(8).withLog("ema8")
        val h1sma20 = close.sma(20)
        val h1ema30 = close.ema(30)

        // Creates the 4-hour indicators.
        val h4price: Indicator = h4.closePrice
        val h4ema4: Indicator = h4price.ema(4)
        val h4ema8: Indicator = h4price.ema(8)
        val h4ema15: Indicator = h4price.ema(15)

        return Strategy(
            // Identifies when to enter a trade.
            entrySignal = close crossedOver h1ema8,
            // Identifies when to exit active trades.
            exitSignal = close crossedUnder h1ema8,
        )
    }

}
