package com.mensinator.app.business

import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface IPeriodPrediction {
    fun getPredictedPeriodDate(): LocalDate?

    @Deprecated("HACK: Must be replaced by a service/repository that only updates the period prediction date consistently")
    fun updatePeriodPrediction()

    val predictedPeriodDate: StateFlow<LocalDate?>
}
