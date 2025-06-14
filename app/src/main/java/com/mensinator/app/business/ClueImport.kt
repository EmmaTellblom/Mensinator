package com.mensinator.app.business

import android.util.Log
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.database.sqlite.transaction
import java.io.FileInputStream

class ClueImport(
    private val dbHelper: IPeriodDatabaseHelper,
) : IClueImport {

    override fun importFileToDatabase(filePath: String): Boolean {
        val db = dbHelper.writableDb
        return try {
            // Read JSON data from the file
            val file = File(filePath)
            val fileInputStream = FileInputStream(file)
            val reader = BufferedReader(InputStreamReader(fileInputStream))

            val fileContent = reader.use { it.readText() }

            // Parse JSON array from the file
            val importArray = JSONArray(fileContent)

            // Validate the data before cleanup
            if (!validateImportData(importArray)) return false

            db.transaction {
                processData(importArray)
            }
            // Close the database
            db.close()

            true
        } catch (e: Exception) {
            Log.d("ClueImport", "Error importing file: $e")
            db.close()
            false
        }
    }

    private fun validateImportData(importArray: JSONArray): Boolean {
        // Check if the array is empty
        if (importArray.length() == 0) {
            return false
        }

        // Check so that at least one entry has "type": "period"
        for (i in 0 until importArray.length()) {
            val obj = importArray.getJSONObject(i)
            if (obj.getString("type") == "period" && obj.has("date") && obj.has("value")) {
                return true
            }
        }

        // If no valid data found, return false
        return false
    }

    private fun processData(importArray: JSONArray) {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        for (i in 0 until importArray.length()) {
            val obj = importArray.getJSONObject(i)
            val type = obj.getString("type")
            val dateString = obj.getString("date")
            val date = LocalDate.parse(dateString, formatter)

            when (type) {
                "period" -> {
                    val periodId = dbHelper.newFindOrCreatePeriodID(date)
                    dbHelper.addDateToPeriod(date, periodId)
                }
                "tests" -> {
                    val valueArray = obj.getJSONArray("value")
                    // we need to loop in case there are several different tests
                    for (j in 0 until valueArray.length()) {
                        val optionObj = valueArray.getJSONObject(j)
                        val option = optionObj.getString("option")
                        if (option == "ovulation_positive") {
                            dbHelper.addOvulationDate(date)
                            break // if a positive ovulation test is found, break the loop
                        }
                    }
                }
            }
        }
    }

}
