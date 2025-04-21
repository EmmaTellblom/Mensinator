package com.mensinator.app.business

import android.net.Uri

interface IClueImport {
    fun getDefaultImportFilePath(): String
    fun importFileToDatabase(filePath: String)
}