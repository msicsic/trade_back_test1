package org.msi.ftx1.infra

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import org.msi.ftx1.business.CandleChartProvider
import org.msi.ftx1.business.OrderBookProvider
import org.msi.ftx1.infra.remote.CandleChartAdapterFTX
import org.msi.ftx1.infra.remote.ftx.FtxClient

class Config {

    lateinit var objectMapper: ObjectMapper
    lateinit var httpClient: HttpHandler
    lateinit var ftxClient: FtxClient
    // lateinit var mainController: MainController
    lateinit var candleChartProvider: CandleChartProvider
    lateinit var orderBookProvider: OrderBookProvider
    // lateinit var marketProvider: MarketProvider
    // lateinit var barChartService: BarChartService

    fun configure(): Config {

        objectMapper = KotlinModule.Builder().build()
            .asConfigurable()
            .withStandardMappings()
            .done()
            .deactivateDefaultTyping()  // other Jackson config...
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        httpClient = JavaHttpClient()
        ftxClient = FtxClient(httpClient, objectMapper)

        val mainProvider = CandleChartAdapterFTX(ftxClient)
        val cachedCandleChartProvider = CandleBarProviderWithCache(mainProvider)
        candleChartProvider = mainProvider
        orderBookProvider = mainProvider
        // marketProvider = MarketAdapterFTX(ftxClient)
        // barChartService = BarChartService()
        // mainController = MainController(ftxClient, objectMapper)

        return this
    }

}
