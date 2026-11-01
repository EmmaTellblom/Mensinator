package com.mensinator.app.business

import java.time.LocalDate

interface IOvulationPrediction {
    suspend fun getPredictedOvulationDate(): LocalDate?
}
