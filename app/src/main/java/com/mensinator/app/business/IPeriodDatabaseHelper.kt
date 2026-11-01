package com.mensinator.app.business

import com.mensinator.app.data.Setting
import com.mensinator.app.data.Symptom
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

interface IPeriodDatabaseHelper {
    val dbWriteTrigger: Flow<Unit>

    suspend fun exportAllTablesToJson(): JSONObject
    suspend fun importTableData(tableName: String, jsonArray: JSONArray)
    suspend fun importAppSettings(jsonArray: JSONArray)
    suspend fun executeInTransaction(block: suspend () -> Boolean): Boolean

    // This function is used to add a date together with a period id to the periods table
    suspend fun addDateToPeriod(date: LocalDate, periodId: PeriodId)

    // Get all period dates for a given month
    suspend fun getPeriodDatesForMonth(year: Int, month: Int): Map<LocalDate, PeriodId>

    // Returns how many periods that are in the database
    suspend fun getPeriodCount(): Int

    // This function is used to remove a date from the periods table
    suspend fun removeDateFromPeriod(date: LocalDate)

    // This function is used to get all symptoms from the database
    suspend fun getAllSymptoms(): List<Symptom>

    // This function inserts new symptom into the Database
    suspend fun createNewSymptom(symptomName: String)

    // This function returns all Symptom dates for given month
    suspend fun getSymptomDatesForMonth(year: Int, month: Int): Set<LocalDate>

    suspend fun getSymptomsForDates(): Map<LocalDate, Set<Symptom>>

    // This function is used to update symptom dates in the database
    suspend fun updateSymptomDate(dates: List<LocalDate>, symptomId: List<Int>)

    // This function is used to get symptoms for a given date
    suspend fun getActiveSymptomIdsForDate(date: LocalDate): List<Int>

    suspend fun getSymptomColorForDate(date: LocalDate): List<String>

    // This function is used to get all settings from the database
    suspend fun getAllSettings(): List<Setting>

    // This function is used for updating settings in the database
    suspend fun updateSetting(key: String, value: String): Boolean

    // This function is used to get a setting from the database
    suspend fun getSettingByKey(key: String): Setting?

    // This function wraps getSettingByKey to return a valid string
    suspend fun getStringSettingByKey(key: String): String

    // This function is used for adding/removing ovulation dates from the database
    suspend fun updateOvulationDate(date: LocalDate)

    //Add ovulation to database
    suspend fun addOvulationDate(date: LocalDate)

    // Remove ovulation from database
    suspend fun removeOvulationDate(date: LocalDate)

    // This function is used to get ovulation date for a given month
    suspend fun getOvulationDatesForMonth(year: Int, month: Int): Set<LocalDate>

    // This function is used to get the number of ovulations in the database
    suspend fun getOvulationCount(): Int

    // This function checks if date input should be included in existing period
    // or if a new periodId should be created
    suspend fun newFindOrCreatePeriodID(date: LocalDate): PeriodId

    // Retrieve the previous period's start date from a given date
    suspend fun getFirstPreviousPeriodDate(date: LocalDate): LocalDate?

    // Retrieve the oldest period date in the database
    suspend fun getOldestPeriodDate(): LocalDate?

    // Retrieve the newest ovulation date in the database
    suspend fun getNewestOvulationDate(): LocalDate?

    // Update symptom's active status and color by symptom ID
    suspend fun updateSymptom(id: Int, active: Int, color: String)

    // Retrieve the latest X ovulation dates where they are followed by a period
    suspend fun getLatestXOvulationsWithPeriod(number: Int): List<LocalDate>

    // Retrieve the most recent ovulation date
    suspend fun getLastOvulation(): LocalDate?

    fun lastOvulation(): Flow<LocalDate?>

    // Retrieve the latest X period start dates
    suspend fun getLatestXPeriodStart(number: Int): List<LocalDate>

    //suspend fun latestXPeriodStart(number: Int): Flow<List<LocalDate>>

    // Retrieve the next period start date after a given date
    suspend fun getFirstNextPeriodDate(date: LocalDate): LocalDate?

    // Get the number of dates in a given period
    suspend fun getNoOfDatesInPeriod(date: LocalDate): Int

    // Retrieve the latest X ovulation dates
    suspend fun getXLatestOvulationsDates(number: Int): List<LocalDate>

    // Remove a symptom by its ID
    suspend fun deleteSymptom(symptomId: Int)

    // Get the database version
    fun getDBVersion(): String

    // Rename a symptom by its ID
    suspend fun renameSymptom(symptomId: Int, newName: String)

    // Retrieve the latest period start date
    suspend fun getLatestPeriodStart(): LocalDate?
}
