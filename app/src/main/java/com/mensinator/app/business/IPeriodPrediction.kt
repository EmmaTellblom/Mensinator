package com.mensinator.app.business

import java.time.LocalDate

interface IPeriodPrediction {
    fun getPredictedPeriodDate(): LocalDate?
}
