package com.mensinator.app.business

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class PeriodPrediction(
    private val dbHelper: IPeriodDatabaseHelper,
    private val calcHelper: ICalculationsHelper,
) : IPeriodPrediction {

    private val _predictedPeriodDate = MutableStateFlow<LocalDate?>(null)

    override fun getPredictedPeriodDate(): LocalDate? {
        val periodCount = dbHelper.getPeriodCount()
        if (periodCount < 2) {
            return null
        }

        return calcHelper.calculateNextPeriod()
    }

    @Deprecated("HACK: Must be replaced by a service/repository that only updates the period prediction date consistently")
    override fun updatePeriodPrediction() {
        _predictedPeriodDate.tryEmit(getPredictedPeriodDate())
    }

    override val predictedPeriodDate: StateFlow<LocalDate?>
        get() = _predictedPeriodDate
}