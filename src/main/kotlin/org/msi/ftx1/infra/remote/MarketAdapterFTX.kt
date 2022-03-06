package org.msi.ftx1.infra.remote

import org.msi.ftx1.business.Market
import org.msi.ftx1.business.MarketProvider
import org.msi.ftx1.business.MarketType
import org.msi.ftx1.infra.remote.ftx.FtxClient

class MarketAdapterFTX(
    val client: FtxClient
) : MarketProvider {
    override fun getSpotMarkets() =
        client.getSpotMarkets().map { Market(it, MarketType.SPOT) }

    override fun getFutureMarkets() =
        client.getFutureMarkets().map { Market(it, MarketType.FUTURE) }

}
