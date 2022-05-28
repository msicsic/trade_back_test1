package org.msi.ftx1.business.backtest

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

var currentTime: Long = System.currentTimeMillis()

// TODO: tempo solution for travel... => put this in the configuration
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

object BackTester {


    fun run(spec: BackTestSpec, strategy: TradeStrategy): BackTestReport {
        val inputBars = CandleBarProviderWithCache(spec.provider).getCandleChart(
            symbol = spec.symbol,
            interval = spec.runTimeFrame,
            startTime = spec.startTime,
            endTime = spec.endTime
        )
        val inputTimeFrame = inputBars.interval
        val timeSeriesManager = BarChart(spec.symbol, inputTimeFrame, spec.startTime)
        val runTimeSeries = timeSeriesManager.getDownSampledChart(spec.runTimeFrame)
        val tradeHistory = TradeHistory(
            initialBalance = spec.startingBalance,
            strategy = strategy
        )

        for (inputBar in inputBars._data) {
            timeSeriesManager += inputBar
            currentTime = inputBar.openTime * 1000

            tradeHistory.updateCurrentPrice(timeSeriesManager, inputBar.close, inputBar.high, inputBar.low, currentTime)

            // Avoids trading before the next close.
            // TODO: improve detection of new bars in the run time series.
            if (runTimeSeries.latest?.closeTime != inputBar.closeTime) {
                continue
            }
        }

        // Closes trades that remain open in the end.
        val lastPrice: Double = runTimeSeries.latest?.close ?: Double.NaN
        tradeHistory.exitActiveTrade()
        val startPrice: Double = runTimeSeries.oldest.open

        return BackTestReport(
            spec = spec,
            tradeHistory = tradeHistory,
            startPrice = startPrice,
            endPrice = lastPrice,
        )
    }
}
