package org.msi.ftx1.infra.remote.ftx

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class FtxHistories(
    val success: Boolean,
    val result: List<FtxHistory>
)

data class FtxHistory(
    val startTime: String,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Double
) {
//    val timeAsSeconds get(): Long = LocalDateTime.parse(startTime, dateParserHistory).toInstant(ZoneOffset.UTC).toEpochMilli() / 1000
    val timeAsSeconds get(): Long = ZonedDateTime.of(LocalDateTime.parse(startTime, dateParserHistory), ZoneId.systemDefault()).toEpochSecond()

    companion object {
        private val dateParserHistory = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+00:00'")
    }
}
