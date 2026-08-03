package com.mensinator.app.business

import java.time.LocalDate
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

class PeriodPrediction(
    private val dbHelper: IPeriodDatabaseHelper,
    private val calcHelper: ICalculationsHelper,
) : IPeriodPrediction {

    override fun getPredictedPeriodDates(): PersistentSet<LocalDate> {
        val periodCount = dbHelper.getPeriodCount()
        if (periodCount < 2) {
            return persistentSetOf()
        }

        return calcHelper.calculateNextPeriodDates()
    }
}
