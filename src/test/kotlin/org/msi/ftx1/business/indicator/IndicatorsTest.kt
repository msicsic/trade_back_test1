package org.msi.ftx1.business.indicator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.msi.ftx1.business.ChartHelper
import org.msi.ftx1.business.TimeFrame

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
//
//    @Test
//    fun `lowestValue indicator`() {
//
//        val chart = ChartHelper(TimeFrame.MIN_5)
//            .bar(10.0, 20.0, 30.0, 10.0)
//            .bar(11.0, 21.0, 31.0, 11.0)
//            .chart
//
//        val close = chart.closePrice.lowestValue(2)
//
//        assertEquals(20.0, close[0])
//        assertEquals(Double.NaN, close[1])
//    }

}
