package com.mensinator.app.business

import android.net.Uri

interface IExportImport {
    fun generateExportFileName(): String
    fun getDefaultImportFilePath(): String
    fun exportDatabase(filePath: Uri)
    fun importDatabase(filePath: String)
}