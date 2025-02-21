package com.mensinator.app.extensions

import androidx.annotation.StringRes
import com.mensinator.app.R
import java.time.Month
import java.time.YearMonth

@get:StringRes
val Month.stringRes: Int
    get() = when (this) {
        Month.JANUARY -> R.string.january
        Month.FEBRUARY -> R.string.february
        Month.MARCH -> R.string.march
        Month.APRIL -> R.string.april
        Month.MAY -> R.string.may
        Month.JUNE -> R.string.june
        Month.JULY -> R.string.july
        Month.AUGUST -> R.string.august
        Month.SEPTEMBER -> R.string.september
        Month.OCTOBER -> R.string.october
        Month.NOVEMBER -> R.string.november
        Month.DECEMBER -> R.string.december
    }

infix fun YearMonth.until(other: YearMonth): Sequence<YearMonth> =
    generateSequence(this) { if (it < other) it.plusMonths(1) else null }
