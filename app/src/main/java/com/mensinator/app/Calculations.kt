package com.mensinator.app

import android.content.Context
import java.time.LocalDate

class Calculations (private val context: Context){
    private val dbHelper = PeriodDatabaseHelper(context)

    fun calculateNextPeriod(advanced: Int): LocalDate? {
        var expectedPeriodDate: LocalDate? = null

        if(advanced==1){
            //go to advanced calculation using 5 latest ovulations
            advancedNextPeriod(5)
        } else{
            //do basic calculation here
            //Use 5 latest periodstartdates (will return list of 6)
            val listPeriodDates = dbHelper.getLatestXPeriodStart(5)
            // Calculate the cycle lengths between consecutive periods
            val cycleLengths = mutableListOf<Long>()
            for (i in 0 until listPeriodDates.size - 1) {
                val cycleLength = java.time.temporal.ChronoUnit.DAYS.between(listPeriodDates[i + 1], listPeriodDates[i])
                cycleLengths.add(cycleLength)
            }
            // Calculate the average cycle length
            val averageLength = cycleLengths.average()
            expectedPeriodDate = listPeriodDates.last().plusDays(averageLength.toLong())
        }

        return expectedPeriodDate
    }

    fun advancedNextPeriod(noOvulations: Int): LocalDate? {
        // Get the list of the latest ovulation dates
        val ovulationDates = dbHelper.getLatestXOvulations(noOvulations)

        if (ovulationDates.isEmpty()) {
            // Return null or handle the case where no ovulations are available
            return null
        }

        var lutealLength = 0
        for (date in ovulationDates) {
            lutealLength += dbHelper.getLutealLengthForPeriod(date)
        }

        // Calculate average luteal length
        val averageLutealLength = lutealLength / ovulationDates.size

        // Get the last ovulation date
        val lastOvulation = dbHelper.getLastOvulation()

        if (lastOvulation == null) {
            // Return null or handle the case where no last ovulation date is available
            return null
        }

        // Calculate the expected period date
        return lastOvulation.plusDays(averageLutealLength.toLong())
    }
}