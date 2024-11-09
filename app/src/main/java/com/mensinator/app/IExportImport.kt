package com.mensinator.app

interface IExportImport {
    fun getDocumentsExportFilePath(): String
    fun getDefaultImportFilePath(): String
    fun exportDatabase(filePath: String)
    fun importDatabase(filePath: String)
}