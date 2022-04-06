package org.msi.ftx1.business.indicator

/** The Relative Strength Index (RSI) indicator. */
fun Indicator.rsi(length: Int = 14): Indicator {
    val gainMma = gainIndicator().modifiedMovingAverage(length)
    val lossMma = lossIndicator().modifiedMovingAverage(length)

    return Indicator { index ->
        val avgGain = gainMma.getValue(index) ?: return@Indicator null
        val avgLoss = lossMma.getValue(index) ?: return@Indicator null
        if (avgLoss == 0.0) {
            return@Indicator if (avgGain == 0.0) 0.0 else 100.0
        }
        val relativeStrength = avgGain / avgLoss
        // Compute Relative Strength Index
        return@Indicator 100.0 - 100.0 / (1.0 + relativeStrength)
    }
}

private fun Indicator.gainIndicator(): Indicator =
    Indicator { index ->
        ((this[index] ?: return@Indicator null) - (this[index + 1] ?: return@Indicator null)).coerceAtLeast(.0)
    }

private fun Indicator.lossIndicator(): Indicator =
    Indicator { index ->
        ((this[index + 1] ?: return@Indicator null) - (this[index] ?: return@Indicator null)).coerceAtLeast(.0)
    }
