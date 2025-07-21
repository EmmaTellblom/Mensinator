package com.mensinator.app.business

import java.time.LocalDate

/**
 * This helper provides methods to calculate menstrual cycle related data
 * such as next period date, average cycle length, and average luteal length.
 */
interface ICalculationsHelper {
    /**
     * Calculates the next expected period date.
     *
     * @return The next expected period date.
     */
    fun calculateNextPeriod(): LocalDate?

    /**
     * Calculates the average number of days from the first day of the last period to ovulation.
     *
     * @return The average follicular phase length as a string.
     */
    fun averageFollicalGrowthInDays(): Double

    /**
     * Calculates the average cycle length using the latest period start dates.
     * X comes from app_settings in the database
     * @return The average cycle length as a double.
     */
    fun averageCycleLength(): Double

    /**
     * Calculates the average period length using the latest period start dates.
     *
     * @return The average period length as a double.
     */
    fun averagePeriodLength(): Double

    /**
     * Calculates the average luteal phase length using the latest ovulation dates.
     *
     * @return The average luteal phase length as a double.
     */
    fun averageLutealLength(): Double

    /**
     * Calculates the luteal phase length for a specific cycle.
     *
     * @param date The ovulation date.
     * @return The luteal phase length as an integer.
     */
    fun getLutealLengthForPeriod(date: LocalDate): Int

    /**
     * Calculates the cycle day of a period for a given date.
     *
     * Example:
     * - The latest period began 2025-02-01. The cycle day for 2025-02-01 is 1.
     * - The latest period began 2025-02-01. The cycle day for 2025-02-10 is 10.
     */
    fun getCycleDay(date: LocalDate): Int?
}
