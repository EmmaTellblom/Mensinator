package com.mensinator.app.business

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.database.sqlite.transaction
import com.mensinator.app.utils.DefaultDispatcherProvider
import java.io.FileInputStream

class ClueImport(
    private val context: Context
) : IClueImport {

    private val dbHelper = PeriodDatabaseHelper(context, DefaultDispatcherProvider())

    override fun getDefaultImportFilePath(): String {
        return File(context.getExternalFilesDir(null), "import.json").absolutePath
    }

    override fun importFileToDatabase(filePath: String) {
        val db = dbHelper.writableDb

        // Read JSON data from the file
        val file = File(filePath)
        val fileInputStream = FileInputStream(file)
        val reader = BufferedReader(InputStreamReader(fileInputStream))

        Toast.makeText(context, "Importing file: ${file.name}", Toast.LENGTH_SHORT).show()

        if (file.name != "measurements.json") {
            Toast.makeText(context, "Invalid file! Expected measurements.json", Toast.LENGTH_SHORT).show()
            return
        }

        val stringBuilder = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            stringBuilder.append(line)
            line = reader.readLine()
        }
        reader.close()

        // Parse JSON array from the file
        val importArray = JSONArray(stringBuilder.toString())

        // Validate the data before cleanup
        if (!validateImportData(importArray)) {
            Toast.makeText(context, "Invalid data in import file", Toast.LENGTH_SHORT).show()
            return
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Begin a transaction for the database
        db.transaction {
            try {
                // Loop through the JSON array and process each entry
                for (i in 0 until importArray.length()) {
                    val obj = importArray.getJSONObject(i)

                    // Only process entries of type "period"
                    if (obj.getString("type") == "period") {
                        val dateString = obj.getString("date")
                        val date = LocalDate.parse(dateString, formatter)

                        // Get periodId from the helper function
                        val periodId = dbHelper.newFindOrCreatePeriodID(date)

                        // Insert the record into the database using the helper method
                        dbHelper.addDateToPeriod(date, periodId)
                    }
                }

                // Commit the transaction if everything went smoothly
            } catch (e: Exception) {
                // Handle any potential errors here
                Toast.makeText(context, "Error importing data: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                // End the transaction
            }
        }

        // Close the database
        db.close()
    }

    private fun validateImportData(importArray: JSONArray): Boolean {
        // Check if the array is empty or does not contain any valid entries
        if (importArray.length() == 0) {
            return false
        }

        // Validate that at least one entry has "type": "period"
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
