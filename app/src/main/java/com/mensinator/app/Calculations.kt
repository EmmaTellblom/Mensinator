package com.mensinator.app

import android.content.Context
import java.time.LocalDate

class Calculations (private val context: Context){
    private val dbHelper = PeriodDatabaseHelper(context)

    fun calculateNextPeriod(advanced: Int): LocalDate? {
        val expectedPeriodDate: LocalDate? = if(advanced==1){
            //go to advanced calculation
            advancedNextPeriod(5)
        } else{
            //do basic calculation here
            null // this is just to avoid issued right now
            // Calculate average cycle length
            //set expectedPeriodDate to last first cycle date + average cycle length
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
            lutealLength += dbHelper.getLutealLength(date)
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