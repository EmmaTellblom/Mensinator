package com.mensinator.app.widgets

import android.graphics.Bitmap
import java.time.LocalDate

data class WidgetData(
    val daysUntilPeriodWithoutText: String,
    val daysUntilPeriodWithText: String,
    val daysUntilPeriodBitmap: Bitmap,
    /*
    val daysUntilOvulationWithoutText: String,
    val daysUntilOvulationWithText: String,
    val cycleDay: Int?,

     */
    val nextPeriod: LocalDate?
)