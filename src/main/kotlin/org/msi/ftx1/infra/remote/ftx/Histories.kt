package org.msi.ftx1.infra.remote.ftx

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Histories(
    val success: Boolean,
    val result: List<History>
)

data class History(
    val startTime: String,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Double
) {
    val timeAsDate get(): LocalDateTime = LocalDateTime.parse(startTime, dateParserHistory)
    val timeAsSeconds get(): Long = timeAsDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000

    companion object {
        private val dateParserHistory = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+00:00'")
    }
}
