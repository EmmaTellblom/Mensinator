package com.mensinator.app.business

import kotlinx.datetime.LocalDate

class PeriodPrediction(
    private val dbHelper: IPeriodDatabaseHelper,
    private val calcHelper: ICalculationsHelper,
) : IPeriodPrediction {

    override suspend fun getPredictedPeriodDate(): LocalDate? {
        val periodCount = dbHelper.getPeriodCount()
        if (periodCount < 2) {
            return null
        }

        return calcHelper.calculateNextPeriod()
    }
}