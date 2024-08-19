package com.mensinator.app.database.repositories

import com.mensinator.app.database.Database
import com.mensinator.app.database.Note
import com.mensinator.app.database.Note_date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository(private val database: Database) {

    //----- READ -----

    suspend fun getAllNotesByCategory(category: String): List<Note>{
        return withContext(Dispatchers.IO){
            database.noteQueries.getAllNotesByCategory(category).executeAsList()
        }
    }

    suspend fun getAllNotesByDate(date: String): List<Note>{
        return withContext(Dispatchers.IO){
            database.noteQueries.getAllNotesByDate(date).executeAsList()
        }
    }

    suspend fun getAllDatesByNote(name: String): List<Note_date>{
        return withContext(Dispatchers.IO) {
            database.noteDateQueries.getAllDatesByNote(name).executeAsList()
        }
    }


    //----- CREATE -----

    suspend fun insertNote(name: String, category: String, isActive: Boolean){
        withContext(Dispatchers.IO){
            database.noteQueries.insertNote(name, category, isActive)
        }
    }

    suspend fun insertNoteDate(date: String){
        withContext(Dispatchers.IO){
            database.noteDateQueries.insertNoteDate(date)
        }
    }

    suspend fun insertNoteDateCrossRef(noteId: Long, noteDateId: Long){
        withContext(Dispatchers.IO){
            database.noteDateCrossRefQueries.insertNoteDateCrossRef(noteId, noteDateId)
        }
    }


    //----- UPDATE -----

    suspend fun toggleNoteActiveStatus(name: String){
        withContext(Dispatchers.IO){
            database.noteQueries.toggleNoteActiveStatus(name)
        }
    }


    //----- DELETE -----

    suspend fun deleteNoteByName(name: String){
        withContext(Dispatchers.IO){
            database.noteQueries.deleteNoteByName(name)
        }
    }

    suspend fun deleteNotesByCategory(category: String){
        withContext(Dispatchers.IO){
            database.noteQueries.deleteNotesByCategory(category)
        }
    }

    suspend fun deleteNoteDate(date: String){
        withContext(Dispatchers.IO){
            database.noteDateQueries.deleteNoteDate(date)
        }
    }

    suspend fun deleteNoteDateCrossRef(noteId: Long, noteDateId: Long){
        withContext(Dispatchers.IO){
            database.noteDateCrossRefQueries.deleteNoteDateCrossRef(noteId, noteDateId)
        }
    }
}