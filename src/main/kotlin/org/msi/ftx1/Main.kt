package org.msi.ftx1

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.client.JavaHttpClient
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import org.msi.ftx1.controller.MainController
import org.msi.ftx1.remote.ftx.FtxClient

fun main() {
    Main().start()
}

class Main {
    lateinit var mainController: MainController

    fun start() {

        val objectMapper = KotlinModule.Builder().build()
            .asConfigurable()
            .withStandardMappings()
            .done()
            .deactivateDefaultTyping()  // other Jackson config...
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)

        val httpClient = JavaHttpClient()
        val ftxClient = FtxClient(httpClient, objectMapper)

        mainController = MainController(ftxClient, objectMapper)
    }
}