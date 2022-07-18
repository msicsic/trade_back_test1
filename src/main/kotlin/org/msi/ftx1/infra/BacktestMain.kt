package org.msi.ftx1.infra

import org.msi.ftx1.business.*
import org.msi.ftx1.business.backtest.BackTestDemo
import java.time.ZoneId
import java.time.ZonedDateTime

fun main() {
    BacktestConfig().configure().apply {
        Main(
            candleChartProvider = candleChartProvider,
            orderBookProvider = orderBookProvider,
            provider = provider
        ).start()
    }
}


class Main(
    private val candleChartProvider: CandleChartProvider,
    private val orderBookProvider: OrderBookProvider,
    private val provider: SymbolDataProvider
    // private val barChartService: BarChartService,
    // private val marketProvider: MarketProvider
) {

    fun start() {
//
//        val fullChart = barChartProvider.getTrades(
//            symbol = "BTC-PERP",
//            startTime = LocalDateTime.now().minusDays(1),
//            endTime = LocalDateTime.now()
//        )
//
        val symbol = "BTC-PERP"

        val recentTime = ZonedDateTime.of(2022, 5, 15, 0, 0, 0, 0, ZoneId.systemDefault())
        val fromTime = recentTime.minusDays(1)
        val demo = BackTestDemo(symbol, fromTime, recentTime, candleChartProvider, orderBookProvider)

        // TODO: la strategie tps reel doit elle s'appliquer a chaque tick ? dans ce cas les calculs lourds doivent etre délégués dans un thread qui traite par lots (buffer) pour ne pas prendre toutes les ressources
        // Il faut en fait séparer le traitement de la position sur le broker (odres limites, stop loss, take profit) qui doivent etre traités au tick, et le developpement de la strat qui elle peut lagger
        // TODO: le provider doit inclure l'history provider et l'orderbookprovider
        // TODO: la stratégie ne doit pas prendre d'intervale en param, et doit traiter les ticks. Elle peut reconstruire des barres sur différents TF en fonction des besoins de calcul
        // TODO: résolution des ticks? Comme traiter les trades arrivant dans la meme ms ?
        // TODO: utiliser également un footprint, il faut donc bien garder tous les trades les plus fins
        // TODO: provider pour le backtest => branché sur un historique au lieu WS

        // TODO: il faut également dev un scanner de marché qui traque les setups sur les différentes paires

        provider.start()
        provider.addListener(listener = object : SymbolDataConsumer {
            override val symbol = symbol

            override fun orderBookUpdateReceived(orderBook: OrderBook2) {
                System.err.println("orderbook: $orderBook")
            }

            override fun tradesUpdateReceived(tradeHistory: TradeHistory) {
                System.err.println("trades: $tradeHistory")
            }
        })

        demo.start()

//
//        val chart = barChartProvider.getCandleChart(
//            symbol = "BTC-PERP",
//            interval = TimeFrame.MIN_15,
//            startTime = LocalDateTime.now().minusDays(360),
//            endTime = LocalDateTime.now()
//        )
//
//        val volatility = candleChartServFice.currentVolatility(chart)
//        System.err.println("volatility: $volatility, min / max / mean : ${chart.min} / ${chart.max} / ${chart.mean}")
//
//        val futures = marketProvider.getFutureMarkets()
//
//        System.err.println("future markets: $futures")
//        System.err.println("spot markets: " + marketProvider.getSpotMarkets())
//
//        val results = Collections.synchronizedList(mutableListOf<Pair<String, Percent>>())
//        val results2 = Collections.synchronizedList(mutableListOf<Pair<String, CandleChart>>())
//        val time = measureTimeMillis {
//            candleChartProvider.processCharts(
//                symbols = futures.map { it.name },
//                interval = CandleChartInterval.MIN_15,
//                startTime = LocalDateTime.now().minusDays(360),
//                endTime = LocalDateTime.now()
//            ) { c ->
//                results.add(Pair(c.symbol, candleChartService.currentVolatility(c)))
//                results2.add(Pair(c.symbol, c))
//            }
//        }
//        System.err.println("result : " + results.sortedByDescending { it.second })
//        System.err.println("DONE in $time ms")
    }
}

// TODO: etudier l'indicateur Coinbase Premium (the_cryptonian) sur tradingview

/*
TODO LIST

- la strat doit generer et manager des ordre limites au lieu d'ordres market
- faut il placer les ordres en avances (engagement a priori), ou bien avoir des ordres "en attente de validation" qui observent le prix quand entrée dans la zone d'achat
  (au risque de perdre l'entrée si mouvement trop rapide)

TODO: passage d'ordres: securité SL + journaliser

ALGO Structure:
- quand le précédent High ou Low est cassé, on trouve l'autre low ou high dans l'intervalle entre la cassure et le high ou low cassé
- on ne tient pas compte des meches pour les cassures
- apres cassure de structure, on peut chercher un POI de retracement. Generalement au dessus des 50% du mouvement (tendance baissiere) ou haut du mouvement (en tendance haissuere)

TODO: alertes quand prix arrive dans POI => pour analyse en 5s comment le prix reagit => prise de decision trade en fonction
  - idées vrac: trouver une ligne de support qui va dans le sens d'un retournement

TODO: ALGOS
- structure de marché (multi TF) => comment traiter les ranges apres de fort mouvement ? Faut il un second algo ?
- trouver les sommets, avec filtre sur le retracement minimum
- detection des POIs, en ciblant les zones principales (50% retracement, origine mouvement...) et en s'aidant des imbalances
- algo imbalances et mitigation
- detection de trendlines

 */
