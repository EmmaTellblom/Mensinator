package com.mensinator.app.business

import kotlinx.datetime.LocalDate

interface IPeriodPrediction {
    suspend fun getPredictedPeriodDate(): LocalDate?
}
