package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.BarChart
import org.msi.ftx1.business.BarChartProvider
import org.msi.ftx1.business.TimeFrame
import org.msi.ftx1.business.indicator.Indicator
import org.msi.ftx1.business.signal.Strategy
import java.time.LocalDateTime

/** Specifies the configuration for a back test run.  */
data class BackTestSpec(
    val symbol: String,
    val provider: BarChartProvider,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,

    val tradeType: TradeType,
    /** The base time frame at which the back tester runs. This is typically the time frame of the fastest indicator. */
    val runTimeFrame: TimeFrame,
    /** The strategy to back test. */
    val strategyFactory: StrategyFactory,
    /** The % of the account to risk on each trade. 0.01 = 1% */
    val exposure: Double,
    /** The initial account balance, in counter currency amount. */
    val startingBalance: Double,
    /** The average fee % charged by the exchange in each trade. */
    val feePerTrade: Double
)

fun interface StrategyFactory {
    fun buildStrategy(timeSeriesManager: BarChart): Strategy
}
