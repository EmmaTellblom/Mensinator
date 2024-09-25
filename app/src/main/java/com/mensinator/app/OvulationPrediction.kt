package com.mensinator.app

import android.content.Context
import java.time.LocalDate

class OvulationPrediction (context: Context) : Prediction(context) {
    private lateinit var ovulationDatePrediction: LocalDate

    fun getPredictedOvulationDate(): LocalDate {
        val ovulationCount = dbHelper.getOvulationCount()

        if (ovulationCount >=2 && periodCount >= 2) {
            // Get average follicleGrowth based on ovulationHistory
            // Get latest start of period, add days to that date
            val averageOvulationDay = calcHelper.averageFollicalGrowthInDays().toInt()
            if (averageOvulationDay > 0) {
                val latestPeriodStart = dbHelper.getLatestPeriodStart()
                ovulationDatePrediction = latestPeriodStart.plusDays(averageOvulationDay.toLong())
            }
            else{
                // Return a default value, not enough data to predict ovulation, averageFollicalGrowthInDays() returns 0
                ovulationDatePrediction = LocalDate.parse("1900-01-01")
            }
        }
        else{
            // Return a default value, not enough data to predict ovulation, ovulationCount < 2
            ovulationDatePrediction = LocalDate.parse("1900-01-01")
        }
        // Valid prediction
        return ovulationDatePrediction
    }

    fun getOvulationDatePrediction(): LocalDate {
        return ovulationDatePrediction
    }
}