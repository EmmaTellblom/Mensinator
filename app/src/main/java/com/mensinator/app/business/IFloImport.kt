package com.mensinator.app.business

interface IFloImport {
    fun importFileToDatabase(filePath: String) : Boolean
}