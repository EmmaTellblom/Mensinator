package com.mensinator.app.business

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import kotlinx.datetime.LocalDate
import org.json.JSONObject
import java.io.File
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

    override suspend fun exportDatabase(filePath: Uri) {
        val exportData = dbHelper.exportAllTablesToJson()
        val contentResolver = context.contentResolver
        contentResolver.openOutputStream(filePath)?.use { outputStream ->
            outputStream.write(exportData.toString().toByteArray())
        }
    }

    override suspend fun importDatabase(filePath: String): Boolean {
        val file = File(filePath)
        val fileContent = file.readText()
        val importData = JSONObject(fileContent)
        return dbHelper.executeInTransaction {
            var result = importPeriodsAndOvulations(importData)
            if (importData.has("symptoms")) {
                dbHelper.importTableData("symptoms", importData.getJSONArray("symptoms"))
            }
            if (importData.has("symptom_date")) {
                dbHelper.importTableData("symptom_date", importData.getJSONArray("symptom_date"))
            }
            if (importData.has("app_settings")) {
                dbHelper.importAppSettings(importData.getJSONArray("app_settings"))
            }
            result
        }
    }

    // This is used for periods and ovulations since these tables have constraints on dates in the db
    private suspend fun importPeriodsAndOvulations(importData: JSONObject): Boolean {
        var success = false

        try {
            // Handle periods
            if (importData.has("periods")) {
                val periodsArray = importData.getJSONArray("periods")
                for (i in 0 until periodsArray.length()) {
                    val periodObj = periodsArray.getJSONObject(i)
                    val dateString = periodObj.getString("date")
                    val date = LocalDate.parse(dateString)
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
                    val date = LocalDate.parse(dateString)
                    dbHelper.addOvulationDate(date)
                }
                success = true
            }
        } catch (e: Exception) {
            success = false
        }
        return success
    }
}
