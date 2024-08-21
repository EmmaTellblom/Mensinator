package com.mensinator.app.database.repositories

import com.mensinator.app.database.MensinatorDB
import com.mensinator.app.database.Note
import com.mensinator.app.database.NoteCategory
import com.mensinator.app.database.Note_date
import com.mensinator.app.database.PredefinedSymptoms
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository(private val database: MensinatorDB, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {



    //----- READ -----

    suspend fun getAllNotes(): List<Note>{
        return withContext(dispatcher){
            database.noteQueries.getAllNotes().executeAsList()
        }
    }

    suspend fun getAllNotesByCategory(category: String): List<Note>{
        return withContext(dispatcher){
            database.noteQueries.getAllNotesByCategory(category).executeAsList()
        }
    }

    suspend fun getAllNotesByDate(date: String): List<Note>{
        return withContext(dispatcher){
            database.noteQueries.getAllNotesByDate(date).executeAsList()
        }
    }

    suspend fun getAllDatesByNote(name: String): List<Note_date>{
        return withContext(dispatcher) {
            database.noteDateQueries.getAllDatesByNote(name).executeAsList()
        }
    }


    //----- CREATE -----

    suspend fun insertNote(name: String, category: String, isActive: Boolean){
        withContext(dispatcher){
            database.noteQueries.insertNote(name, category, isActive)
        }
    }

    suspend fun insertNoteDate(date: String){
        withContext(dispatcher){
            database.noteDateQueries.insertNoteDate(date)
        }
    }

    suspend fun insertNoteDateCrossRef(noteId: Long, noteDateId: Long){
        withContext(dispatcher){
            database.noteDateCrossRefQueries.insertNoteDateCrossRef(noteId, noteDateId)
        }
    }


    //----- UPDATE -----

    suspend fun toggleNoteActiveStatus(name: String){
        withContext(dispatcher){
            database.noteQueries.toggleNoteActiveStatus(name)
        }
    }


    //----- DELETE -----

    suspend fun deleteNoteByName(name: String){
        withContext(dispatcher){
            database.noteQueries.deleteNoteByName(name)
        }
    }

    suspend fun deleteNotesByCategory(category: String){
        withContext(dispatcher){
            database.noteQueries.deleteNotesByCategory(category)
        }
    }

    suspend fun deleteNoteDate(date: String){
        withContext(dispatcher){
            database.noteDateQueries.deleteNoteDate(date)
        }
    }

    suspend fun deleteNoteDateCrossRef(noteId: Long, noteDateId: Long){
        withContext(dispatcher){
            database.noteDateCrossRefQueries.deleteNoteDateCrossRef(noteId, noteDateId)
        }
    }
}