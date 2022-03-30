package org.msi.ftx1.business

enum class TimeFrame(val seconds: Int) {
    SEC_15(15),
    MIN_1(60),
    MIN_5(5 * 60),
    MIN_15(15 * 60),
    HOUR_1(3600),
    HOUR_4(4 * 3600),
    DAY_1(86400),
    WEEK_1(7 * 86400);

    fun canBeDownSampledTo(other: TimeFrame) =
        other.seconds > this.seconds && other.seconds % this.seconds == 0
}
