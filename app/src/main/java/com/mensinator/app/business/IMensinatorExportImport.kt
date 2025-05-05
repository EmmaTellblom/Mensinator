package com.mensinator.app.business

import android.net.Uri

interface IMensinatorExportImport {
    fun generateExportFileName(): String
    fun getDefaultImportFilePath(): String
    fun exportDatabase(filePath: Uri)
    /**
     * Read the content of the given file and attempts to import the data into the database.
     *
     * @return true if the import was successful, false otherwise
     */
    fun importDatabase(filePath: String) : Boolean
}