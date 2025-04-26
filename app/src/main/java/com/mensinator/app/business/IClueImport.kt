package com.mensinator.app.business

interface IClueImport {
    fun getDefaultImportFilePath(): String
    fun importFileToDatabase(filePath: String)
}