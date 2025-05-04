package com.mensinator.app.business

interface IClueImport {
    fun importFileToDatabase(filePath: String) : Boolean
}