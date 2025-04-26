package com.mensinator.app.business

import java.io.File
import android.content.Context
import android.widget.Toast
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.database.sqlite.transaction
import java.io.FileInputStream

class FloImport (
    private val context: Context,
    private val dbHelper: IPeriodDatabaseHelper,
    ) : IFloImport {
    override fun getDefaultImportFilePath(): String {
        return File(context.getExternalFilesDir(null), "import.json").absolutePath
    }
    override fun importFileToDatabase(filePath: String) {

    }
}