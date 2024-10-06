package com.mensinator.app

import android.content.Context
import android.util.Log
import java.time.LocalDate

class OvulationPrediction (context: Context) : Prediction(context) {
    private val periodPrediction = PeriodPrediction(context)
    private var periodPredictionDate = periodPrediction.getPredictedPeriodDate()
    private lateinit var ovulationDatePrediction: LocalDate
    private val lastOvulationDate = dbHelper.getNewestOvulationDate()
    private val firstDayOfNextMonth = LocalDate.now().withDayOfMonth(1).plusMonths(1)
    private val previousFirstPeriodDate = dbHelper.getFirstPreviousPeriodDate(firstDayOfNextMonth)

    fun getPredictedOvulationDate(): LocalDate {
        val ovulationCount = dbHelper.getOvulationCount()

        // No data at all in database
        if (lastOvulationDate == null || previousFirstPeriodDate == null) {
            Log.e("TAG", "Null values found: lastOvulationDate=$lastOvulationDate, previousFirstPeriodDate=$previousFirstPeriodDate")
            return LocalDate.parse("1900-01-01")
        }

        if (ovulationCount >=2 && periodCount >= 2 && lastOvulationDate < previousFirstPeriodDate) {
            Log.d("TAG", "Inside if statement")
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
        }   // If Ovulation is after previous first period date and prediction exists for Period, calculate next ovulation based on calculated start of period
        else if (lastOvulationDate > previousFirstPeriodDate && (periodPredictionDate != LocalDate.parse("1900-01-01"))) {
            Log.d("TAG", "IM ALIVE")
            val follicleGrowthDays = calcHelper.averageFollicalGrowthInDays()
            val follicleGrowthDaysLong = follicleGrowthDays.toLong()

            if(follicleGrowthDaysLong > 0){
                ovulationDatePrediction = periodPredictionDate.plusDays(follicleGrowthDaysLong)
            }
            else{
                // Return a default value, not enough data to predict ovulation, averageFollicalGrowthInDays() returns 0
                ovulationDatePrediction = LocalDate.parse("1900-01-01")
            }
        }
        else{
            Log.d("TAG", "THERE ARE NOW OVULATIONS")
            // Return a default value, not enough data to predict ovulation, ovulationCount < 2
            ovulationDatePrediction = LocalDate.parse("1900-01-01")
        }
        // Valid prediction
        return ovulationDatePrediction
    }

//    fun getOvulationDatePrediction(): LocalDate {
//        return ovulationDatePrediction
//    }
}