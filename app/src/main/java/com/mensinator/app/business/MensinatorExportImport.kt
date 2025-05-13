package com.mensinator.app.business

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class MensinatorExportImport(
    private val context: Context,
    private val dbHelper: IPeriodDatabaseHelper,
) : IMensinatorExportImport {

    override fun generateExportFileName(): String {
        // Create a date formatter to include the current date in the filename
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        // Generate a random number for the filename
        val randomNumber = (1000..9999).random()

        // Construct the filename with date and random number
        return "mensinator_${dateStr}_$randomNumber.json"
    }

    override fun getDefaultImportFilePath(): String {
        return File(context.getExternalFilesDir(null), "import.json").absolutePath
    }

    override fun exportDatabase(filePath: Uri) {
        val db = dbHelper.readableDb

        val exportData = JSONObject()

        // Export periods table
        val periodsCursor = db.query("periods", null, null, null, null, null, null)
        exportData.put("periods", cursorToJsonArray(periodsCursor))
        periodsCursor.close()

        // Export symptoms table
        val symptomsCursor = db.query("symptoms", null, null, null, null, null, null)
        exportData.put("symptoms", cursorToJsonArray(symptomsCursor))
        symptomsCursor.close()

        // Export symptom_date table
        val symptomDatesCursor = db.query("symptom_date", null, null, null, null, null, null)
        exportData.put("symptom_date", cursorToJsonArray(symptomDatesCursor))
        symptomDatesCursor.close()

        // Export ovulations table
        val ovulationsCursor = db.query("ovulations", null, null, null, null, null, null)
        exportData.put("ovulations", cursorToJsonArray(ovulationsCursor))
        ovulationsCursor.close()

        // Export app_settings table
        val settingsCursor = db.query("app_settings", null, null, null, null, null, null)
        exportData.put("app_settings", cursorToJsonArray(settingsCursor))
        settingsCursor.close()

        // Write JSON data to file
        val contentResolver = context.contentResolver
        contentResolver.openOutputStream(filePath)?.use { outputStream ->
            outputStream.write(exportData.toString().toByteArray())
        }

        db.close()
    }

    private fun cursorToJsonArray(cursor: Cursor): JSONArray {
        val jsonArray = JSONArray()
        while (cursor.moveToNext()) {
            val jsonObject = JSONObject()
            for (i in 0 until cursor.columnCount) {
                jsonObject.put(cursor.getColumnName(i), cursor.getString(i))
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray
    }

    override fun importDatabase(filePath: String) : Boolean {
        val db = dbHelper.writableDb

        // Read JSON data from file
        val file = File(filePath)
        val fileInputStream = FileInputStream(file)
        val reader = BufferedReader(InputStreamReader(fileInputStream))
        val stringBuilder = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            stringBuilder.append(line)
            line = reader.readLine()
        }
        reader.close()
        val importData = JSONObject(stringBuilder.toString())

        db.beginTransaction()

        var result = false

        try {
            // Import periods and ovulations
            // if something has been imported it will return true
            result = importPeriodsAndOvulations(importData)

            // Check if "symptoms" key exists and import if present
            if (importData.has("symptoms")) {
                importJsonArrayToTable(db, "symptoms", importData.getJSONArray("symptoms"))
            } else {
                Log.d("Import", "No symptoms data found in the file.")
            }

            // Check if "symptom_date" key exists and import if present
            if (importData.has("symptom_date")) {
                importJsonArrayToTable(db, "symptom_date", importData.getJSONArray("symptom_date"))
            } else {
                Log.d("Import", "No symptom_date data found in the file.")
            }

            // Check if "app_settings" key exists and import if present
            if (importData.has("app_settings")) {
                importAppSettings(db, importData.getJSONArray("app_settings"))
            } else {
                Log.d("Import", "No app_settings data found in the file.")
            }

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("Import", "Error during import", e)
            result = false
        }

        db.endTransaction()
        db.close()

        return result

    }

    // This function will import all period, ovulation, symptoms and symptom dates
    private fun importJsonArrayToTable(db: SQLiteDatabase, tableName: String, jsonArray: JSONArray) {
        db.delete(tableName, null, null)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val contentValues = ContentValues()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                contentValues.put(key, jsonObject.getString(key))
            }
            db.insert(tableName, null, contentValues)
        }
    }

    // This is used for periods and ovulations since these tables have constraints on dates in the db
    private fun importPeriodsAndOvulations(importData: JSONObject): Boolean {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val db = dbHelper.writableDb
        var success = false

        db.beginTransaction()

        try {
            // Handle periods
            if (importData.has("periods")) {
                val periodsArray = importData.getJSONArray("periods")
                for (i in 0 until periodsArray.length()) {
                    val periodObj = periodsArray.getJSONObject(i)
                    val dateString = periodObj.getString("date")
                    val date = LocalDate.parse(dateString, formatter)
                    val periodId = dbHelper.newFindOrCreatePeriodID(date)
                    dbHelper.addDateToPeriod(date, periodId)
                }
                success = true
            }

            // Handle ovulations
            if (importData.has("ovulations")) {
                val ovulationsArray = importData.getJSONArray("ovulations")
                for (i in 0 until ovulationsArray.length()) {
                    val ovulationObj = ovulationsArray.getJSONObject(i)
                    val dateString = ovulationObj.getString("date")
                    val date = LocalDate.parse(dateString, formatter)
                    dbHelper.addOvulationDate(date)
                }
                success = true
            }

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        return success
    }

    // This function will only update values of the settings provided in the import file
    // Due to different db-versions there should never be a time where we want data from the file
    // to insert into the database. It should always up update based on setting_key
    private fun importAppSettings(db: SQLiteDatabase, jsonArray: JSONArray) {
        // Loop through each JSON object in the array
        for (i in 0 until jsonArray.length()) {
            // Get the JSON object
            val jsonObject = jsonArray.getJSONObject(i)

            // Extract the setting_key to use as the condition for updating
            val settingKey = jsonObject.getString("setting_key")
            val settingValue = jsonObject.getString("setting_value")

            // Create a ContentValues object to hold the updated values
            val contentValues = ContentValues()
            contentValues.put("setting_value", settingValue)

            // Update the setting in the database
                db.update("app_settings", contentValues, "setting_key = ?", arrayOf(settingKey))

        }
    }
}