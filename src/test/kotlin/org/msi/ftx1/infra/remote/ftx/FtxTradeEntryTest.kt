package org.msi.ftx1.infra.remote.ftx

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class FtxTradeEntryTest {

    @Test
    fun getTimeAsMs() {
        val entry = FtxTradeEntry(1, false, 100f, "buy", 10f, "2022-03-20T18:16:23.397991+00:00")

        val date = entry.timeAsMs
        assertEquals(1647796583397, date)
    }

    @Test
    fun getTimeAsSeconds() {
        val entry = FtxTradeEntry(1, false, 100f, "buy", 10f, "2022-03-20T18:16:23.397991+00:00")

        val date = entry.timeAsSeconds
        assertEquals(1647796583, date)
    }

    @Test
    fun getTimeAsDate() {
        val entry = FtxTradeEntry(1, false, 100f, "buy", 10f, "2022-03-20T18:16:23.397991+00:00")

        val date = entry.timeAsDate
        assertEquals(2022, date.year)
        assertEquals(20, date.dayOfMonth)
        assertEquals(18, date.hour)
        assertEquals(16, date.minute)
        assertEquals(23, date.second)
        assertEquals(397991000, date.nano)
    }
}
