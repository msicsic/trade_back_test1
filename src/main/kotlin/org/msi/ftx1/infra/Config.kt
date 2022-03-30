package org.msi.ftx1.infra

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import org.msi.ftx1.business.BarChartProvider
import org.msi.ftx1.business.BarChartService
import org.msi.ftx1.business.MarketProvider
import org.msi.ftx1.infra.controller.MainController
import org.msi.ftx1.infra.remote.BarChartAdapterFTX
import org.msi.ftx1.infra.remote.MarketAdapterFTX
import org.msi.ftx1.infra.remote.ftx.FtxClient

class Config {

    lateinit var objectMapper: ObjectMapper
    lateinit var httpClient: HttpHandler
    lateinit var ftxClient: FtxClient
    lateinit var mainController: MainController
    lateinit var barChartProvider: BarChartProvider
    lateinit var marketProvider: MarketProvider
    var barChartService = BarChartService()

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

        barChartProvider = BarChartAdapterFTX(ftxClient)
        marketProvider = MarketAdapterFTX(ftxClient)

        mainController = MainController(ftxClient, objectMapper)

        return this
    }

}
