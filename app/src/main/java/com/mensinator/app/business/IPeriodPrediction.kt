package com.mensinator.app.business

import java.time.LocalDate

interface IPeriodPrediction {
    suspend fun getPredictedPeriodDate(): LocalDate?
}
