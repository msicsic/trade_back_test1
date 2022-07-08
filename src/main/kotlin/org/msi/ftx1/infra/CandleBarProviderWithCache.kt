package org.msi.ftx1.infra

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.http4k.asByteBuffer
import org.msi.ftx1.business.BarChart
import org.msi.ftx1.business.CandleChartProvider
import org.msi.ftx1.business.TimeFrame
import org.msi.ftx1.infra.persist.BarChartDTO
import org.msi.ftx1.infra.persist.toDTO
import java.nio.file.Files
import java.nio.file.Path
import java.time.ZonedDateTime

class CandleBarProviderWithCache(
    private val provider: CandleChartProvider
) : CandleChartProvider {
    private val mapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
    private val path = Path.of("./cached_bar_chart.json")

    override fun getCandleChart(
        symbol: String,
        interval: TimeFrame,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ): BarChart {
        val result = getCachedData()
        return if (result == null) {
            val data = provider.getCandleChart(symbol, interval, startTime, endTime)
            writeCache(data)
            data
        } else {
            result
        }
    }

    private fun writeCache(chart: BarChart) {
        val json = mapper.writeValueAsString(chart.toDTO())
        Files.write(path, json.asByteBuffer().array())
        System.err.println("Cached saved")
    }

    private fun getCachedData(): BarChart? {
        if (Files.exists(path)) {
            val json = String(Files.readAllBytes(path))
            System.err.println("Cached loaded")
            val dto: BarChartDTO = mapper.readValue(json)
            return dto.toBO()
        }
        return null
    }

}
