package org.msi.ftx1.business

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

internal class CandleChartTest {

    @Test
    fun downSample() {
        val time = LocalDateTime.of(2022, 3, 15, 18, 35, 22)
        val timeSeconds = time.toEpochSecond(ZoneOffset.UTC)

        val interval = CandleChartInterval.MIN_5
        val chart = CandleChart(
            symbol = "BTC",
            interval = interval,
            startTimeSeconds = timeSeconds,
            endTimeSeconds = timeSeconds + interval.seconds * 5,
            data = listOf(
                CandleStick(
                    timeSeconds = timeSeconds,
                    interval = interval,
                    open = 5f,
                    close = 6f,
                    high = 10f,
                    low = 4f,
                    volume = 10f,
                ),
                CandleStick(
                    timeSeconds = timeSeconds + interval.seconds * 1,
                    interval = interval,
                    open = 6f,
                    close = 7f,
                    high = 11f,
                    low = 5f,
                    volume = 10f,
                ),
                CandleStick(
                    timeSeconds = timeSeconds + interval.seconds * 2,
                    interval = interval,
                    open = 7f,
                    close = 8f,
                    high = 10f,
                    low = 5f,
                    volume = 10f,
                ),
                CandleStick(
                    timeSeconds = timeSeconds + interval.seconds * 3,
                    interval = interval,
                    open = 7f,
                    close = 8f,
                    high = 15f,
                    low = 7f,
                    volume = 5f,
                ),
                CandleStick(
                    timeSeconds = timeSeconds + interval.seconds * 4,
                    interval = interval,
                    open = 8f,
                    close = 9f,
                    high = 14f,
                    low = 9f,
                    volume = 5f,
                )
            )
        )

        val newChart = chart.downSample(CandleChartInterval.MIN_15)

        assertEquals(2, newChart.data.size)
        val bar1 = newChart.data[0]
        assertEquals(4f, bar1.low)
        assertEquals(11f, bar1.high)
        assertEquals(5f, bar1.open)
        assertEquals(8f, bar1.close)
        assertEquals(30f, bar1.volume)
        assertEquals(timeSeconds, bar1.timeSeconds)
        assertEquals(CandleChartInterval.MIN_15, bar1.interval)

        val bar2 = newChart.data[1]
        assertEquals(7f, bar2.low)
        assertEquals(15f, bar2.high)
        assertEquals(7f, bar2.open)
        assertEquals(9f, bar2.close)
        assertEquals(10f, bar2.volume)
        assertEquals(timeSeconds + CandleChartInterval.MIN_15.seconds, bar2.timeSeconds)
        assertEquals(CandleChartInterval.MIN_15, bar2.interval)
    }
}
