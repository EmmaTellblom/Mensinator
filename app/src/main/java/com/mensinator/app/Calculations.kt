package com.mensinator.app

import android.content.Context
import android.util.Log


class Calculations (private val context: Context){
    private val dbHelper = PeriodDatabaseHelper(context)

    fun calculateNextPeriod(advanced: Int): String {
        val expectedPeriodDate: String

        if(advanced==1){
            //go to advanced calculation using 5 latest ovulations
            Log.d("TAG", "Advanced calculation")
            //expectedPeriodDate = advancedNextPeriod(5)
            expectedPeriodDate = advancedNextPeriod(5)
        } else{
            Log.d("TAG", "Basic calculation")
            //do basic calculation here
            //Use 5 latest periodstartdates (will return list of 6)
            val listPeriodDates = dbHelper.getLatestXPeriodStart(5)
            // Calculate the cycle lengths between consecutive periods
            val cycleLengths = mutableListOf<Long>()
            for (i in 0 until listPeriodDates.size - 1) {
                val cycleLength = java.time.temporal.ChronoUnit.DAYS.between(listPeriodDates[i], listPeriodDates[i + 1])
                cycleLengths.add(cycleLength)
            }
            // Calculate the average cycle length
            val averageLength = cycleLengths.average()
            expectedPeriodDate = listPeriodDates.last().plusDays(averageLength.toLong()).toString()
        }

        return expectedPeriodDate
    }

    fun advancedNextPeriod(noOvulations: Int): String {
        // Get the list of the latest ovulation dates
        val ovulationDates = dbHelper.getLatestXOvulations(noOvulations)
        //Log.d("TAG", "Ovulation dates: $ovulationDates")
        if (ovulationDates.isEmpty()) {
            // Return null or handle the case where no ovulations are available
            Log.d("TAG", "No ovulationdates are empty")
            return "Not enough data"
        }

        var lutealLength = 0
        for (date in ovulationDates) {
            lutealLength += dbHelper.getLutealLengthForPeriod(date)
        }

        // Calculate average luteal length
        val averageLutealLength = lutealLength / ovulationDates.size
        Log.d("TAG", "Average luteal length: $averageLutealLength")

        // Get the last ovulation date
        val lastOvulation = dbHelper.getLastOvulation()
        Log.d("TAG", "Last ovulation: $lastOvulation")

        if (lastOvulation == null) {
            // Return null or handle the case where no last ovulation date is available
            Log.d("TAG", "Ovulation is null")
            return "Not enough data"
        }
        val nextExpectedPeriod = lastOvulation.plusDays(averageLutealLength.toLong()).toString()
        Log.d("TAG", "Test: $nextExpectedPeriod")
        // Calculate the expected period date
        //return lastOvulation.plusDays(averageLutealLength.toLong())
        return nextExpectedPeriod
    }
}