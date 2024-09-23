package com.mensinator.app

import android.content.Context
import java.util.Date

class OvulationPrediction (context: Context) : Prediction(context) {
    private lateinit var ovulationDatePrediction: Date
    private val ovulationHistory = dbHelper.getSettingByKey("ovulation_history")?.value?.toIntOrNull() ?: 5

    fun getPredictedOvulationDate(): Date {
        return ovulationDatePrediction
    }
}