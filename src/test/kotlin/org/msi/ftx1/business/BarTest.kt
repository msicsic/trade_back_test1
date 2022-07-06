package org.msi.ftx1.business

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

internal class BarTest {

    @Test
    fun shouldIncludeTickTimestamp() {
        val openTime = Instant.parse("2019-04-19T20:00:00Z").epochSecond
        val bar = Bar(TimeFrame.MIN_5, openTime)
        val tickTime = Instant.parse("2019-04-19T20:04:15Z").epochSecond

        assertEquals(true, bar.includesTimestamp(tickTime))
    }

    @Test
    fun shouldNotIncludeNextOpenTimestamp() {
        val openTime = Instant.parse("2019-04-19T20:00:00Z").epochSecond
        val bar = Bar(TimeFrame.MIN_5, openTime)
        val nextOpen = Instant.parse("2019-04-19T20:05:00Z").epochSecond

        assertEquals(false, bar.includesTimestamp(nextOpen))
    }

    @Test
    fun `should compute end time`() {
        val bar = Bar(TimeFrame.MIN_5, 100)

        assertEquals(100+5*60, bar.closeTimeSeconds)
    }

    @Test
    fun `should raise an exception when add another undefined`() {
        val bar = Bar(TimeFrame.MIN_5, 100)
        val undefinedBar = Bar(TimeFrame.MIN_5, 200)

        assertThrows(IllegalArgumentException::class.java) { bar += undefinedBar }
    }

    @Test
    fun `should add a non undefined bar to a defined bar`() {
        val bar = Bar(TimeFrame.MIN_5, 100)
        val otherBar = Bar(TimeFrame.MIN_5, 100, 15.0, 5.0)

        bar += otherBar

        assertEquals(bar.close, otherBar.close)
        assertEquals(bar.high, otherBar.high)
        assertEquals(bar.low, otherBar.low)
        assertEquals(bar.open, otherBar.open)
        assertEquals(bar.volume, otherBar.volume)
    }
}
