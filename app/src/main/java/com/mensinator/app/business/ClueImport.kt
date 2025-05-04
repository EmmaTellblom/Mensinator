package com.mensinator.app.business

import android.content.Context
import android.widget.Toast
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.database.sqlite.transaction
import java.io.FileInputStream

class ClueImport(
    private val context: Context,
    private val dbHelper: IPeriodDatabaseHelper,
) : IClueImport {

    override fun importFileToDatabase(filePath: String): Boolean {
        val db = dbHelper.writableDb

        // Read JSON data from the file
        val file = File(filePath)
        val fileInputStream = FileInputStream(file)
        val reader = BufferedReader(InputStreamReader(fileInputStream))

        Toast.makeText(context, "Importing file...", Toast.LENGTH_SHORT).show()

        val fileContent = reader.use { it.readText() }

        // Parse JSON array from the file
        val importArray = JSONArray(fileContent)

        // Validate the data before cleanup
        if (!validateImportData(importArray)) {
            Toast.makeText(context, "Invalid data in import file", Toast.LENGTH_SHORT).show()
            return false
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        db.transaction {
            try {
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

            } catch (e: Exception) {
                Toast.makeText(context, "Error importing data: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return false
            }
        }

        // Close the database
        db.close()
        return true
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

}
