package com.mensinator.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.time.LocalDate

/*
This file contains functions to get/set data into the database
For handling database structure, see DatabaseUtils
 */

class PeriodDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "periods.db"
        private const val DATABASE_VERSION = 6
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

    //See DatabaseUtils
    override fun onCreate(db: SQLiteDatabase) {
        DatabaseUtils.createDatabase(db)
    }

    //See DatabaseUtils
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        if(oldVersion < 3){
            DatabaseUtils.createAppSettings(db)
        }

        if(oldVersion <4){
            db.execSQL("DROP TABLE IF EXISTS $TABLE_APP_SETTINGS")
            DatabaseUtils.createAppSettingsGroup(db)
            DatabaseUtils.createAppSettings(db)
        }

        if(oldVersion < 5){
            DatabaseUtils.createOvulationStructure(db)
        }

        if(oldVersion < 6){
            DatabaseUtils.insertLutealSetting(db)
        }
    }

    //This function is used to add a date together with a period id to the periods table
    fun addDateToPeriod(date: LocalDate, periodId: Int) {
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
            Log.d(TAG, "Inserted date $date with periodId $periodId into $TABLE_PERIODS")
        } else {
            Log.d(TAG, "Updated date $date with periodId $periodId in $TABLE_PERIODS")
        }
        db.close()
    }

    //Get all period dates for a given month
    fun getPeriodDatesForMonth(year: Int, month: Int): Map<LocalDate, Int> {
        val dates = mutableMapOf<LocalDate, Int>()
        val db = readableDatabase

        // Query the database for dates in the specified month and year
        val cursor = db.query(
            TABLE_PERIODS,
            arrayOf(COLUMN_DATE, COLUMN_PERIOD_ID),
            "strftime('%Y', $COLUMN_DATE) = ? AND strftime('%m', $COLUMN_DATE) = ?",
            arrayOf(year.toString(), month.toString().padStart(2, '0')),
            null, null, null
        )

        if (cursor != null) {
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
//                        Log.d(TAG, "Fetched date $date with periodId $periodId from $TABLE_PERIODS")
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
        } else {
            Log.e(TAG, "Cursor is null while querying for dates")
        }

        db.close()
        return dates
    }

    // This function is used to get all period dates from the database
    //We should look at removing this because its not efficient
    //TODO: Rebuild Statistics in CalendarScreen so we can remove this function
    fun getAllPeriodDates(): Map<LocalDate, Int> {
        val dates = mutableMapOf<LocalDate, Int>()
        val db = readableDatabase

        // Query the database for all dates
        val cursor = db.query(
            TABLE_PERIODS,
            arrayOf(COLUMN_DATE, COLUMN_PERIOD_ID),
            null, null, null, null, null
        )

        if (cursor != null) {
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
                        //Log.d(TAG, "Fetched date $date with periodId $periodId from $TABLE_PERIODS")
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
        } else {
            Log.e(TAG, "Cursor is null while querying for dates")
        }

        db.close()
        return dates
    }

    //Returns how many periods that are in the database
    fun getPeriodCount(): Int {
        val db = readableDatabase
        val countQuery = "SELECT COUNT(DISTINCT $COLUMN_PERIOD_ID) FROM $TABLE_PERIODS"
        val cursor = db.rawQuery(countQuery, null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    //This function is used to remove a date from the periods table
    fun removeDateFromPeriod(date: LocalDate) {
        val db = writableDatabase
        val whereClause = "$COLUMN_DATE = ?"
        val whereArgs = arrayOf(date.toString())
        val rowsDeleted = db.delete(TABLE_PERIODS, whereClause, whereArgs)
        if (rowsDeleted > 0) {
            Log.d(TAG, "Removed date $date from $TABLE_PERIODS")
        } else {
            Log.d(TAG, "No date $date found in $TABLE_PERIODS to remove")
        }
        db.close()
    }

    //This function is used to get all active symptoms from the database
    fun getAllActiveSymptoms(): List<Symptom> {
        val db = readableDatabase
        val symptoms = mutableListOf<Symptom>()
        val query =
            "SELECT $COLUMN_ID, $COLUMN_SYMPTOM_NAME, $COLUMN_SYMPTOM_ACTIVE FROM $TABLE_SYMPTOMS WHERE $COLUMN_SYMPTOM_ACTIVE = '1'"

        val cursor = db.rawQuery(query, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val symptomId = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val symptomName = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMPTOM_NAME))
                    val symptomActive = it.getInt(it.getColumnIndexOrThrow(COLUMN_SYMPTOM_ACTIVE))
                    symptoms.add(Symptom(symptomId, symptomName, symptomActive))
                } while (it.moveToNext())
            }
        }
        return symptoms
    }

    //This function is used to get all symptoms from the database
    fun getAllSymptoms(): List<Symptom> {
        val db = readableDatabase
        val symptoms = mutableListOf<Symptom>()
        val query =
            "SELECT $COLUMN_ID, $COLUMN_SYMPTOM_NAME, $COLUMN_SYMPTOM_ACTIVE FROM $TABLE_SYMPTOMS ORDER BY $COLUMN_SYMPTOM_NAME"

        val cursor = db.rawQuery(query, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val symptomId = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val symptomName = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMPTOM_NAME))
                    val symptomActive = it.getInt(it.getColumnIndexOrThrow(COLUMN_SYMPTOM_ACTIVE))
                    symptoms.add(Symptom(symptomId, symptomName, symptomActive))
                } while (it.moveToNext())
            }
        }
        return symptoms
    }

    //This function inserts new symptom into the Database
    fun createNewSymptom(symptomName: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SYMPTOM_NAME, symptomName)  // Set the symptom name
            put(COLUMN_SYMPTOM_ACTIVE, 1)  // Set the active column to 1 (true)
        }
        // Insert the new symptom into the symptoms table
        db.insert(TABLE_SYMPTOMS, null, values)
        db.close()  // Close the database connection to free up resources
    }

    //This function returns all Symptom dates for given month
    fun getSymptomDatesForMonth(year: Int, month: Int): Set<LocalDate> {
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
                        // Log the fetched date
                        //Log.d(TAG, "Fetched date $date from $TABLE_SYMPTOM_DATE")
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
            db.close()
        }

        return dates
    }

    //This function is used to update symptom dates in the database
    fun updateSymptomDate(dates: List<LocalDate>, symptomId: List<Int>) {
        val db = writableDatabase

        // Convert dates to strings for database operations
        val dateStrings = dates.map { it.toString() }

        db.beginTransaction()
        try {
            // Delete existing symptoms for the specified dates
            db.execSQL(
                """
                DELETE FROM $TABLE_SYMPTOM_DATE WHERE $COLUMN_SYMPTOM_DATE IN (${dateStrings.joinToString(",") { "?" }})
            """,
                dateStrings.toTypedArray()
            )

            // Insert new symptoms for the specified dates
            val insertSQL = """
            INSERT INTO $TABLE_SYMPTOM_DATE ($COLUMN_SYMPTOM_DATE, $COLUMN_SYMPTOM_ID) VALUES (?, ?)
        """
            for (date in dates) {
                for (id in symptomId) {
                    db.execSQL(insertSQL, arrayOf(date.toString(), id))
                }
            }

            // Mark the transaction as successful
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
        } finally {
            // End the transaction
            db.endTransaction()
        }

        // Close the database connection
        db.close()
    }

    //This function is used to get symptoms for a given date
    fun getSymptomsFromDate(date: LocalDate): List<Int> {
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
                    val symptomId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYMPTOM_ID))
                    symptoms.add(symptomId)
                } while (cursor.moveToNext())
            }
        }

        db.close()
        return symptoms
    }

    //This function is used to get all settings from the database
    fun getAllSettings(): List<Setting> {
        val settings = mutableListOf<Setting>()
        val db = readableDatabase
        val cursor = db.query(TABLE_APP_SETTINGS, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val key = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_KEY))
            val value = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_VALUE))
            val label = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_LABEL))
            val groupId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SETTING_GROUP_ID))
            settings.add(Setting(key, value, label, groupId))
        }
        cursor.close()

        val noSettings = settings.count()
        Log.d(TAG, "Found $noSettings in the database")

        return settings
    }

    //This function is used for updating settings in the database
    fun updateSetting(key: String, value: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_SETTING_VALUE, value)
        }
        val rowsUpdated = db.update(TABLE_APP_SETTINGS, contentValues, "$COLUMN_SETTING_KEY = ?", arrayOf(key))
        return rowsUpdated > 0
    }

    //This function is used to get a setting from the database
    fun getSettingByKey(key: String): Setting? {
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
            Setting(key, value, label, groupId)
        } else {
            null
        }

        cursor.close()
        return setting
    }

    //This function is used for adding/removing ovulation dates from the database
    fun updateOvulationDate(date: LocalDate) {
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
        db.close()
    }

    //This function is used to get ovulation date for a given month
    fun getOvulationDatesForMonth(year: Int, month: Int): Set<LocalDate> {
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
//                        Log.d("TAG", "Fetched date $date from ovulations")
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
            db.close()
        }

        return dates
    }

    //This function is used to get all ovulation dates from the database
    //TODO: Re-write statistics ans then remove this function
    fun getAllOvulationDates(): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT date FROM Ovulations", null)

        try {
            val dateIndex = cursor.getColumnIndex("date")
            if (dateIndex != -1) {
                while (cursor.moveToNext()) {
                    val dateStr = cursor.getString(dateIndex)
                    val date = LocalDate.parse(dateStr)
                    dates.add(date)
                }
            }
        } catch (e: Exception) {
            Log.e("Database", "Error reading ovulation dates", e)
        } finally {
            cursor.close()
            db.close()
        }

        return dates
    }

    //This function is used to get the number of ovulations in the database
    fun getOvulationCount(): Int {
        val db = readableDatabase
        val countQuery = "SELECT COUNT(DISTINCT DATE) FROM OVULATIONS"
        val cursor = db.rawQuery(countQuery, null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    //This function checks if date input should be included in existing period
    //or if a new periodId should be created
    fun newFindOrCreatePeriodID(date: LocalDate): Int {
        val db = readableDatabase
        val dateStr = date.toString()
        var periodId = 1

        // Query to check the current date and adjacent dates
        val query = """
        SELECT DISTINCT PERIOD_ID FROM PERIODS WHERE DATE = ? OR DATE BETWEEN ? AND ?
    """
        val args = arrayOf(dateStr, date.minusDays(1).toString(), date.plusDays(1).toString())
        val cursor = db.rawQuery(query, args)

        if (cursor != null) {
            try {
                if (cursor.count > 1) {
                    cursor.moveToFirst()
                    val primaryPeriodID = cursor.getInt(0)
                    cursor.moveToNext()
                    val secondaryPeriodID = cursor.getInt(0)

                    // Merge periods
                    val updateQuery = "UPDATE PERIODS SET PERIOD_ID = ? WHERE PERIOD_ID = ?"
                    val updateArgs = arrayOf(primaryPeriodID.toString(), secondaryPeriodID.toString())
                    db.execSQL(updateQuery, updateArgs)

                    periodId = primaryPeriodID
                    Log.d(TAG, "Merged periods. Reusing existing periodId $periodId for date $date")
                } else if (cursor.count == 1) {
                    cursor.moveToFirst()
                    periodId = cursor.getInt(0)
                    Log.d(TAG, "Found existing periodId $periodId for date $date")
                } else {
                    // Create a new period ID
                    val maxPeriodIdCursor = db.rawQuery("SELECT MAX($COLUMN_PERIOD_ID) FROM $TABLE_PERIODS", null)
                    if (maxPeriodIdCursor.moveToFirst()) {
                        val maxPeriodId = if (maxPeriodIdCursor.moveToFirst()) maxPeriodIdCursor.getInt(0) else 0
                        periodId = maxPeriodId + 1
                        Log.d(TAG, "No existing periodId found for date $date. Created new periodId $periodId")
                    }
                    maxPeriodIdCursor.close()
                }
            } finally {
                cursor.close()
            }
        } else {
            Log.e(TAG, "Cursor is null while querying for periodId")
        }

        db.close()
        return periodId
    }

    //This function is used to get the previous period start date from given input date
    fun getFirstPreviousPeriodDate(date: LocalDate): LocalDate? {
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
            firstLatestDate = LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("date")))
        }

        cursor.close()
        db.close()

        return firstLatestDate
    }

    //This function is to get the oldest period date in the database
    fun getOldestPeriodDate(): LocalDate?{
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
        db.close()
        return oldestPeriodDate
    }

    //This function is used for updating symptom active status
    fun updateSymptom(id: Int, active: Int) {
        val db = writableDatabase

        // Ensure that active is either 0 or 1
        val newActiveStatus = if (active in 0..1) active else throw IllegalArgumentException("Active status must be 0 or 1")

        val contentValues = ContentValues().apply {
            put("active", newActiveStatus)
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

        db.close()
    }

    // This function is used to get the luteal length of a cycle.
    // Date input should only be ovulation date
    // To be used with setting for calculating periods according to ovulation
    fun getLutealLengthForPeriod(date: LocalDate): Int {
        var lutealLength = 0
        val db = readableDatabase
        val query = """
        SELECT DATE FROM PERIODS WHERE DATE < ? ORDER BY DATE DESC LIMIT 1
    """
        val cursor = db.rawQuery(query, arrayOf(date.toString()))

        if (cursor.moveToFirst()) {
            val previousPeriodDate = LocalDate.parse(cursor.getString(0))
            lutealLength = java.time.temporal.ChronoUnit.DAYS.between(previousPeriodDate, date).toInt()
        }

        cursor.close()
        return lutealLength
    }

    // This function is used to get the latest X ovulation dates from the database
    // using input number to be more flexible
    fun getLatestXOvulations(number: Int): List<LocalDate> {
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
        db.close()
        return ovulationDates
    }

    fun getLastOvulation(): LocalDate? {
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
        db.close()
        return ovulationDate
    }

    fun getLatestXPeriodStart(number: Int): List<LocalDate> {
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
        db.close()

        return dateList
    }

    fun getLastPeriod(): LocalDate? {
        val db = readableDatabase
        val query = """
        SELECT DATE FROM PERIODS ORDER BY DATE DESC LIMIT 1
    """

        val cursor = db.rawQuery(query, null)
        var periodDate: LocalDate? = null

        if (cursor.moveToFirst()) {
            val dateString = cursor.getString(0)
            periodDate = LocalDate.parse(dateString)
        }

        cursor.close()
        db.close()
        return periodDate
    }


}