package com.mensinator.app.business

import android.database.sqlite.SQLiteDatabase
import com.mensinator.app.data.Setting
import com.mensinator.app.data.Symptom
import java.time.LocalDate

interface IPeriodDatabaseHelper {

    // TODO: The database should only be accessible via the functions of this interface.
    //       Refactor this soon!
    val readableDb: SQLiteDatabase

    // TODO: The database should only be accessible via the functions of this interface.
    //       Refactor this soon!
    val writableDb: SQLiteDatabase

    // This function is used to add a date together with a period id to the periods table
    fun addDateToPeriod(date: LocalDate, periodId: PeriodId)

    // Get all period dates for a given month
    fun getPeriodDatesForMonth(year: Int, month: Int): Map<LocalDate, PeriodId>

    // NEW! Testing new function for getting all period dates month-1, month, month+1
    suspend fun getPeriodDatesForMonthNew(year: Int, month: Int): Map<LocalDate, PeriodId>

    // Returns how many periods that are in the database
    fun getPeriodCount(): Int

    // This function is used to remove a date from the periods table
    fun removeDateFromPeriod(date: LocalDate)

    // This function is used to get all symptoms from the database
    suspend fun getAllSymptoms(): List<Symptom>

    // This function inserts new symptom into the Database
    fun createNewSymptom(symptomName: String)

    // This function returns all Symptom dates for given month
    fun getSymptomDatesForMonth(year: Int, month: Int): Set<LocalDate>
    // NEW! Testing new function for getting all symptom dates month-1, month, month+1
    suspend fun getSymptomDatesForMonthNew(year: Int, month: Int): Set<LocalDate>

    suspend fun getSymptomsForDates(): Map<LocalDate, Set<Symptom>>

    // This function is used to update symptom dates in the database
    fun updateSymptomDate(dates: List<LocalDate>, symptomId: List<Int>)

    // This function is used to get symptoms for a given date
    suspend fun getActiveSymptomIdsForDate(date: LocalDate): List<Int>

    fun getSymptomColorForDate(date: LocalDate): List<String>

    // This function is used to get all settings from the database
    fun getAllSettings(): List<Setting>

    // This function is used for updating settings in the database
    suspend fun updateSetting(key: String, value: String): Boolean

    // This function is used to get a setting from the database
    suspend fun getSettingByKey(key: String): Setting?

    // This function wraps getSettingByKey to return a valid string
    suspend fun getStringSettingByKey(key: String): String

    // This function is used for adding/removing ovulation dates from the database
    fun updateOvulationDate(date: LocalDate)

    // This function is used to get ovulation date for a given month
    fun getOvulationDatesForMonth(year: Int, month: Int): Set<LocalDate>

    //NEW! Testing new function for getting all ovulation dates month-1, month, month+1
    suspend fun getOvulationDatesForMonthNew(year: Int, month: Int): Set<LocalDate>

    // This function is used to get the number of ovulations in the database
    fun getOvulationCount(): Int

    // This function checks if date input should be included in existing period
    // or if a new periodId should be created
    fun newFindOrCreatePeriodID(date: LocalDate): PeriodId

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
    fun getLatestPeriodStart(): LocalDate?
}
