package com.mensinator.app.business

import android.util.Log
import java.time.LocalDate

class OvulationPrediction(
    private val dbHelper: IPeriodDatabaseHelper,
    private val calcHelper: ICalculationsHelper,
    private val periodPrediction: IPeriodPrediction,
) : IOvulationPrediction {

    override fun getPredictedOvulationDate(): LocalDate? {
        val periodCount = dbHelper.getPeriodCount()
        val ovulationCount = dbHelper.getOvulationCount()
        val periodPredictionDate = periodPrediction.getPredictedPeriodDate()
        val lastOvulationDate = dbHelper.getNewestOvulationDate()
        val firstDayOfNextMonth = LocalDate.now().withDayOfMonth(1).plusMonths(1)
        val previousFirstPeriodDate = dbHelper.getFirstPreviousPeriodDate(firstDayOfNextMonth)

        // No data at all in database
        if (lastOvulationDate == null || previousFirstPeriodDate == null) {
            Log.e("TAG", "Null values found: lastOvulationDate=$lastOvulationDate, previousFirstPeriodDate=$previousFirstPeriodDate")
            return null
        }

        return if (ovulationCount >=2 && periodCount >= 2 && lastOvulationDate < previousFirstPeriodDate) {
            Log.d("TAG", "Inside if statement")
            // Get average follicleGrowth based on ovulationHistory
            // Get latest start of period, add days to that date
            val averageOvulationDay = calcHelper.averageFollicalGrowthInDays().toInt()
            if (averageOvulationDay > 0) {
                val latestPeriodStart = dbHelper.getLatestPeriodStart()
                latestPeriodStart?.plusDays(averageOvulationDay.toLong())
            }
            else {
                // Return a default value, not enough data to predict ovulation, averageFollicalGrowthInDays() returns 0
                null
            }
        }   // If Ovulation is after previous first period date and prediction exists for Period, calculate next ovulation based on calculated start of period
        else if (lastOvulationDate > previousFirstPeriodDate && (periodPredictionDate != LocalDate.parse("1900-01-01"))) {
            Log.d("TAG", "IM ALIVE")
            val follicleGrowthDays = calcHelper.averageFollicalGrowthInDays()
            val follicleGrowthDaysLong = follicleGrowthDays.toLong()

            if (follicleGrowthDaysLong > 0) {
                periodPredictionDate?.plusDays(follicleGrowthDaysLong)
            } else {
                // Return a default value, not enough data to predict ovulation, averageFollicalGrowthInDays() returns 0
               null
            }
        }
        else {
            Log.d("TAG", "THERE ARE NOW OVULATIONS")
            // Return a default value, not enough data to predict ovulation, ovulationCount < 2
            null
        }
    }
}