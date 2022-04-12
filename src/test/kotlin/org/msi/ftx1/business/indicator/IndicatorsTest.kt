package org.msi.ftx1.business.indicator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.msi.ftx1.business.ChartHelper
import org.msi.ftx1.business.TimeFrame
import kotlin.math.round

class IndicatorsTest {

    @Test
    fun `close indicator`() {

        val chart = ChartHelper(TimeFrame.MIN_5)
            .bar(10.0, 11.0, 20.0, 5.0)
            .bar(11.0, 12.0, 21.0, 6.0)
            .chart

        val close = chart.closePrice

        assertEquals(12.0, close[0])
        assertEquals(11.0, close[1])
        assertEquals(Double.NaN, close[2])
    }

    @Test
    fun `hlc3Price indicator`() {

        val chart = ChartHelper(TimeFrame.MIN_5)
            .bar(10.0, 20.0, 30.0, 10.0)
            .chart

        val close = chart.hlc3Price

        assertEquals(20.0, close[0])
        assertEquals(Double.NaN, close[1])
    }

    @Test
    fun `lowestValue indicator`() {

        val chart = ChartHelper(TimeFrame.MIN_5)
            .bar(11.0)
            .bar(10.0)
            .chart

        val lowestValue = chart.closePrice.lowestValue(2)

        assertEquals(10.0, lowestValue[0])
        assertEquals(11.0, lowestValue[1])
    }

    @Test
    fun `highestValue indicator`() {

        val chart = ChartHelper(TimeFrame.MIN_5)
            .bar(10.0)
            .bar(11.0)
            .chart

        val highestValue = chart.closePrice.highestValue(2)

        assertEquals(11.0, highestValue[0])
        assertEquals(10.0, highestValue[1])
    }

    @Test
    fun `sma indicator`() {

        val chart = ChartHelper(TimeFrame.MIN_5)
            .bar(10.0)
            .bar(20.0)
            .bar(30.0)
            .chart

        val sma = sma(chart.closePrice, 3)

        assertEquals(20.0, sma[0])
        assertEquals(Double.NaN, sma[1])
    }

    @Test
    fun `ema indicator`() {

        val chart = ChartHelper(TimeFrame.MIN_5)
            .bar(4086.29) // 10 NaN
            .bar(4310.01) // 9 NaN
            .bar(4509.08) // 8 NaN
            .bar(4130.37) // 7 NaN
            .bar(3699.99) // 6 4147.15 = SMA
            .bar(3660.02) // 5 3984.77
            .bar(4378.48) // 4 4116.01
            .bar(4640.00) // 3 4290.67
            .bar(5709.99) // 2 4763.78
            .bar(5950.02) // 1 ema: 5159.19
            .bar(6169.98) // 0 ema: 5496.12
            .chart

        val ema = ema(chart.closePrice, 5)

        assertEquals(Double.NaN, round(ema[10]))
        assertEquals(Double.NaN, round(ema[9]))
        assertEquals(Double.NaN, round(ema[8]))
        assertEquals(Double.NaN, round(ema[7]))
        assertEquals(4147.0, round(ema[6]))
        assertEquals(3985.0, round(ema[5]))
        assertEquals(4116.0, round(ema[4]))
        assertEquals(4291.0, round(ema[3]))
        assertEquals(4764.0, round(ema[2]))
        assertEquals(5159.0, round(ema[1]))
        assertEquals(5496.0, round(ema[0]))
    }
}
