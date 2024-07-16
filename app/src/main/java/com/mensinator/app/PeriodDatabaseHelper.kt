package com.mensinator.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.time.LocalDate
import com.mensinator.app.DatabaseUtils

/*
This file contains functions to get/set data into the database
For handling database structure, see DatabaseUtils
 */

class PeriodDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "periods.db"
        private const val DATABASE_VERSION = 4
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

    override fun onCreate(db: SQLiteDatabase) {
        DatabaseUtils.createDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        if(oldVersion < 3){
            DatabaseUtils.createAppSettings(db)
        }

        if(oldVersion <4){
            db.execSQL("DROP TABLE IF EXISTS $TABLE_APP_SETTINGS")
            DatabaseUtils.createAppSettingsGroup(db)
            DatabaseUtils.createAppSettings(db)
        }
    }

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
                        Log.d(TAG, "Fetched date $date with periodId $periodId from $TABLE_PERIODS")
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

    fun findOrCreatePeriodId(date: LocalDate): Int {
        val db = readableDatabase
        val dateStr = date.toString()

        // Check if the date already has a period ID
        val cursor = db.query(
            TABLE_PERIODS,
            arrayOf(COLUMN_PERIOD_ID),
            "$COLUMN_DATE = ?",
            arrayOf(dateStr),
            null, null, null
        )

        var periodId = 1  // Default period ID

        if (cursor != null) {
            try {
                val periodIdIndex = cursor.getColumnIndex(COLUMN_PERIOD_ID)

                if (periodIdIndex != -1 && cursor.moveToFirst()) {
                    periodId = cursor.getInt(periodIdIndex)
                    Log.d(TAG, "Found existing periodId $periodId for date $date")
                } else {
                    // Find the period ID of adjacent dates
                    val previousDate = date.minusDays(1)
                    val nextDate = date.plusDays(1)

                    val periodIds = mutableSetOf<Int>()

                    // Check the previous date for period ID
                    val prevCursor = db.query(
                        TABLE_PERIODS,
                        arrayOf(COLUMN_PERIOD_ID),
                        "$COLUMN_DATE = ?",
                        arrayOf(previousDate.toString()),
                        null, null, null
                    )

                    if (prevCursor != null) {
                        try {
                            val prevPeriodIdIndex = prevCursor.getColumnIndex(COLUMN_PERIOD_ID)
                            if (prevPeriodIdIndex != -1 && prevCursor.moveToFirst()) {
                                val prevPeriodId = prevCursor.getInt(prevPeriodIdIndex)
                                periodIds.add(prevPeriodId)
                            }
                        } finally {
                            prevCursor.close()
                        }
                    }

                    // Check the next date for period ID
                    val nextCursor = db.query(
                        TABLE_PERIODS,
                        arrayOf(COLUMN_PERIOD_ID),
                        "$COLUMN_DATE = ?",
                        arrayOf(nextDate.toString()),
                        null, null, null
                    )

                    if (nextCursor != null) {
                        try {
                            val nextPeriodIdIndex = nextCursor.getColumnIndex(COLUMN_PERIOD_ID)
                            if (nextPeriodIdIndex != -1 && nextCursor.moveToFirst()) {
                                val nextPeriodId = nextCursor.getInt(nextPeriodIdIndex)
                                periodIds.add(nextPeriodId)
                            }
                        } finally {
                            nextCursor.close()
                        }
                    }

                    // Check if there are existing periods to merge with
                    if (periodIds.isNotEmpty()) {
                        periodId =
                            periodIds.first()  // Use the first found period ID (assume dates are adjacent)
                        Log.d(TAG, "Reusing existing periodId $periodId for date $date")
                    } else {
                        // Find the highest period ID and increment it
                        val maxPeriodIdCursor = db.rawQuery(
                            "SELECT MAX($COLUMN_PERIOD_ID) FROM $TABLE_PERIODS",
                            null
                        )

                        if (maxPeriodIdCursor.moveToFirst()) {
                            periodId = (maxPeriodIdCursor.getInt(0) ?: 0) + 1
                            Log.d(
                                TAG,
                                "No adjacent periodId found for date $date. Created new periodId $periodId"
                            )
                        }

                        maxPeriodIdCursor.close()
                    }
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
                        Log.d(TAG, "Fetched date $date with periodId $periodId from $TABLE_PERIODS")
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

    fun getAllActiveSymptoms(): List<Symptom> {
        val db = readableDatabase
        val symptoms = mutableListOf<Symptom>()
        val query =
            "SELECT $COLUMN_ID, $COLUMN_SYMPTOM_NAME FROM $TABLE_SYMPTOMS WHERE $COLUMN_SYMPTOM_ACTIVE = '1'"

        val cursor = db.rawQuery(query, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val symptomId = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val symptomName = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMPTOM_NAME))
                    symptoms.add(Symptom(symptomId, symptomName))
                } while (it.moveToNext())
            }
        }
        return symptoms
    }

    fun getAllSymptoms(): List<Symptom> {
        val db = readableDatabase
        val symptoms = mutableListOf<Symptom>()
        val query =
            "SELECT $COLUMN_ID, $COLUMN_SYMPTOM_NAME FROM $TABLE_SYMPTOMS"

        val cursor = db.rawQuery(query, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val symptomId = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val symptomName = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMPTOM_NAME))
                    symptoms.add(Symptom(symptomId, symptomName))
                } while (it.moveToNext())
            }
        }
        return symptoms
    }

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

    fun getSymptomDatesForMonth(year: Int, month: Int): Set<LocalDate> {
        val dates = mutableSetOf<LocalDate>()
        val db = readableDatabase

        // Query the database for dates in the specified month and year
        val cursor = db.query(
            TABLE_SYMPTOM_DATE,
            arrayOf(COLUMN_SYMPTOM_DATE),
            "strftime('%Y', $COLUMN_SYMPTOM_DATE) = ? AND strftime('%m', $COLUMN_SYMPTOM_DATE) = ?",
            arrayOf(year.toString(), month.toString().padStart(2, '0')),
            null, null, null
        )

        try {
            // Get the column index for the date
            val dateIndex = cursor.getColumnIndex(COLUMN_SYMPTOM_DATE)

            if (dateIndex != -1) {
                while (cursor.moveToNext()) {
                    val dateStr = cursor.getString(dateIndex)
                    try {
                        val date = LocalDate.parse(dateStr)
                        // Add the date to the set of dates with symptoms
                        dates.add(date)
                        Log.d(TAG, "Fetched date $date from $TABLE_SYMPTOM_DATE")
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

    fun inactivateSymptoms(symptomId: List<Int>){
        val db = readableDatabase
        db.execSQL("""
            UPDATE TABLE_$TABLE_SYMPTOMS SET $COLUMN_SYMPTOM_ACTIVE = $COLUMN_ID = ?
        """)
        arrayOf(symptomId)
    }

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

    fun updateSetting(key: String, value: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_SETTING_VALUE, value)
        }
        val rowsUpdated = db.update(TABLE_APP_SETTINGS, contentValues, "$COLUMN_SETTING_KEY = ?", arrayOf(key))
        return rowsUpdated > 0
    }



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


}