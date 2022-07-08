package org.msi.ftx1.business.backtest

import org.msi.ftx1.business.BarChart
import org.msi.ftx1.business.CandleChartProvider
import org.msi.ftx1.business.OrderBookProvider
import org.msi.ftx1.business.TimeFrame
import org.msi.ftx1.business.indicator.closePrice
import org.msi.ftx1.business.indicator.ema
import org.msi.ftx1.business.signal.crossedOver
import org.msi.ftx1.business.signal.crossedUnder
import java.time.ZonedDateTime

interface TradeStrategy {
    fun evaluateTrade(chart: BarChart, history: TradeHistory, currentTime: Long, trade: TradeRecord)
    fun evaluateEntry(chart: BarChart, history: TradeHistory, currentTime: Long)
}

/** A sample back tester and strategy implementation. */
class BackTestDemo(
    val symbol: String,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val barProvider: CandleChartProvider,
    val orderBookProvider: OrderBookProvider
) {

    fun start() {

        // Sets up the backtest settings.
        val spec = BackTestSpec(
            symbol = symbol,
            candleProvider = barProvider,
            orderBookProvider = orderBookProvider,
            startTime = startTime,
            endTime = endTime,
            // Defines the timeframe the strategy will run on. In this case the strategy will be evaluated every 1 hour,
            // even though the input data consists of 5-minute bars.
            runTimeFrame = TimeFrame.HOUR_1,
            // Starting account balance, in counter currency units. E.g. USD for a BTC/USD pair.
            startingBalance = 10000.0,
            // Risks 2% of the account in each trade.
            exposurePercent = 2.0 / 100.0,
            maxLever = 20.0,
            // The % fee charged by the exchange in each trade. E.g. Binance charges 0.1% per trade.
            feePerTrade = 0.064 / 100.0,
        )

        // TODO: inclure des stats sur l'orderbook dans le trading (liquidité, QT pour up ou down d'un %, et surtout calcul de slippage en fonction du trade)

        // TODO: faire un renderer du graphe et des trades
        // TODO: ouvrir des ordres limites en fonction de la bougie courante (ordres dans le futur)
        // TODO: ne pas se baser uniquement sur le close, mais surtout le range de la bougie pour le calcul des trades (ouverture et cloture)

        // trouver les resistances et supports horizontaux, dans les HT en premier, puis dans les LT
        // TODO: algo pour trouver les pivots (sommets et creux) => ceux a tester pour trouver les S/P
        // TODO: detection des imbalances

        // calcul de force de trend (taille des bougies en trend et retracement : voir : https://www.youtube.com/watch?v=6S41IPUs690)
        // => trouver la diminution de la trend (indicateur peut faire le meme job ?)

        // TODO: indicateur Rebond (avec ou sans franchissement ?)
        // TODO: divergences RSI
        // TODO: trouver les MA support/resist d'une bougie (ou de plusieurs?)

        // TODO: cross signal avec close ou low/high
        // TODO: signal de confirmation
        // TODO: signal de rebond sur EMA
        // TODO: BDD / UT sur les calculs et indicateurs / signaux

        // TODO: calcul pivots
        // TODO: algo pour trouver les EMA/SMA/WMA/WVAP/Channels... qui passent par le max de pivots dans un intervale de temps

        // TODO: coRoutines pour chargement parallele des tranches de 1500 candles

        // TODO: volatility graph (absolute volat high-low for each candle) => pour repérer les squizes
        // TODO: volatility graph (relative volat open-close for each candle)
        // TODO: generate html page with graphs (JOCL?, JCuda?, JavaFX?)

        val strategy = object : TradeStrategy {

            override fun evaluateTrade(chart: BarChart, history: TradeHistory, currentTime: Long, trade: TradeRecord) {
                val close = chart.closePrice
                val ema1 = ema(close, 8)

                val longIndicator = close crossedOver ema1
//                if (!longIndicator[0] && trade.type == TradeType.LONG && trade.isOpen) {
//                    trade.exit(false)
//                } else if (longIndicator[0] && trade.type == TradeType.SHORT && trade.isOpen) {
//                    trade.exit(false)
//                }
            }

            override fun evaluateEntry(chart: BarChart, history: TradeHistory, currentTime: Long) {
                val close = chart.closePrice
                val ema1 = ema(close, 8)

                val longIndicator = close crossedOver ema1
                val shortIndicator = close crossedUnder ema1

                if (longIndicator[0] && ! history.hasActiveTrade) {
                    history.openTrade(spec, TradeType.LONG, close[0], currentTime, close[0]*(1.0-spec.exposurePercent))
                }

                if (shortIndicator[0] && ! history.hasActiveTrade) {
                    history.openTrade(spec, TradeType.SHORT, close[0], currentTime, close[0]*(1.0+spec.exposurePercent))
                }
            }
        }

        // Runs the backtest over the whole test dataset.
        val report = BackTester.run(spec, strategy)

        // Prints out the report
        report.print()
    }
}

// TODO: utiliser le volume, en 1 mns les volumes atypiques sont souvent signe de reversal
// TODO: idéalement se baser sur les trades, et non les bars, et reconstruire dynamiquement les bars
