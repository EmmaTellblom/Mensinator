package com.mensinator.app.business

import java.io.File
import android.content.Context
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import androidx.core.database.sqlite.transaction
import org.json.JSONObject
import java.io.FileInputStream
import java.time.format.DateTimeFormatter

class FloImport (
    private val context: Context,
    private val dbHelper: IPeriodDatabaseHelper,
) : IFloImport {

    override fun importFileToDatabase(filePath: String): Boolean {
        val db = dbHelper.writableDb

        // Read JSON data from the file
        val file = File(filePath)
        val fileInputStream = FileInputStream(file)
        val reader = BufferedReader(InputStreamReader(fileInputStream))

        Toast.makeText(context, "Importing file...", Toast.LENGTH_SHORT).show()

        val fileContent = reader.use { it.readText() }

        // Parse JSON object from the file
        val importObject = JSONObject(fileContent)

        // Validate the data before cleanup
        if (!validateImportData(importObject)) {
            Toast.makeText(context, "Invalid data in import file", Toast.LENGTH_SHORT).show()
            return false
        }

        val success = try {
            db.transaction {
                processData(importObject)
            }
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Error importing data: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
        db.close()
        return success
    }

    private fun validateImportData(importObject: JSONObject): Boolean {
        // Check if the "operationalData" key exists
        if (!importObject.has("operationalData")) {
            return false
        }

        val operationalData = importObject.getJSONObject("operationalData")

        // Check if the "cycles" array exists
        if (operationalData.has("cycles")) {
            val cyclesArray = operationalData.getJSONArray("cycles")

            // Loop through all cycles
            for (i in 0 until cyclesArray.length()) {
                val cycle = cyclesArray.getJSONObject(i)

                // Check if both "period_start_date" and "period_end_date" exist in the cycle object
                if (cycle.has("period_start_date") && cycle.has("period_end_date")) {
                    return true // Valid cycle found
                }
            }
        }

        return false // No valid cycle found
    }

    // Get all dates in a cycle
    private fun getPeriodDates(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val periodDates = mutableListOf<LocalDate>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            periodDates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }
        return periodDates
    }

    private fun processData(importObject: JSONObject) {
        val operationalData = importObject.getJSONObject("operationalData")

        // Handle cycles first
        val cyclesArray = operationalData.getJSONArray("cycles")
        for (i in 0 until cyclesArray.length()) {
            val cycle = cyclesArray.getJSONObject(i)

            val startDateString = cycle.optString("period_start_date")
            val endDateString = cycle.optString("period_end_date")

            if (startDateString.isNotEmpty() && endDateString.isNotEmpty()) {
                val startDate = LocalDate.parse(startDateString, DateTimeFormatter.ISO_LOCAL_DATE)
                val endDate = LocalDate.parse(endDateString, DateTimeFormatter.ISO_LOCAL_DATE)

                val dates = getPeriodDates(startDate, endDate)

                for (date in dates) {
                    val periodId = dbHelper.newFindOrCreatePeriodID(date)
                    dbHelper.addDateToPeriod(date, periodId)
                }
            }
        }

        // Get Ovulations from either OvulationTests or "own judgement"
        val pointEventsArray = operationalData.getJSONArray("point_events_manual_v2")
        for (i in 0 until pointEventsArray.length()) {
            val event = pointEventsArray.getJSONObject(i)

            val category = event.optString("category")
            val subcategory = event.optString("subcategory")

            if ((category == "OvulationTest" && subcategory == "Positive") ||
                (category == "Ovulation" && subcategory == "OtherMethods")) {

                val dateString = event.optString("date")
                if (dateString.isNotEmpty()) {
                    val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
                    dbHelper.addOvulationDate(date)
                }
            }
        }
    }
}
