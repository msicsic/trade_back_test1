package org.msi.ftx1.business

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.Double.NaN
import java.time.ZoneId
import java.time.ZonedDateTime

internal class BarChartTest {

    @Test
    fun `first index should contain the most recent bar`() {
        val startTime = ZonedDateTime.of(2022, 3, 15, 20, 30, 50, 0, ZoneId.systemDefault())

        val chart = ChartHelper(startTime, TimeFrame.MIN_5)
            .bar(10.0) // 2
            .bar(20.0) // 1
            .bar(30.0) // 0
            .chart

        assertEquals(startTime.seconds, chart[2].openTime)
        assertEquals(startTime.seconds + TimeFrame.MIN_5.seconds * 1, chart[1].openTime)
        assertEquals(startTime.seconds + TimeFrame.MIN_5.seconds * 2, chart[0].openTime)

        assertEquals(chart[0], chart.latest)
        assertEquals(chart[2], chart.oldest)

        assertEquals(30.0, chart[0].close)
        assertEquals(20.0, chart[1].close)
        assertEquals(10.0, chart[2].close)
    }

    @Test
    fun `index greater than the number of bar should return a undefined bar with a correct time`() {
        val startTime = ZonedDateTime.of(2022, 3, 15, 20, 30, 50, 0, ZoneId.systemDefault())

        val chart = ChartHelper(startTime, TimeFrame.MIN_5)
            .bar(10.0)
            .bar(20.0)
            .bar(30.0)
            .chart

        assertEquals(NaN, chart[3].close)
        assertEquals(startTime.seconds - TimeFrame.MIN_5.seconds * 1, chart[3].openTime)
    }

    @Test
    fun downSample() {
        val time = ZonedDateTime.of(2022, 3, 15, 18, 35, 22, 0, ZoneId.systemDefault())
        val timeSeconds = time.seconds

        val interval = TimeFrame.MIN_5
        val chart = BarChart(
            symbol = "BTC",
            startTime = time,
            interval = interval,
            _data = mutableListOf(
                Bar(
                    interval = interval,
                    openTime = timeSeconds,
                    open = 5.0,
                    close = 6.0,
                    high = 10.0,
                    low = 4.0,
                    volume = 10.0
                ),
                Bar(
                    interval = interval,
                    openTime = timeSeconds + interval.seconds * 1,
                    open = 6.0,
                    close = 7.0,
                    high = 11.0,
                    low = 5.0,
                    volume = 10.0
                ),
                Bar(
                    interval = interval,
                    openTime = timeSeconds + interval.seconds * 2,
                    open = 7.0,
                    close = 8.0,
                    high = 10.0,
                    low = 5.0,
                    volume = 10.0
                ),
                Bar(
                    interval = interval,
                    openTime = timeSeconds + interval.seconds * 3,
                    open = 7.0,
                    close = 8.0,
                    high = 15.0,
                    low = 7.0,
                    volume = 5.0
                ),
                Bar(
                    interval = interval,
                    openTime = timeSeconds + interval.seconds * 4,
                    open = 8.0,
                    close = 9.0,
                    high = 14.0,
                    low = 9.0,
                    volume = 5.0
                )
            )
        )

        val newChart = chart.getDownSampledChart(TimeFrame.MIN_15)

        assertEquals(2, newChart.data.size)
        val bar1 = newChart.data[0]
        assertEquals(4.0, bar1.low)
        assertEquals(11.0, bar1.high)
        assertEquals(5.0, bar1.open)
        assertEquals(8.0, bar1.close)
        assertEquals(30.0, bar1.volume)
        assertEquals(timeSeconds, bar1.openTime)
        assertEquals(TimeFrame.MIN_15, bar1.interval)

        val bar2 = newChart.data[1]
        assertEquals(7.0, bar2.low)
        assertEquals(15.0, bar2.high)
        assertEquals(7.0, bar2.open)
        assertEquals(9.0, bar2.close)
        assertEquals(10.0, bar2.volume)
        assertEquals(timeSeconds + TimeFrame.MIN_15.seconds, bar2.openTime)
        assertEquals(TimeFrame.MIN_15, bar2.interval)
    }
}

class ChartHelper(startTime: ZonedDateTime, interval: TimeFrame) {
    val chart = BarChart("TEST", interval, startTime, mutableListOf())
    var currentTime = startTime.seconds

    fun bar(close: Double): ChartHelper {
        chart += Bar(chart.interval, currentTime, close, 1.0)
        currentTime += chart.interval.seconds
        return this
    }
}
