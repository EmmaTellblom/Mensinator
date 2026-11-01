package com.mensinator.app.business

import android.util.Log
import com.mensinator.app.extensions.now
import com.mensinator.app.extensions.roundToTwoDecimalPoints
import com.mensinator.app.widgets.PeriodCalculationWidgetUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.until

class CalculationsHelper(
    private val dbHelper: IPeriodDatabaseHelper,
) : ICalculationsHelper {

    private suspend fun getPeriodHistory(): Int =
        dbHelper.getSettingByKey("period_history")?.value?.toIntOrNull() ?: 5

    private suspend fun getOvulationHistory(): Int =
        dbHelper.getSettingByKey("ovulation_history")?.value?.toIntOrNull() ?: 5

    private suspend fun getLutealCalculation(): Int =
        dbHelper.getSettingByKey("luteal_period_calculation")?.value?.toIntOrNull() ?: 0

    override suspend fun calculateNextPeriod(): LocalDate? {
        return if (getLutealCalculation() == 1) {
            //go to advanced calculation using X latest ovulations (set by user in settings)
            Log.d("TAG", "Advanced calculation")
            advancedNextPeriod()
        } else {
            // Basic calculation using latest period start dates
            Log.d("TAG", "Basic calculation")
            //do basic calculation here
            //Use X latest periodstartdates (will return list of X+1)
            val listPeriodDates = dbHelper.getLatestXPeriodStart(getPeriodHistory())

            if (listPeriodDates.isEmpty()) {
                null
            } else {
                // Calculate the cycle lengths between consecutive periods
                val cycleLengths = mutableListOf<Long>()
                for (i in 0 until listPeriodDates.size - 1) {
                    val cycleLength =
                        listPeriodDates[i].until(listPeriodDates[i + 1], DateTimeUnit.DAY)
                    cycleLengths.add(cycleLength.toLong())
                }
                // Calculate the average cycle length
                val averageLength = cycleLengths.average()

                listPeriodDates.last().plus(averageLength.toLong(), DateTimeUnit.DAY)
            }.also {
                Log.d("TAG", "Expected period date Basic: $it")
            }
        }
    }

    override fun nextPeriod(): Flow<LocalDate?> = dbHelper.dbWriteTrigger.map {
        val result = calculateNextPeriod()
        PeriodCalculationWidgetUpdater.updateWidgetsIfPeriodChanged(result?.toJavaLocalDate())
        result
    }

    /**
     * Advanced calculation for the next expected period date using ovulation data.
     *
     * @return The next expected period date.
     */
    private suspend fun advancedNextPeriod(): LocalDate? {
        // Get the list of the latest ovulation dates
        val ovulationDates = dbHelper.getLatestXOvulationsWithPeriod(getOvulationHistory())
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
            dbHelper.getLatestXPeriodStart(getOvulationHistory()) //This always returns no+1 period dates
        if (periodDates.isNotEmpty() && periodDates.last() > lastOvulation) { //Check the latest first periodDate
            //There is a period start date after last ovulation date
            //We need to recalculate according to next calculated ovulation
            val avgGrowthRate = averageFollicalGrowthInDays()
            val expectedOvulation = periodDates.last().plus(
                avgGrowthRate.toInt().toLong(),
                DateTimeUnit.DAY
            )
            val expectedPeriodDate = expectedOvulation.plus(
                averageLutealLength.toLong(),
                DateTimeUnit.DAY
            )
            Log.d("TAG", "Calculating according to calculated ovulation: $expectedPeriodDate")
            return expectedPeriodDate
        } else {
            val expectedPeriodDate =
                lastOvulation.plus(averageLutealLength.toLong(), DateTimeUnit.DAY)
            Log.d("TAG", "Next expected period: $expectedPeriodDate")
            // Calculate the expected period date
            return expectedPeriodDate
        }
    }

    //Returns average number of days from first last period to ovulation for passed X periods
    override suspend fun averageFollicalGrowthInDays(): Double {
        val ovulationDates = dbHelper.getXLatestOvulationsDates(getOvulationHistory())
        if (ovulationDates.isEmpty()) {
            return 0.0
        }

        val growthRate = ovulationDates.mapNotNull { d ->
            dbHelper.getFirstPreviousPeriodDate(d)?.let { firstDate ->
                d.toEpochDays() - firstDate.toEpochDays()
            }
        }

        return if (growthRate.isNotEmpty()) growthRate.average().roundToTwoDecimalPoints() else 0.0
    }

    // Takes the X (getPeriodHistory()) latest period start dates and calculates the average cycle length
    // X comes from app_settings in the database
    override suspend fun averageCycleLength(): Double {
        val listPeriodDates = dbHelper.getLatestXPeriodStart(getPeriodHistory())
        if (listPeriodDates.size < 2) {
            return 0.0
        }

        val cycleLengths = listPeriodDates.zipWithNext { a, b ->
            a.until(b, DateTimeUnit.DAY)
        }

        return cycleLengths.average()
    }

    override suspend fun averagePeriodLength(): Double {
        val daysInPeriod = mutableListOf<Int>()

        // Retrieve the start dates of the latest periods
        val periodDates = dbHelper.getLatestXPeriodStart(getPeriodHistory())

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

    // Takes the X (getOvulationHistory()) latest ovulation dates and calculates the average luteal length
    // X comes from app_settings in the database
    override suspend fun averageLutealLength(): Double {
        // List to hold the luteal lengths
        val lutealLengths = mutableListOf<Long>()

        // Get the list of ovulation dates
        val ovulationDates = dbHelper.getLatestXOvulationsWithPeriod(getOvulationHistory())

        // Iterate through each ovulation date
        for (ovulationDate in ovulationDates) {
            // Get the date of the first next period
            val nextPeriodDate = dbHelper.getFirstNextPeriodDate(ovulationDate)

            // Check if the nextPeriodDate is not null
            if (nextPeriodDate != null) {
                // Calculate the number of days between the ovulation date and the next period date
                val daysBetween = ovulationDate.until(nextPeriodDate, DateTimeUnit.DAY)
                // Add the result to the list
                lutealLengths.add(daysBetween.toLong())
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
    override suspend fun getLutealLengthForPeriod(date: LocalDate): Int {
        val firstNextPeriodDate = dbHelper.getFirstNextPeriodDate(date)
        if (firstNextPeriodDate != null) {
            val lutealLength = date.until(firstNextPeriodDate, DateTimeUnit.DAY).toInt()
            Log.d("TAG", "Luteal for single date PDH $firstNextPeriodDate $date: $lutealLength")
            return lutealLength
        } else {
            return 0
        }
    }

    override suspend fun getCycleDay(date: LocalDate): Int? {
        // Don't generate cycle numbers for future dates
        if (date > LocalDate.now()) return null

        val lastPeriodStartDate = dbHelper.getFirstPreviousPeriodDate(date) ?: return null
        return lastPeriodStartDate.until(date, DateTimeUnit.DAY).toInt() + 1
    }

    override fun cycleDay(date: LocalDate): Flow<Int?> = dbHelper.dbWriteTrigger.map {
        getCycleDay(date)
    }
}
