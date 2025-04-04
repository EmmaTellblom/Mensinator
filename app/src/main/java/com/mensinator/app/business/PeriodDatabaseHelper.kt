package com.mensinator.app.business

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.sqlite.transaction
import com.kizitonwose.calendar.core.atStartOfMonth
import com.mensinator.app.data.Setting
import com.mensinator.app.data.Symptom
import com.mensinator.app.extensions.until
import com.mensinator.app.utils.IDispatcherProvider
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth


typealias PeriodId = Int

/*
This file contains functions to get/set data into the database
For handling database structure, see DatabaseUtils
 */
class PeriodDatabaseHelper(
    context: Context,
    private val dispatcherProvider: IDispatcherProvider,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION),
    IPeriodDatabaseHelper {

    companion object {
        private const val DATABASE_NAME = "periods.db"
        private const val DATABASE_VERSION = 10
        private const val TABLE_PERIODS = "periods"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_PERIOD_ID = "period_id"

        private const val TABLE_SYMPTOMS = "symptoms"

        //we are using variable COLUMN_ID for ID
        private const val COLUMN_SYMPTOM_NAME = "symptom_name"
        private const val COLUMN_SYMPTOM_ACTIVE = "active"

        private const val TABLE_SYMPTOM_DATE = "symptom_date"
        private const val COLUMN_SYMPTOM_DATE = "symptom_date"
        private const val COLUMN_SYMPTOM_ID = "symptom_id"

        private const val TABLE_APP_SETTINGS = "app_settings"

        //we use $COLUMN_ID
        private const val COLUMN_SETTING_KEY = "setting_key"
        private const val COLUMN_SETTING_LABEL = "setting_label"
        private const val COLUMN_SETTING_VALUE = "setting_value"
        private const val COLUMN_SETTING_GROUP_ID = "group_label_id"

        private const val TAG = "PeriodDatabaseHelper"
    }

    override val readableDb: SQLiteDatabase
        get() = readableDatabase

    override val writableDb: SQLiteDatabase
        get() = writableDatabase

    private val surroundingMonthsToConsider = 2L

    //See DatabaseUtils
    override fun onCreate(db: SQLiteDatabase) {
        DatabaseUtils.createDatabase(db)
    }

    //See DatabaseUtils
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        if (oldVersion < 3) {
            DatabaseUtils.createAppSettings(db)
        }

        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_APP_SETTINGS")
            DatabaseUtils.createAppSettingsGroup(db)
            DatabaseUtils.createAppSettings(db)
        }

        if (oldVersion < 5) {
            DatabaseUtils.createOvulationStructure(db)
        }

        if (oldVersion < 6) {
            DatabaseUtils.insertLutealSetting(db)
        }

        if (oldVersion < 7) {
            DatabaseUtils.databaseVersion7(db)
        }
        if (oldVersion < 8) {
            DatabaseUtils.databaseVersion8(db)
        }
        if (oldVersion < 9) {
            DatabaseUtils.databaseVersion9(db)
        }
        if (oldVersion < 10) {
            DatabaseUtils.databaseVersion10(db)
        }
    }

    override fun addDateToPeriod(date: LocalDate, periodId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, date.toString())
            put(COLUMN_PERIOD_ID, periodId)
        }
        val whereClause = "$COLUMN_DATE = ? AND $COLUMN_PERIOD_ID = ?"
        val whereArgs = arrayOf(date.toString(), periodId.toString())
        val rowsUpdated = db.update(TABLE_PERIODS, values, whereClause, whereArgs)
        if (rowsUpdated == 0) {
            db.insert(TABLE_PERIODS, null, values)
        }
    }

    override fun getPeriodDatesForMonth(year: Int, month: Int): Map<LocalDate, PeriodId> {
        val dates = mutableMapOf<LocalDate, PeriodId>()
        val db = readableDatabase

        // Query the database for dates in the specified month and year
        val cursor = db.query(
            TABLE_PERIODS,
            arrayOf(COLUMN_DATE, COLUMN_PERIOD_ID),
            "strftime('%Y', $COLUMN_DATE) = ? AND strftime('%m', $COLUMN_DATE) = ?",
            arrayOf(year.toString(), month.toString().padStart(2, '0')),
            null, null, null
        )

        try {
            // Get column indices
            val dateIndex = cursor.getColumnIndex(COLUMN_DATE)
            val periodIdIndex = cursor.getColumnIndex(COLUMN_PERIOD_ID)

            if (dateIndex != -1 && periodIdIndex != -1) {
                while (cursor.moveToNext()) {
                    val dateStr = cursor.getString(dateIndex)
                    val periodId = cursor.getInt(periodIdIndex)
                    val date = LocalDate.parse(dateStr)
                    dates[date] = periodId
                }
            } else {
                Log.e(
                    TAG,
                    "Column indices are invalid: dateIndex=$dateIndex, periodIdIndex=$periodIdIndex"
                )
            }
        } finally {
            cursor.close()
        }

        return dates
    }

    override suspend fun getPeriodDatesForMonthNew(
        year: Int,
        month: Int
    ): Map<LocalDate, PeriodId> = withContext(dispatcherProvider.IO) {
        val dates = mutableMapOf<LocalDate, PeriodId>()
        val db = readableDatabase

        // Calculate previous, current, and next months
        val currentMonth = YearMonth.of(year, month)
        val startMonth = currentMonth.minusMonths(surroundingMonthsToConsider)
        val endMonth = currentMonth.plusMonths(surroundingMonthsToConsider)
        val months = startMonth until endMonth

        // SQL query to match year and month
        val queryCondition =
            months.joinToString(" OR ") { "(strftime('%Y', $COLUMN_DATE) = ? AND strftime('%m', $COLUMN_DATE) = ?)" }
        val queryArgs = months.flatMap {
            listOf(
                it.year.toString(),
                it.monthValue.toString().padStart(2, '0')
            )
        }.toList().toTypedArray()

        // Execute query
        val cursor = db.query(
            TABLE_PERIODS,
            arrayOf(COLUMN_DATE, COLUMN_PERIOD_ID),
            queryCondition,
            queryArgs,
            null, null, null
        )

        try {
            // Get column indices
            val dateIndex = cursor.getColumnIndex(COLUMN_DATE)
            val periodIdIndex = cursor.getColumnIndex(COLUMN_PERIOD_ID)

            if (dateIndex != -1 && periodIdIndex != -1) {
                while (cursor.moveToNext()) {
                    val dateStr = cursor.getString(dateIndex)
                    val periodId = cursor.getInt(periodIdIndex)
                    val date = LocalDate.parse(dateStr)
                    dates[date] = periodId
                }
            } else {
                Log.e(
                    TAG,
                    "Column indices are invalid: dateIndex=$dateIndex, periodIdIndex=$periodIdIndex"
                )
            }
        } finally {
            cursor.close()
        }

        dates
    }


    override fun getPeriodCount(): Int {
        val db = readableDatabase
        val countQuery = "SELECT COUNT(DISTINCT $COLUMN_PERIOD_ID) FROM $TABLE_PERIODS"
        val cursor = db.rawQuery(countQuery, null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    override fun removeDateFromPeriod(date: LocalDate) {
        val db = writableDatabase
        val whereClause = "$COLUMN_DATE = ?"
        val whereArgs = arrayOf(date.toString())
        val rowsDeleted = db.delete(TABLE_PERIODS, whereClause, whereArgs)
        if (rowsDeleted > 0) {
            Log.d(TAG, "Removed date $date from $TABLE_PERIODS")
        } else {
            Log.d(TAG, "No date $date found in $TABLE_PERIODS to remove")
        }
    }

    override suspend fun getAllSymptoms(): List<Symptom> = withContext(dispatcherProvider.IO) {
        val db = readableDatabase
        val symptoms = mutableListOf<Symptom>()
        val query =
            "SELECT $COLUMN_ID, SUBSTR($COLUMN_SYMPTOM_NAME, 1, 15) AS truncated_name, $COLUMN_SYMPTOM_ACTIVE, color FROM $TABLE_SYMPTOMS ORDER BY $COLUMN_SYMPTOM_NAME"

        val cursor = db.rawQuery(query, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val symptomId = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val symptomName = it.getString(it.getColumnIndexOrThrow("truncated_name"))
                    val symptomActive = it.getInt(it.getColumnIndexOrThrow(COLUMN_SYMPTOM_ACTIVE))
                    val color = it.getString(it.getColumnIndexOrThrow("color"))
                    symptoms.add(Symptom(symptomId, symptomName, symptomActive, color))
                } while (it.moveToNext())
            }
        }
        cursor.close()
        symptoms
    }

    override fun createNewSymptom(symptomName: String) {
        val db = writableDatabase
        val trimmedSymptomName = symptomName.trim()
        val values = ContentValues().apply {
            put(COLUMN_SYMPTOM_NAME, trimmedSymptomName)  // Set the symptom name
            put(COLUMN_SYMPTOM_ACTIVE, 1)  // Set the active column to 1 (true)
        }
        // Insert the new symptom into the symptoms table
        db.insert(TABLE_SYMPTOMS, null, values)
    }

    override fun getSymptomDatesForMonth(year: Int, month: Int): Set<LocalDate> {
        val dates = mutableSetOf<LocalDate>()
        val db = readableDatabase

        // Define the raw SQL query
        val query = """
        SELECT sd.symptom_date
        FROM $TABLE_SYMPTOM_DATE AS sd
        INNER JOIN $TABLE_SYMPTOMS AS s ON sd.$COLUMN_SYMPTOM_ID = s.$COLUMN_ID
        WHERE strftime('%Y', sd.symptom_date) = ? 
        AND strftime('%m', sd.symptom_date) = ? 
        AND s.$COLUMN_SYMPTOM_ACTIVE = 1
    """

        // Execute the query with the provided parameters
        val cursor = db.rawQuery(query, arrayOf(year.toString(), month.toString().padStart(2, '0')))

        try {
            // Get the column index for the date
            val dateIndex = cursor.getColumnIndex("symptom_date")

            if (dateIndex != -1) {
                while (cursor.moveToNext()) {
                    val dateStr = cursor.getString(dateIndex)
                    try {
                        val date = LocalDate.parse(dateStr)
                        // Add the date to the set of dates with active symptoms
                        dates.add(date)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse date string: $dateStr", e)
                    }
                }
            } else {
                Log.e(TAG, "Column index is invalid: dateIndex=$dateIndex")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying for symptom dates", e)
        } finally {
            cursor.close()
        }

        return dates
    }

    override suspend fun getSymptomDatesForMonthNew(year: Int, month: Int): Set<LocalDate> =
        withContext(dispatcherProvider.IO) {
            val dates = mutableSetOf<LocalDate>()
            val db = readableDatabase

            // Calculate previous month and next month
            val currentYearMonth = YearMonth.of(year, month)
            val prevMonth = currentYearMonth.minusMonths(surroundingMonthsToConsider)
            val nextMonth = currentYearMonth.plusMonths(surroundingMonthsToConsider)

            val prevYear = prevMonth.year
            val prevMonthValue = prevMonth.monthValue

            val nextYear = nextMonth.year
            val nextMonthValue = nextMonth.monthValue

            // Define the raw SQL query to get symptom dates for previous, current, and next months
            val query = """
        SELECT sd.symptom_date
        FROM $TABLE_SYMPTOM_DATE AS sd
        INNER JOIN $TABLE_SYMPTOMS AS s ON sd.$COLUMN_SYMPTOM_ID = s.$COLUMN_ID
        WHERE (strftime('%Y', sd.symptom_date) = ? AND strftime('%m', sd.symptom_date) = ?)
           OR (strftime('%Y', sd.symptom_date) = ? AND strftime('%m', sd.symptom_date) = ?)
           OR (strftime('%Y', sd.symptom_date) = ? AND strftime('%m', sd.symptom_date) = ?)
           AND s.$COLUMN_SYMPTOM_ACTIVE = 1
    """

            // Execute the query with the calculated previous, current, and next months
            val cursor = db.rawQuery(
                query,
                arrayOf(
                    year.toString(), month.toString().padStart(2, '0'),
                    prevYear.toString(), prevMonthValue.toString().padStart(2, '0'),
                    nextYear.toString(), nextMonthValue.toString().padStart(2, '0')
                )
            )

            try {
                // Get the column index for the date
                val dateIndex = cursor.getColumnIndex("symptom_date")

                if (dateIndex != -1) {
                    while (cursor.moveToNext()) {
                        val dateStr = cursor.getString(dateIndex)
                        try {
                            val date = LocalDate.parse(dateStr)
                            // Add the date to the set of dates with active symptoms
                            dates.add(date)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse date string: $dateStr", e)
                        }
                    }
                } else {
                    Log.e(TAG, "Column index is invalid: dateIndex=$dateIndex")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying for symptom dates", e)
            } finally {
                cursor.close()
            }

            dates
        }

    override suspend fun getSymptomsForDates(): Map<LocalDate, Set<Symptom>> =
        withContext(dispatcherProvider.IO) {
            val dates = mutableMapOf<LocalDate, MutableSet<Symptom>>()
            val db = readableDatabase

            val query = """
        SELECT sd.$COLUMN_SYMPTOM_DATE, 
               s.$COLUMN_ID, 
               s.$COLUMN_SYMPTOM_NAME, 
               s.$COLUMN_SYMPTOM_ACTIVE, 
               s.color
        FROM $TABLE_SYMPTOM_DATE AS sd
        INNER JOIN $TABLE_SYMPTOMS AS s ON sd.$COLUMN_SYMPTOM_ID = s.$COLUMN_ID
        WHERE s.$COLUMN_SYMPTOM_ACTIVE = 1
    """

            val cursor = db.rawQuery(query, null)

            cursor.use {  // Ensures cursor is closed automatically
                while (it.moveToNext()) {
                    val dateString = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMPTOM_DATE))
                    val date =
                        LocalDate.parse(dateString)  // Assuming it's stored in "YYYY-MM-DD" format

                    val symptomId = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val symptomName = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMPTOM_NAME))
                    val symptomActive = it.getInt(it.getColumnIndexOrThrow(COLUMN_SYMPTOM_ACTIVE))
                    val color = it.getString(it.getColumnIndexOrThrow("color"))

                    val symptom = Symptom(symptomId, symptomName, symptomActive, color)

                    dates.computeIfAbsent(date) { mutableSetOf() }.add(symptom)
                }
            }

            dates
        }

    override fun updateSymptomDate(dates: List<LocalDate>, symptomId: List<Int>) {
        val db = writableDatabase

        // Convert dates to strings for database operations
        val dateStrings = dates.map { it.toString() }

        db.transaction {
            try {
                // Delete existing symptoms for the specified dates
                execSQL(
                    """
              DELETE FROM $TABLE_SYMPTOM_DATE WHERE $COLUMN_SYMPTOM_DATE IN (${
                        dateStrings.joinToString(",") { "?" }
                    })
           """,
                    dateStrings.toTypedArray()
                )

                // Insert new symptoms for the specified dates
                val insertSQL = """
            INSERT INTO $TABLE_SYMPTOM_DATE ($COLUMN_SYMPTOM_DATE, $COLUMN_SYMPTOM_ID) VALUES (?, ?)
        """
                for (date in dates) {
                    for (id in symptomId) {
                        execSQL(insertSQL, arrayOf<Any>(date.toString(), id))
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }
        }
    }

    override suspend fun getActiveSymptomIdsForDate(date: LocalDate): List<Int> =
        withContext(dispatcherProvider.IO) {
            val db = readableDatabase
            val symptoms = mutableListOf<Int>()

            val cursor = db.query(
                TABLE_SYMPTOM_DATE,
                arrayOf(COLUMN_SYMPTOM_ID),
                "$COLUMN_SYMPTOM_DATE = ?",
                arrayOf(date.toString()),
                null, null, null
            )

            cursor.use {
                if (cursor.moveToFirst()) {
                    do {
                        val symptomId =
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYMPTOM_ID))
                        symptoms.add(symptomId)
                    } while (cursor.moveToNext())
                }
            }

            symptoms
        }

    override fun getSymptomColorForDate(date: LocalDate): List<String> {
        val db = readableDatabase
        val query = """
        SELECT s.color
        FROM symptoms s
        INNER JOIN symptom_date sd ON s.id = sd.symptom_id
        WHERE sd.symptom_date = ?
    """

        val cursor = db.rawQuery(query, arrayOf(date.toString()))
        val symptomColors = mutableListOf<String>()

        // Iterate over all rows in the cursor
        if (cursor.moveToFirst()) {
            do {
                val color = cursor.getString(cursor.getColumnIndexOrThrow("color"))
                symptomColors.add(color)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return symptomColors
    }

    override fun getAllSettings(): List<Setting> {
        val settings = mutableListOf<Setting>()
        val db = readableDatabase
        val cursor = db.query(TABLE_APP_SETTINGS, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val key = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_KEY))
            val value = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_VALUE))
            val label = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_LABEL))
            val groupId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SETTING_GROUP_ID))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("setting_type"))
            settings.add(Setting(key, value, label, groupId, type))
        }
        cursor.close()

        return settings
    }

    override suspend fun updateSetting(key: String, value: String): Boolean = withContext(dispatcherProvider.IO) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_SETTING_VALUE, value)
        }
        val rowsUpdated =
            db.update(
                TABLE_APP_SETTINGS,
                contentValues,
                "$COLUMN_SETTING_KEY = ?",
                arrayOf(key)
            )
        rowsUpdated > 0
    }

    override suspend fun getSettingByKey(key: String): Setting? = withContext(dispatcherProvider.IO) {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_APP_SETTINGS,
            null,
            "$COLUMN_SETTING_KEY = ?",
            arrayOf(key),
            null,
            null,
            null
        )
        val setting = if (cursor.moveToFirst()) {
            val key = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_KEY))
            val value = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_VALUE))
            val label = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_LABEL))
            val groupId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SETTING_GROUP_ID))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("setting_type"))
            Setting(key, value, label, groupId, type)
        } else {
            null
        }

        cursor.close()
        setting
    }

    override suspend fun getStringSettingByKey(key: String): String = withContext(dispatcherProvider.IO) {
        val string = getSettingByKey(key)?.value
        if (string == null) {
            Log.e("getStringSettingByKey", "key '$key' is null")
            return@withContext "Unknown"
        }
        string
    }

    override fun updateOvulationDate(date: LocalDate) {
        val db = writableDatabase

        // Convert LocalDate to String for SQLite compatibility
        val dateString = date.toString()

        // Check if the date already exists in the database
        val cursor = db.rawQuery("SELECT * FROM OVULATIONS WHERE date = ?", arrayOf(dateString))

        if (cursor.moveToFirst()) {
            // Date exists, delete it
            db.delete("OVULATIONS", "date = ?", arrayOf(dateString))
        } else {
            // Date does not exist, insert it
            val contentValues = ContentValues().apply {
                put("date", dateString)
            }
            db.insert("OVULATIONS", null, contentValues)
        }

        cursor.close()
    }

    override fun getOvulationDatesForMonth(year: Int, month: Int): Set<LocalDate> {
        val dates = mutableSetOf<LocalDate>()
        val db = readableDatabase

        // Query the database for dates in the specified month and year
        val cursor = db.query(
            "ovulations", // Table name
            arrayOf("date"), // Column name
            "strftime('%Y', date) = ? AND strftime('%m', date) = ?",
            arrayOf(year.toString(), month.toString().padStart(2, '0')),
            null, null, null
        )

        try {
            // Get the column index for the date
            val dateIndex = cursor.getColumnIndex("date")

            if (dateIndex != -1) {
                while (cursor.moveToNext()) {
                    val dateStr = cursor.getString(dateIndex)
                    try {
                        val date = LocalDate.parse(dateStr)
                        // Add the date to the set of dates
                        dates.add(date)
                    } catch (e: Exception) {
                        Log.e("TAG", "Failed to parse date string: $dateStr", e)
                    }
                }
            } else {
                Log.e("TAG", "Column index is invalid: dateIndex=$dateIndex")
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error querying for ovulation dates", e)
        } finally {
            cursor.close()
        }

        return dates
    }

    override suspend fun getOvulationDatesForMonthNew(year: Int, month: Int): Set<LocalDate> =
        withContext(dispatcherProvider.IO) {
            val dates = mutableSetOf<LocalDate>()
            val db = readableDatabase

            try {
                // Calculate the date range
                val currentMonth = YearMonth.of(year, month)

                val rangeStart = currentMonth.minusMonths(surroundingMonthsToConsider).atStartOfMonth()
                val rangeEnd = currentMonth.plusMonths(surroundingMonthsToConsider).atEndOfMonth()

                // Query the database for dates within the calculated range
                val cursor = db.query(
                    "ovulations", // Table name
                    arrayOf("date"), // Column name
                    "date BETWEEN ? AND ?", // Date range condition
                    arrayOf(rangeStart.toString(), rangeEnd.toString()),
                    null, null, null
                )

                try {
                    // Get the column index for the date
                    val dateIndex = cursor.getColumnIndex("date")

                    if (dateIndex != -1) {
                        while (cursor.moveToNext()) {
                            val dateStr = cursor.getString(dateIndex)
                            try {
                                val date = LocalDate.parse(dateStr)
                                // Add the date to the set of dates
                                dates.add(date)
                            } catch (e: Exception) {
                                Log.e("TAG", "Failed to parse date string: $dateStr", e)
                            }
                        }
                    } else {
                        Log.e("TAG", "Column index is invalid: dateIndex=$dateIndex")
                    }
                } finally {
                    cursor.close()
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error querying for ovulation dates", e)
            } finally {
            }

            dates
        }

    override fun getOvulationCount(): Int {
        val db = readableDatabase
        val countQuery = "SELECT COUNT(DISTINCT DATE) FROM OVULATIONS"
        val cursor = db.rawQuery(countQuery, null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }


    override fun newFindOrCreatePeriodID(date: LocalDate): Int {
        val db = readableDatabase
        val dateStr = date.toString()
        var periodId = 1

        // Query to check the current date and adjacent dates
        val query = """
        SELECT DISTINCT PERIOD_ID FROM PERIODS WHERE DATE = ? OR DATE BETWEEN ? AND ?
    """
        val args = arrayOf(dateStr, date.minusDays(1).toString(), date.plusDays(1).toString())
        val cursor = db.rawQuery(query, args)

        try {
            if (cursor.count > 1) {
                cursor.moveToFirst()
                val primaryPeriodID = cursor.getInt(0)
                cursor.moveToNext()
                val secondaryPeriodID = cursor.getInt(0)

                // Merge periods
                val updateQuery = "UPDATE PERIODS SET PERIOD_ID = ? WHERE PERIOD_ID = ?"
                val updateArgs =
                    arrayOf(primaryPeriodID.toString(), secondaryPeriodID.toString())
                db.execSQL(updateQuery, updateArgs)

                periodId = primaryPeriodID
                //Log.d(TAG, "Merged periods. Reusing existing periodId $periodId for date $date")
            } else if (cursor.count == 1) {
                cursor.moveToFirst()
                periodId = cursor.getInt(0)
                //Log.d(TAG, "Found existing periodId $periodId for date $date")
            } else {
                // Create a new period ID
                val maxPeriodIdCursor =
                    db.rawQuery("SELECT MAX($COLUMN_PERIOD_ID) FROM $TABLE_PERIODS", null)
                if (maxPeriodIdCursor.moveToFirst()) {
                    val maxPeriodId =
                        if (maxPeriodIdCursor.moveToFirst()) maxPeriodIdCursor.getInt(0) else 0
                    periodId = maxPeriodId + 1
                    //Log.d(TAG, "No existing periodId found for date $date. Created new periodId $periodId")
                }
                maxPeriodIdCursor.close()
            }
        } finally {
            cursor.close()
        }

        return periodId
    }

    //This function is used to get the previous period start date from given input date
    override fun getFirstPreviousPeriodDate(date: LocalDate): LocalDate? {
        val db = readableDatabase
        var firstLatestDate: LocalDate? = null

        val query = """
        SELECT date, period_id
        FROM periods
        WHERE period_id = (
            SELECT period_id
            FROM periods
            WHERE date <= ?
            ORDER BY date DESC
            LIMIT 1
        )
        ORDER BY date ASC
        LIMIT 1
    """

        val cursor = db.rawQuery(query, arrayOf(date.toString()))

        if (cursor.moveToFirst()) {
            firstLatestDate =
                LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("date")))
        }

        cursor.close()
        return firstLatestDate
    }

    //This function is to get the oldest period date in the database
    override fun getOldestPeriodDate(): LocalDate? {
        val db = readableDatabase
        var oldestPeriodDate: LocalDate? = null

        val query = """
           SELECT DATE FROM PERIODS ORDER BY DATE ASC LIMIT 1 
        """
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            oldestPeriodDate = LocalDate.parse(cursor.getString(0))
        }

        cursor.close()
        return oldestPeriodDate
    }

    //This function is to get the oldest period date in the database
    override fun getNewestOvulationDate(): LocalDate? {
        val db = readableDatabase
        var newestOvulationDate: LocalDate? = null

        val query = """
           SELECT DATE FROM OVULATIONS ORDER BY DATE DESC LIMIT 1 
        """
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            newestOvulationDate = LocalDate.parse(cursor.getString(0))
        }

        cursor.close()
        return newestOvulationDate
    }


    //This function is used for updating symptom active status
    override fun updateSymptom(id: Int, active: Int, color: String) {
        val db = writableDatabase

        // Ensure that active is either 0 or 1
        val newActiveStatus =
            if (active in 0..1) active else throw IllegalArgumentException("Active status must be 0 or 1")

        val contentValues = ContentValues().apply {
            put("active", newActiveStatus)
            put("color", color)
        }

        // Update the symptom with the given ID
        val rowsAffected = db.update(
            "symptoms", // Table name
            contentValues,
            "id = ?", // WHERE clause
            arrayOf(id.toString()) // WHERE arguments
        )

        if (rowsAffected == 0) {
            throw IllegalStateException("No symptom found with ID: $id")
        }
    }

    // This function is used to get the latest X ovulation dates where they are followed by a period
    // Used for calculations
    override fun getLatestXOvulationsWithPeriod(number: Int): List<LocalDate> {
        val ovulationDates = mutableListOf<LocalDate>()
        val db = readableDatabase
        // only include ovulations that has a period coming afterwards
        val query = """
        SELECT o.DATE
        FROM OVULATIONS o
        JOIN PERIODS p ON p.DATE > o.DATE
        GROUP BY o.DATE
        ORDER BY o.DATE DESC
        LIMIT ?
    """
        val cursor = db.rawQuery(query, arrayOf(number.toString()))

        if (cursor.moveToFirst()) {
            do {
                // Assuming the DATE column is stored as a string in the format yyyy-MM-dd
                val dateString = cursor.getString(0)
                val date = LocalDate.parse(dateString)
                ovulationDates.add(date)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return ovulationDates
    }

    override fun getLastOvulation(): LocalDate? {
        val db = readableDatabase
        val query = """
        SELECT DATE FROM OVULATIONS ORDER BY DATE DESC LIMIT 1
    """

        val cursor = db.rawQuery(query, null)
        var ovulationDate: LocalDate? = null

        if (cursor.moveToFirst()) {
            val dateString = cursor.getString(0)
            ovulationDate = LocalDate.parse(dateString)
        }

        cursor.close()
        return ovulationDate
    }

    override fun getLatestXPeriodStart(number: Int): List<LocalDate> {
        val dateList = mutableListOf<LocalDate>()
        val db = readableDatabase
        val numberOfPeriods = number + 1 // Include the current ongoing period if applicable

        val query = """
        SELECT period_id, MIN(date) AS date
        FROM periods
        WHERE period_id IN (
            SELECT DISTINCT period_id
            FROM periods
            ORDER BY date DESC
            LIMIT ?
        )
        GROUP BY period_id
        ORDER BY date ASC
    """

        val cursor = db.rawQuery(query, arrayOf(numberOfPeriods.toString()))

        if (cursor.moveToFirst()) {
            do {
                val dateString = cursor.getString(1) // Get the date from the second column
                val date = LocalDate.parse(dateString)
                dateList.add(date)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return dateList
    }

    override fun getFirstNextPeriodDate(date: LocalDate): LocalDate? {
        val db = readableDatabase
        var firstNextDate: LocalDate? = null

        // Query to get the first period date that is greater than the provided date
        val query = """
        SELECT date
        FROM periods
        WHERE date > ?
        ORDER BY date ASC
        LIMIT 1
    """

        val cursor = db.rawQuery(query, arrayOf(date.toString()))

        if (cursor.moveToFirst()) {
            firstNextDate = LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("date")))
        }

        cursor.close()
        return firstNextDate
    }

    override fun getNoOfDatesInPeriod(date: LocalDate): Int {
        val db = readableDatabase
        val query = """
            SELECT COUNT(DATE) FROM PERIODS WHERE period_id in (SELECT period_id FROM PERIODS WHERE date = ?)
        """
        val cursor = db.rawQuery(query, arrayOf(date.toString()))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }

        cursor.close()
        return count
    }

    override fun getXLatestOvulationsDates(number: Int): List<LocalDate> {
        val db = readableDatabase
        val ovulationDates = mutableListOf<LocalDate>()
        val query = """
            SELECT DATE FROM OVULATIONS ORDER BY DATE DESC LIMIT ?
        """
        val cursor = db.rawQuery(query, arrayOf(number.toString()))
        if (cursor.moveToFirst()) {
            do {
                val dateString = cursor.getString(0)
                val date = LocalDate.parse(dateString)
                ovulationDates.add(date)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return ovulationDates
    }

    //This function is used to remove a date from the periods table
    override fun deleteSymptom(symptomId: Int) {
        val db = writableDatabase
        val whereClause = "id = ?"
        val whereArgs = arrayOf(symptomId.toString())
        val rowsDeleted = db.delete("symptoms", whereClause, whereArgs)
        if (rowsDeleted > 0) {
            Log.d(TAG, "Deleted symptom from symptoms")
        } else {
            Log.d(TAG, "No symptoms to delete")
        }
    }

    override fun getDBVersion(): String {
        return DATABASE_VERSION.toString()
    }

    override fun renameSymptom(symptomId: Int, newName: String) {
        val db = writableDatabase
        val trimmedSymptomName = newName.trim()
        val contentValues = ContentValues().apply {
            put("symptom_name", trimmedSymptomName)
        }
        db.update("symptoms", contentValues, "id = ?", arrayOf(symptomId.toString()))

    }

    override fun getLatestPeriodStart(): LocalDate? {
        val db = readableDatabase
        val query =
            "SELECT date FROM periods where period_id = (SELECT MAX(period_id) FROM periods) ORDER BY date asc LIMIT 1"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val dateString = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            return LocalDate.parse(dateString)
        }
        cursor.close()
        return null
    }
}
