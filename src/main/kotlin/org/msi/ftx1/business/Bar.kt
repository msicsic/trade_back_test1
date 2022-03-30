package org.msi.ftx1.business

data class Bar(
    val interval: TimeFrame,
    val openTime: Long,
    var open: Double,
    var close: Double,
    var high: Double,
    var low: Double,
    var volume: Double,
    val valid: Boolean = true
) {
    val closeTime = openTime + interval.seconds

    constructor(interval: TimeFrame, timeSeconds: Long) : this(
        openTime = timeSeconds,
        interval = interval,
        open = 0.0,
        close = 0.0,
        high = 0.0,
        low = 0.0,
        volume = 0.0,
        valid = false
    )

    constructor(
        timeFrame: TimeFrame,
        otherBar: Bar,
    ) : this(
        interval = timeFrame,
        openTime = otherBar.openTime,
        open = otherBar.open,
        high = otherBar.high,
        low = otherBar.low,
        close = otherBar.close,
        volume = otherBar.volume,
    )

    operator fun plusAssign(otherBar: Bar) {
        close = otherBar.close
        volume += otherBar.volume
        if (otherBar.high > high) {
            high = otherBar.high
        }
        if (otherBar.low < low) {
            low = otherBar.low
        }
    }

    operator fun plusAssign(price: Double) {
        close = price
        if (open == 0.0) {
            open = price
        }
        if (price > high) {
            high = price
        }
        if (price < low) {
            low = price
        }
    }

    fun includesTimestamp(timestamp: Long): Boolean = timestamp in openTime until closeTime
}
