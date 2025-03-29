package com.mensinator.app.business

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.icu.text.SimpleDateFormat
import android.os.Environment
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class ExportImport(
    private val context: Context,
    private val dbHelper: IPeriodDatabaseHelper,
) : IExportImport {

    override fun getDocumentsExportFilePath(): String {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        // Create a date formatter to include the current date in the filename
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        // Generate a random number for the filename
        val randomNumber = (1000..9999).random()

        // Construct the filename with date and random number
        val fileName = "mensinator_${dateStr}_$randomNumber.json"

        // Create the file object for the export file
        val exportFile = File(documentsDir, fileName)

        // Create the documents directory if it does not exist
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }

        // Return the absolute path of the export file
        return exportFile.absolutePath
    }

    override fun getDefaultImportFilePath(): String {
        return File(context.getExternalFilesDir(null), "import").absolutePath
    }

    override fun exportDatabase(filePath: String) {
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
        val file = File(filePath)
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(exportData.toString().toByteArray())
        fileOutputStream.close()

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

    override fun importDatabase(filePath: String) {
        val db = dbHelper.writableDb

        // Read JSON data from file
        val file = File(filePath)
        val fileInputStream = FileInputStream(file)
        val reader = BufferedReader(InputStreamReader(fileInputStream))
        val importData : JSONObject

        // Check file extension
        when (file.extension.lowercase()) {
            "txt" -> importData = transformMyCalendarFormatToMensinatorFormat(reader)

            "json" -> {
                val stringBuilder = StringBuilder()
                reader.forEachLine {line -> stringBuilder.append(line) }
                importData = JSONObject(stringBuilder.toString())
            }

            else -> throw Exception("Extension file is not supported")
        }
        reader.close()

        db.beginTransaction()
        try {
            // Import periods table
            importJsonArrayToTable(db, "periods", importData.getJSONArray("periods"))

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

            // Check if "ovulations" key exists and import if present
            if (importData.has("ovulations")) {
                importJsonArrayToTable(db, "ovulations", importData.getJSONArray("ovulations"))
            } else {
                Log.d("Import", "No ovulations data found in the file.")
            }

            // Check if "app_settings" key exists and import if present
            if (importData.has("app_settings")) {
                importAppSettings(db, importData.getJSONArray("app_settings"))
            } else {
                Log.d("Import", "No app_settings data found in the file.")
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        db.close()
    }

    // This function will delete all period, ovulation, symptoms and symptomdates before importing the file
    // User should never do any changes before importing their file
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

    // This function will only update values of the settings provided in the importfile
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

    // This functions transforms an import file that has "My Calendar" period app's txt format into Mensinator's JSON format
    private fun transformMyCalendarFormatToMensinatorFormat(reader: BufferedReader) : JSONObject {
        val logMessage: (String) -> Unit = { message -> Log.d("Import", "[TransformMyCalendarFormatToMensinatorFormat] $message") }

        logMessage("Transforming My Calendar's txt format into Mensinator's JSON format")

        // Period data
        val periodsSection = JSONArray()
        var periodDateHelper = LocalDate.now()
        var periodIDHelper = 1
        var periodDateIDHelper = 1

        // Symptom data
        val symptomMap : HashMap<String, Int> = HashMap()
        val symptomSection = JSONArray()
        val symptomDateSection = JSONArray()
        var symptomIDHelper = 1
        var symptomDateIDHelper = 1

        // Regular expressions and formats
        val dateAndContentRegex = Regex("(.+)\t(.+)") // To get the date and content of each annotation in My Calendar export. Format is '($date)\t($content)'.
        val symptomsRegex = Regex("Síntomas:(.+)") // To get the symptoms list
        val myCalendarDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy") // My Calendar's date format

        // Process file
        var line: String? = reader.readLine()
        while (line != null) {

            val lineMatch = dateAndContentRegex.find(line)
            line = reader.readLine()
            if (lineMatch == null)  continue

            val date = LocalDate.parse(lineMatch.groupValues[1], myCalendarDateFormatter)
            val content = lineMatch.groupValues[2]

            when {
                // Period start
                content == "Inicio del período" -> periodDateHelper = date

                // Period end
                content == "Fin del período" -> {
                    logMessage("Adding period ranged from $periodDateHelper to $date")

                    while (periodDateHelper <= date) {
                        val periodJSON = JSONObject()
                        periodJSON.put("id", periodDateIDHelper++)
                        periodJSON.put("date", periodDateHelper)
                        periodJSON.put("period_id", periodIDHelper)

                        periodsSection.put(periodJSON)
                        periodDateHelper = periodDateHelper.plusDays(1)
                    }

                    periodIDHelper++
                }

                // Dated symptoms annotated
                symptomsRegex.containsMatchIn(content) -> {
                    val symptomList = symptomsRegex.find(content)!!.groupValues[1]
                    val symptoms = symptomList.split(";").dropLast(1) // My Calendar's symptoms list always ends with a ';'.
                    // That means that the last symptom will always be an empty string, so we drop it.

                    for (symptom in symptoms) {
                        var currentSymptomID : Int
                        if (symptomMap.contains(symptom)) {
                            currentSymptomID = symptomMap[symptom]!!
                        }
                        else {
                            symptomMap[symptom] = symptomIDHelper++
                            currentSymptomID = symptomIDHelper
                        }

                        val symptomDateJSON = JSONObject()
                        symptomDateJSON.put("id", symptomDateIDHelper++)
                        symptomDateJSON.put("symptom_date", date)
                        symptomDateJSON.put("symptom_id", currentSymptomID)

                        symptomDateSection.put(symptomDateJSON)
                    }
                }
            }
        }

        // Add symptoms
        for (symptom in symptomMap.keys) {
            val symptomJSON = JSONObject()
            symptomJSON.put("id", symptomMap[symptom])
            symptomJSON.put("symptom_name", symptom)
            symptomJSON.put("active", 1)

            symptomSection.put(symptomJSON)
        }

        val mensinatorJSON = JSONObject()
        mensinatorJSON.put("periods", periodsSection)
        mensinatorJSON.put("symptoms", symptomSection)
        mensinatorJSON.put("symptom_date", symptomDateSection)

        logMessage("Transformation ended")

        return mensinatorJSON
    }
}