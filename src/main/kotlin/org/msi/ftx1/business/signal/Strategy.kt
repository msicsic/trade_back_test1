package org.msi.ftx1.business.signal

/** Translates the individual signal expressions into a resulting trade signal at each time series bar. */
class Strategy(
    /** Identifies when to enter a trade. This depends on the trend signal if one is specified. */
    private val entrySignal: Signal,
    /** Identifies when to exit all active trades. */
    private val exitSignal: Signal,
) {

    /**
     * Computes the trading signal at the specified timeseries bar index.
     */
    operator fun get(index: Int): SignalType =
        when {
            entrySignal.value(index) -> SignalType.ENTRY
            exitSignal.value(index) -> SignalType.EXIT_TAKE_PROFIT
            else -> SignalType.NO_OP
        }
}
