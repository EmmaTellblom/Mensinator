package com.mensinator.app.business

interface IFloImport {
    fun getDefaultImportFilePath(): String
    fun importFileToDatabase(filePath: String)
}