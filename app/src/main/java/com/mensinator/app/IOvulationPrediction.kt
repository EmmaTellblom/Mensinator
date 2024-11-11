package com.mensinator.app

import java.time.LocalDate

interface IOvulationPrediction {
    fun getPredictedOvulationDate(): LocalDate
}
