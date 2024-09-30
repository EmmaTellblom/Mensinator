package com.mensinator.app

import android.content.Context
import java.time.LocalDate
import java.util.Date

class PeriodPrediction(context: Context) : Prediction(context) {

    private val lutealCalculation = dbHelper.getSettingByKey("luteal_period_calculation")?.value
    private val periodHistory = dbHelper.getSettingByKey("period_history")?.value?.toIntOrNull() ?: 5
    private lateinit var periodDatePrediction: LocalDate

    fun getPredictedPeriodDate() : LocalDate{

        if(periodCount>=2){
            periodDatePrediction = calcHelper.calculateNextPeriod()
        }
        else{
            periodDatePrediction = LocalDate.parse("1900-01-01")
        }

        return periodDatePrediction
    }

    fun getPredictedPeriodDate(date: Date): LocalDate {
        return periodDatePrediction
    }

}