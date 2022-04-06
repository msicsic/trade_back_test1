//package org.msi.ftx1.business.indicator
//
//import org.msi.ftx1.business.BarChart
//
///**
// * Keltner channel indicators.
// * {@see https://school.stockcharts.com/doku.php?id=technical_indicators:keltner_channels}
// *
// * Usage:
// *    val keltner = h1.keltnerChannel(length=20, atrMultiplier = 2.0)
// *    val keltnerUpper = keltner.upperChannel
// *    val keltnerLower = keltner.lowerChannel
// */
//fun BarChart.keltnerChannel(length: Int = 20, atrLength: Int = 10, atrMultiplier: Double = 2.0): KeltnerChannel {
//    val ema = typicalPrice.ema(length)
//    val atr = averageTrueRange(atrLength)
//
//    return KeltnerChannel(ema, atr, atrMultiplier)
//}
//
//class KeltnerChannel internal constructor(
//    private val ema: Indicator,
//    private val atr: Indicator,
//    private val atrMultiplier: Double = 2.0,
//) : Indicator {
//
//    override fun getValue(index: Int): Double? = ema[index]
//
//    val upperChannel: Indicator
//        get() = Indicator { index ->
//            (this[index] ?: return@Indicator null) + atrMultiplier * (atr[index] ?: return@Indicator null)
//        }
//
//    val lowerChannel: Indicator
//        get() = Indicator { index ->
//            (this[index] ?: return@Indicator null) + atrMultiplier * (atr[index] ?: return@Indicator null)
//        }
//}
