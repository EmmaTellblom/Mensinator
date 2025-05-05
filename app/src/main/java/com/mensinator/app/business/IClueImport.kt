package com.mensinator.app.business

interface IClueImport {
    /**
     * Read the content of the given file and attempts to import the data into the database.
     *
     * @return true if the import was successful, false otherwise
     */
    fun importFileToDatabase(filePath: String) : Boolean
}