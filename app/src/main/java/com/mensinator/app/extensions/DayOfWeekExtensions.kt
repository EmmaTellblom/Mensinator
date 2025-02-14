package com.mensinator.app.extensions

import androidx.annotation.StringRes
import com.mensinator.app.R
import java.time.DayOfWeek

@get:StringRes
val DayOfWeek.stringRes: Int
    get() = when (this) {
        DayOfWeek.MONDAY -> R.string.mon
        DayOfWeek.TUESDAY -> R.string.tue
        DayOfWeek.WEDNESDAY -> R.string.wed
        DayOfWeek.THURSDAY -> R.string.thu
        DayOfWeek.FRIDAY -> R.string.fri
        DayOfWeek.SATURDAY -> R.string.sat
        DayOfWeek.SUNDAY -> R.string.sun
    }