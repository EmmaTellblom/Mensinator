package com.mensinator.app

import android.content.Context
import java.util.Date

class OvulationPrediction (context: Context) : Prediction(context) {
    private lateinit var ovulationDatePrediction: Date
    private val ovulationHistory = dbHelper.getSettingByKey("ovulation_history")?.value?.toIntOrNull() ?: 5

    fun getPredictedOvulationDate(): Date {
        val ovulationCount = dbHelper.getOvulationCount()
        val periodCount = dbHelper.getPeriodCount()
        val follicleGrowthDays = calcHelper.averageFollicalGrowthInDays()

        // Must be at least 2 ovulations in history to predict ovulation
        // Must be at least 2 periods in history to predict ovulation
        if (ovulationCount >=2 && periodCount >= 2) {
            // Get latest start of period, add days to that date
            //
            //ovulationDatePrediction =
        }

        return ovulationDatePrediction
    }
}