package org.msi.ftx1.business

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DateUtilsTest {

    @Test
    fun `should convert ZonedDateTime to seconds and back to ZonedDateTime`() {
        val zonedDateTime = ZonedDateTime.of(2022, 3, 15, 18, 35, 22, 0, ZoneId.systemDefault())
        val seconds = zonedDateTime.seconds
        val convertedDateTime = seconds.secondsToZonedDateTime
        val convertedSeconds = convertedDateTime.seconds

        assertEquals(seconds, convertedSeconds)
    }

}
