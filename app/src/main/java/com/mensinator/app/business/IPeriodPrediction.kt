package com.mensinator.app.business

import java.time.LocalDate
import kotlinx.collections.immutable.PersistentSet

interface IPeriodPrediction {
    fun getPredictedPeriodDates(): PersistentSet<LocalDate>

}
