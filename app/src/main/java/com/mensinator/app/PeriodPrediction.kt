package com.mensinator.app

import android.content.Context
import java.util.Date

class PeriodPrediction(context: Context) : Prediction(context) {

    private val lutealCalculation = dbHelper.getSettingByKey("luteal_period_calculation")
    private val periodHistory = dbHelper.getSettingByKey("period_history")?.value?.toIntOrNull() ?: 5

    private lateinit var periodDatePrediction: Date

    fun getPredictedPeriodDate(): Date {
        return periodDatePrediction
    }

}