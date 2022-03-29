package org.msi.ftx1.business

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CandleChartIntervalTest {

    @Test
    fun `1 mn interval can be down sampled to 5 mns`() {
        val subject = CandleChartInterval.MIN_1
        assertEquals(true, subject.canBeDownSampledTo(CandleChartInterval.MIN_5))
    }

    @Test
    fun `5 mn interval cannot be down sampled to 1 mn`() {
        val subject = CandleChartInterval.MIN_5
        assertEquals(false, subject.canBeDownSampledTo(CandleChartInterval.MIN_1))
    }
}
