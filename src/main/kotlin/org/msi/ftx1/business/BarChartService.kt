package org.msi.ftx1.business

class BarChartService {

    fun meanVolatility(chart: BarChart) = chart.run {
        Percent((max - min) / mean)
    }

    fun currentVolatility(chart: BarChart) = chart.run {
        Percent((max - min) / (chart.latest?.close ?: (max - min)))
    }

}
