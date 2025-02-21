package com.mensinator.app.business

import java.time.LocalDate

class PeriodPrediction(
    private val dbHelper: IPeriodDatabaseHelper,
    private val calcHelper: ICalculationsHelper,
) : IPeriodPrediction {

    override fun getPredictedPeriodDate(): LocalDate? {
        val periodCount = dbHelper.getPeriodCount()
        if (periodCount < 2) {
            return null
        }

        return calcHelper.calculateNextPeriod()
    }
}