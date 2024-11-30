package com.mensinator.app.extensions

import java.util.Locale
import kotlin.math.round

fun Double.formatToOneDecimalPoint(): String {
    if (this.isNaN()) return "-"
    return String.format(Locale.getDefault(), "%.1f", this)
}

fun Double.roundToTwoDecimalPoints(): Double {
    return (round(this * 100) / 100)
}