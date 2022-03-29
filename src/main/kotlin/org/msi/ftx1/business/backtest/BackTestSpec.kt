package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.CandleChart
import org.msi.ftx1.business.CandleChartInterval
import org.msi.ftx1.business.CandleStick
import org.msi.ftx1.business.indicator.Indicator
import org.msi.ftx1.business.signal.Strategy

/** Specifies the configuration for a back test run.  */
data class BackTestSpec(
    val tradeType: TradeType = TradeType.LONG,
    /** The base time frame at which the back tester runs. This is typically the time frame of the fastest indicator. */
    val runTimeFrame: CandleChartInterval,
    /** The strategy to back test. */
    val strategyFactory: StrategyFactory,
    /** The input bars to use in backtesting. Must be in the same time frame as the base time frame, or faster. */
    val inputBars: List<CandleStick>,
    /** The stop loss level */
    val stopLoss: StopLossFactory,
    val trailingStops: Boolean,
    /** The % of the account to risk on each trade. 0.01 = 1% */
    val betSize: Double = 0.02,
    /** The initial account balance, in counter currency amount. */
    val startingBalance: Double = 10000.0,
    /** The average fee % charged by the exchange in each trade. */
    val feePerTrade: Double = 0.001,
    var pyramidingLimit: Int = 1,
    /** Level at which we exit 50% of the position. */
    val takeProfitIndicator: Indicator? = null,
)

fun interface StopLossFactory {
    fun buildIndicator(timeSeriesManager: CandleChart): Indicator
}

fun interface StrategyFactory {
    fun buildStrategy(timeSeriesManager: CandleChart): Strategy
}
