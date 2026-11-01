package com.mensinator.app.business

import kotlinx.datetime.LocalDate

interface IOvulationPrediction {
    suspend fun getPredictedOvulationDate(): LocalDate?
}
