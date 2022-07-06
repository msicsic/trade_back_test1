package org.msi.ftx1.business.signal

import org.msi.ftx1.business.indicator.Indicator

enum class SignalType {
    /** Do nothing.  */
    NO_OP,

    /** Enter a new trade.  */
    ENTRY,

    /** Exit all active trades.  */
    EXIT_TAKE_PROFIT
}

/**
 * Represents one or more conditions that evaluate to a true/false result signal.
 *
 * Example:
 *    Signal entrySignal = ema8.crossedOver(sma21).and(sma21.isOver(sma30)).and(slope.isPositive());
 */
fun interface Signal {
    fun value(index: Int): Boolean

    operator fun get(index: Int): Boolean = value(index)
}

val Signal.latestValue: Boolean get() = this[0]

// ===========================================================
// Boolean operations used to combine multiple signals.
// ===========================================================

/** Combines the value of the signal with the specified signal using the logical AND operation. */
infix fun Signal.and(otherSignal: Signal): Signal = Signal { index -> this[index] && otherSignal[index] }

/** Combines the value of the signal with the specified signal using the logical OR operation. */
infix fun Signal.or(otherSignal: Signal): Signal = Signal { index -> this[index] || otherSignal[index] }

/** Inverts the value of the signal. */
fun Signal.inverted(): Signal = Signal { index: Int -> !this[index] }

operator fun Signal.not(): Signal = inverted()

// ===========================================================
//  Basic signal indicator expressions.
// ===========================================================

infix fun Indicator.isOver(otherIndicator: Indicator): Signal =
    Signal { index -> get(index) > (otherIndicator[index] ?: return@Signal false) }

infix fun Indicator.isOver(fixedValue: Double): Signal =
    Signal { index -> get(index) > fixedValue }

val Indicator.isPositive: Signal
    get() = Signal { index -> get(index) > 0.0 }

val Indicator.isNegative: Signal
    get() = Signal { index -> get(index) < 0.0 }

infix fun Indicator.isUnder(otherIndicator: Indicator): Signal =
    Signal { index -> get(index) < (otherIndicator[index] ?: return@Signal false) }

infix fun Indicator.isUnder(fixedValue: Double): Signal =
    Signal { index -> get(index) < fixedValue }

infix fun Indicator.crossedOver(otherIndicator: Indicator): Signal = Signal { index ->
    (get(index) > otherIndicator[index]
            && get(index + 1) <= otherIndicator[index + 1])
}

infix fun Indicator.crossedOver(fixedValue: Double): Signal = Signal { index ->
    (get(index) > fixedValue
            && get(index + 1) <= fixedValue)
}

infix fun Indicator.crossedUnder(otherIndicator: Indicator): Signal = Signal { index ->
    (get(index) < otherIndicator[index]
            && get(index + 1) >= otherIndicator[index + 1])
}

infix fun Indicator.crossedUnder(fixedValue: Double): Signal = Signal { index ->
    (get(index) < fixedValue && get(index + 1) >= fixedValue)
}
