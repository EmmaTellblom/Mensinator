package com.mensinator.app.widgets

import java.time.LocalDate

data class WidgetData(
    val daysUntilPeriodWithoutText: String,
    val daysUntilPeriodWithText: String,

    val daysUntilOvulationWithoutText: String,
    val daysUntilOvulationWithText: String,
    val cycleDay: Int?,

    val nextPeriod: LocalDate?
)