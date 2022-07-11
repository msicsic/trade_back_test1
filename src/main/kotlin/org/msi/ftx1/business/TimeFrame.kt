package org.msi.ftx1.business

// TODO: le provider peut ne pas supporter ces intervales, il faut gérer cela
enum class TimeFrame(val seconds: Int) {
    SEC_1(1),
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
