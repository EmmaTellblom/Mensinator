package com.mensinator.app.business

import java.time.LocalDate

interface IOvulationPrediction {
    fun getPredictedOvulationDate(): LocalDate?
}
