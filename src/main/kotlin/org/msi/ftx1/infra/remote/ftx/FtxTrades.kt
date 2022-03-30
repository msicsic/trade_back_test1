package org.msi.ftx1.infra.remote.ftx

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class FtxTrades(
    val success: Boolean,
    val result: List<FtxTradeEntry>
)

data class FtxTradeEntry(
    val id: Long,
    val liquidation: Boolean,
    val price: Double,
    val side: String,
    val size: Double,
    val time: String
) {
    val timeAsDate get(): LocalDateTime = LocalDateTime.parse(time, dateParserTrades)
    val timeAsMs get(): Long = timeAsDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val timeAsSeconds get(): Long = timeAsMs / 1000

    companion object {
        private val dateParserTrades = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'+00:00'")
    }
}
