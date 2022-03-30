package org.msi.ftx1.business

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class BarChartIntervalTest {

    @Test
    fun `1 mn interval can be down sampled to 5 mns`() {
        val subject = TimeFrame.MIN_1
        assertEquals(true, subject.canBeDownSampledTo(TimeFrame.MIN_5))
    }

    @Test
    fun `5 mn interval cannot be down sampled to 1 mn`() {
        val subject = TimeFrame.MIN_5
        assertEquals(false, subject.canBeDownSampledTo(TimeFrame.MIN_1))
    }
}
