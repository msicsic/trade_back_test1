package org.msi.ftx1.business

import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.*
import kotlin.Double.Companion.NaN

data class Bar(
    val interval: TimeFrame,
    val openTimeSeconds: Long,
    var open: Double,
    var close: Double,
    var high: Double,
    var low: Double,
    var volume: Double
) {
    val closeTimeSeconds = openTimeSeconds + interval.seconds

    val openTimeAsDate get() = ZonedDateTime.ofInstant(Date(openTimeSeconds*1000).toInstant(), ZoneId.systemDefault())
    val closeTimeAsDate get() = ZonedDateTime.ofInstant(Date(closeTimeSeconds*1000).toInstant(), ZoneId.systemDefault())

    val undefined get() = open.isNaN() || close.isNaN() || high.isNaN() || low.isNaN() || volume.isNaN()

    constructor(interval: TimeFrame, timeSeconds: Long) : this(
        openTimeSeconds = timeSeconds,
        interval = interval,
        open = NaN,
        close = NaN,
        high = NaN,
        low = NaN,
        volume = NaN,
    )

    constructor(interval: TimeFrame, timeSeconds: Long, close: Double, volume: Double) : this(
        openTimeSeconds = timeSeconds,
        interval = interval,
        open = close,
        close = close,
        high = close,
        low = close,
        volume = volume,
    )

    constructor(
        timeFrame: TimeFrame,
        otherBar: Bar
    ) : this(
        interval = timeFrame,
        openTimeSeconds = otherBar.openTimeSeconds,
        open = otherBar.open,
        high = otherBar.high,
        low = otherBar.low,
        close = otherBar.close,
        volume = otherBar.volume,
    )

    operator fun plusAssign(otherBar: Bar) {
        if (otherBar.undefined) throw java.lang.IllegalArgumentException("Cannot merge an undefined Bar")
        volume = if (volume.isNaN()) otherBar.volume else volume + otherBar.volume
        this += otherBar.close
    }

    operator fun plusAssign(price: Double) {
        close = price
        if (open.isNaN()) {
            open = price
        }
        if (price > high || high.isNaN()) {
            high = price
        }
        if (price < low || low.isNaN()) {
            low = price
        }
    }

    fun includesTimestamp(timestamp: Long): Boolean = timestamp in openTimeSeconds until closeTimeSeconds
}
