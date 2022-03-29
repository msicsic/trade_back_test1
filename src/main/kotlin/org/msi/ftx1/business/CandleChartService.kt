package org.msi.ftx1.business

import java.time.LocalDateTime

class CandleChartService {

    fun meanVolatility(chart: CandleChart) = chart.run {
        Percent((max - min) / mean)
    }

    fun currentVolatility(chart: CandleChart) = chart.run {
        Percent((max - min) / chart.latest.close)
    }

}
