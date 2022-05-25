package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.CandleChartProvider
import org.msi.ftx1.business.TimeFrame
import java.time.ZonedDateTime

/** Specifies the configuration for a back test run.  */
data class BackTestSpec(
    val symbol: String,
    val provider: CandleChartProvider,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,

    /** The base time frame at which the back tester runs. This is typically the time frame of the fastest indicator. */
    val runTimeFrame: TimeFrame,
    /** The % of the account to risk on each trade. 0.01 = 1% */
    val exposurePercent: Double,
    val maxLever: Double,
    /** The initial account balance, in counter currency amount. */
    val startingBalance: Double,
    /** The average fee % charged by the exchange in each trade. */
    val feePerTrade: Double,
)
