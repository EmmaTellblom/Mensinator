package com.mensinator.app

import android.database.sqlite.SQLiteDatabase
import java.time.LocalDate

interface IPeriodDatabaseHelper {

    // TODO: The database should only be accessible via the functions of this interface.
    //       Refactor this soon!
    val readableDb: SQLiteDatabase

    // TODO: The database should only be accessible via the functions of this interface.
    //       Refactor this soon!
    val writableDb: SQLiteDatabase

    fun addDateToPeriod(date: LocalDate, periodId: Int)

    fun getPeriodDatesForMonth(year: Int, month: Int): Map<LocalDate, Int>

    fun getPeriodCount(): Int

    fun removeDateFromPeriod(date: LocalDate)

    fun getAllSymptoms(): List<Symptom>

    fun createNewSymptom(symptomName: String)

    fun getSymptomDatesForMonth(year: Int, month: Int): Set<LocalDate>

    fun updateSymptomDate(dates: List<LocalDate>, symptomId: List<Int>)

    fun getSymptomsFromDate(date: LocalDate): List<Int>

    fun getSymptomColorForDate(date: LocalDate): List<String>

    fun getAllSettings(): List<Setting>

    fun updateSetting(key: String, value: String): Boolean

    fun getSettingByKey(key: String): Setting?

    fun updateOvulationDate(date: LocalDate)

    fun getOvulationDatesForMonth(year: Int, month: Int): Set<LocalDate>

    fun getOvulationCount(): Int

    fun newFindOrCreatePeriodID(date: LocalDate): Int

    // Retrieve the previous period's start date from a given date
    fun getFirstPreviousPeriodDate(date: LocalDate): LocalDate?

    // Retrieve the oldest period date in the database
    fun getOldestPeriodDate(): LocalDate?

    // Retrieve the newest ovulation date in the database
    fun getNewestOvulationDate(): LocalDate?

    // Update symptom's active status and color by symptom ID
    fun updateSymptom(id: Int, active: Int, color: String)

    // Retrieve the latest X ovulation dates where they are followed by a period
    fun getLatestXOvulationsWithPeriod(number: Int): List<LocalDate>

    // Retrieve the most recent ovulation date
    fun getLastOvulation(): LocalDate?

    // Retrieve the latest X period start dates
    fun getLatestXPeriodStart(number: Int): List<LocalDate>

    // Retrieve the next period start date after a given date
    fun getFirstNextPeriodDate(date: LocalDate): LocalDate?

    // Get the number of dates in a given period
    fun getNoOfDatesInPeriod(date: LocalDate): Int

    // Retrieve the latest X ovulation dates
    fun getXLatestOvulationsDates(number: Int): List<LocalDate>

    // Remove a symptom by its ID
    fun deleteSymptom(symptomId: Int)

    // Get the database version
    fun getDBVersion(): String

    // Rename a symptom by its ID
    fun renameSymptom(symptomId: Int, newName: String)

    // Retrieve the latest period start date
    fun getLatestPeriodStart(): LocalDate
}
