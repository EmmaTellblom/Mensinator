package com.mensinator.app.business

import android.util.Log
import com.mensinator.app.extensions.roundToTwoDecimalPoints
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class CalculationsHelper(
    private val dbHelper: IPeriodDatabaseHelper,
) : ICalculationsHelper {
    private val periodHistory
        get() = runBlocking {
            dbHelper.getSettingByKey("period_history")?.value?.toIntOrNull() ?: 5
        }
    private val ovulationHistory
        get() = runBlocking {
            dbHelper.getSettingByKey("ovulation_history")?.value?.toIntOrNull() ?: 5
        }
    private val lutealCalculation
        get() = runBlocking {
            dbHelper.getSettingByKey("luteal_period_calculation")?.value?.toIntOrNull() ?: 0
        }

    override fun calculateNextPeriod(): LocalDate? {
        return if (lutealCalculation == 1) {
            //go to advanced calculation using X latest ovulations (set by user in settings)
            Log.d("TAG", "Advanced calculation")
            advancedNextPeriod()
        } else {
            // Basic calculation using latest period start dates
            Log.d("TAG", "Basic calculation")
            //do basic calculation here
            //Use X latest periodstartdates (will return list of X+1)
            val listPeriodDates = dbHelper.getLatestXPeriodStart(periodHistory)

            if (listPeriodDates.isEmpty()) {
                null
            } else {
                // Calculate the cycle lengths between consecutive periods
                val cycleLengths = mutableListOf<Long>()
                for (i in 0 until listPeriodDates.size - 1) {
                    val cycleLength = java.time.temporal.ChronoUnit.DAYS.between(
                        listPeriodDates[i],
                        listPeriodDates[i + 1]
                    )
                    cycleLengths.add(cycleLength)
                }
                // Calculate the average cycle length
                val averageLength = cycleLengths.average()

                listPeriodDates.last().plusDays(averageLength.toLong())
            }.also {
                Log.d("TAG", "Expected period date Basic: $it")
            }
        }
    }

    /**
     * Advanced calculation for the next expected period date using ovulation data.
     *
     * @return The next expected period date.
     */
    private fun advancedNextPeriod(): LocalDate? {
        // Get the list of the latest ovulation dates
        val ovulationDates = dbHelper.getLatestXOvulationsWithPeriod(ovulationHistory)
        //Log.d("TAG", "Ovulation dates: $ovulationDates")
        if (ovulationDates.isEmpty()) {
            // Return null or handle the case where no ovulations are available
            Log.d("TAG", "Ovulationdates are empty")
            return null
        }

        var lutealLength = 0
        Log.d("TAG", "Ovulation dates: $ovulationDates")
        for (date in ovulationDates) {
            val lutealDay = getLutealLengthForPeriod(date)
            lutealLength += lutealDay
            Log.d("TAG", "Luteal for date $date: $lutealDay")
        }

        // Calculate average luteal length
        val averageLutealLength = lutealLength / ovulationDates.size
        Log.d("TAG", "Average luteal length calc: $averageLutealLength")

        // Get the last ovulation date
        val lastOvulation = dbHelper.getLastOvulation()
        Log.d("TAG", "Last ovulation: $lastOvulation")

        if (lastOvulation == null) {
            // Return null or handle the case where no last ovulation date is available
            Log.d("TAG", "Ovulation is null")
            return null
        }
        val periodDates =
            dbHelper.getLatestXPeriodStart(ovulationHistory) //This always returns no+1 period dates
        if (periodDates.isNotEmpty() && periodDates.last() > lastOvulation) { //Check the latest first periodDate
            //There is a period start date after last ovulation date
            //We need to recalculate according to next calculated ovulation
            val avgGrowthRate = averageFollicalGrowthInDays()
            val expectedOvulation = periodDates.last().plusDays(avgGrowthRate.toInt().toLong())
            val expectedPeriodDate = expectedOvulation.plusDays(averageLutealLength.toLong())
            Log.d("TAG", "Calculating according to calculated ovulation: $expectedPeriodDate")
            return expectedPeriodDate
        } else {
            val expectedPeriodDate = lastOvulation.plusDays(averageLutealLength.toLong())
            Log.d("TAG", "Next expected period: $expectedPeriodDate")
            // Calculate the expected period date
            return expectedPeriodDate
        }
    }

    //Returns average number of days from first last period to ovulation for passed X periods
    override fun averageFollicalGrowthInDays(): Double {
        val ovulationDates = dbHelper.getXLatestOvulationsDates(ovulationHistory)
        if (ovulationDates.isEmpty()) {
            // Return a meaningful message or handle the case where no ovulations are available
            return 0.0
        } else {
            val growthRate = mutableListOf<Int>()
            for (d in ovulationDates) {
                val firstDatePreviousPeriod = dbHelper.getFirstPreviousPeriodDate(d)
                if (firstDatePreviousPeriod != null) {
                    growthRate.add(
                        d.toEpochDay().toInt() - firstDatePreviousPeriod.toEpochDay().toInt()
                    )
                }
            }
            if (growthRate.isEmpty()) {
                return 0.0
            }
            return growthRate.average().roundToTwoDecimalPoints()
        }
    }

    // Takes the X (periodHistory) latest period start dates and calculates the average cycle length
    // X comes from app_settings in the database
    override fun averageCycleLength(): Double {
        val periodDates = dbHelper.getLatestXPeriodStart(periodHistory)

        // Check if there are enough dates to calculate cycle lengths
        if (periodDates.size < 2) {
            // Not enough data to calculate cycle lengths
            return 0.0
        }

        // List to store cycle lengths
        val cycleLengths = mutableListOf<Long>()

        // Calculate the cycle lengths between consecutive periods
        for (i in 0 until periodDates.size - 1) {
            val cycleLength =
                java.time.temporal.ChronoUnit.DAYS.between(periodDates[i], periodDates[i + 1])
            cycleLengths.add(cycleLength)
        }

        // Calculate the average cycle length
        val cycleLengthAverage = cycleLengths.average()

        return cycleLengthAverage
    }

    override fun averagePeriodLength(): Double {
        val daysInPeriod = mutableListOf<Int>()

        // Retrieve the start dates of the latest periods
        val periodDates = dbHelper.getLatestXPeriodStart(periodHistory)

        // Iterate through dates, except the last one because that can be the current one
        for (i in 0 until periodDates.size - 1) {
            // Calculate the number of dates in the period between consecutive dates
            val numberOfDates = dbHelper.getNoOfDatesInPeriod(periodDates[i])
            // Ensure the value is positive and meaningful
            if (numberOfDates > 0) {
                daysInPeriod.add(numberOfDates)
            }
        }

        // Return the average length of periods
        return if (daysInPeriod.isNotEmpty()) {
            daysInPeriod.average()
        } else {
            // Handle the case where no valid periods were found
            0.0
        }
    }

    // Takes the X (ovulationHistory) latest ovulation dates and calculates the average luteal length
    // X comes from app_settings in the database
    override fun averageLutealLength(): Double {
        // List to hold the luteal lengths
        val lutealLengths = mutableListOf<Long>()

        // Get the list of ovulation dates
        val ovulationDates = dbHelper.getLatestXOvulationsWithPeriod(ovulationHistory)

        // Iterate through each ovulation date
        for (ovulationDate in ovulationDates) {
            // Get the date of the first next period
            val nextPeriodDate = dbHelper.getFirstNextPeriodDate(ovulationDate)

            // Check if the nextPeriodDate is not null
            if (nextPeriodDate != null) {
                // Calculate the number of days between the ovulation date and the next period date
                val daysBetween =
                    java.time.temporal.ChronoUnit.DAYS.between(ovulationDate, nextPeriodDate)
                // Add the result to the list
                lutealLengths.add(daysBetween)
            } else {
                Log.d("TAG", "Next period date is null")
                return 0.0
            }
        }
        // Return average of no of luteal lengths
        return lutealLengths.average()
    }


    // This function is used to get the luteal length of a cycle.
    // Date input should only be ovulation date
    // To be used with setting for calculating periods according to ovulation
    override fun getLutealLengthForPeriod(date: LocalDate): Int {
        val firstNextPeriodDate = dbHelper.getFirstNextPeriodDate(date)
        if (firstNextPeriodDate != null) {
            val lutealLength =
                java.time.temporal.ChronoUnit.DAYS.between(date, firstNextPeriodDate).toInt()
            Log.d("TAG", "Luteal for single date PDH $firstNextPeriodDate $date: $lutealLength")
            return lutealLength
        } else {
            return 0
        }

    }
}