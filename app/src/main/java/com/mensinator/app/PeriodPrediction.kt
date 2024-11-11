package com.mensinator.app

import java.time.LocalDate

class PeriodPrediction(
    private val dbHelper: IPeriodDatabaseHelper,
    private val calcHelper: ICalculationsHelper,
) : IPeriodPrediction {

    override fun getPredictedPeriodDate(): LocalDate {
        val periodCount = dbHelper.getPeriodCount()
        return if (periodCount >= 2) {
            calcHelper.calculateNextPeriod()
        } else {
            LocalDate.parse("1900-01-01")
        }
    }
}