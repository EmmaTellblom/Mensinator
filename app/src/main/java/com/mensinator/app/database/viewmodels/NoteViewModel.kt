package com.mensinator.app.database.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mensinator.app.database.Note
import com.mensinator.app.database.Note_date
import com.mensinator.app.database.repositories.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NoteViewModel(private val noteRepository: NoteRepository): ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _noteDates = MutableStateFlow<List<Note_date>>(emptyList())
    val noteDates: StateFlow<List<Note_date>> = _noteDates


    //----- READ -----

    fun loadAllNotes(){
        viewModelScope.launch {
            _notes.value = noteRepository.getAllNotes()
        }
    }

    fun loadNotesByCategory(category: String){
        viewModelScope.launch {
            _notes.value = noteRepository.getAllNotesByCategory(category)
        }
    }

    fun loadNotesByDate(date: String){
        viewModelScope.launch {
            _notes.value = noteRepository.getAllNotesByDate(date)
        }
    }

    fun loadDatesByNote(name: String){
        viewModelScope.launch {
            _noteDates.value = noteRepository.getAllDatesByNote(name)
        }
    }


    //----- CREATE -----

    fun addNote(name: String, category: String, isActive: Boolean){
        viewModelScope.launch {
            noteRepository.insertNote(name, category, isActive)
        }
    }

    fun addNoteDate(date: String){
        viewModelScope.launch {
            noteRepository.insertNoteDate(date)
        }
    }

    fun addNoteDateCrossRef(noteId: Long, noteDateId: Long){
        viewModelScope.launch {
            noteRepository.insertNoteDateCrossRef(noteId, noteDateId)
        }
    }


    //----- UPDATE -----

    fun toggleActiveStatus(name: String){
        viewModelScope.launch {
            noteRepository.toggleNoteActiveStatus(name)
        }
    }


    //----- DELETE -----

    fun deleteNoteByName(name: String){
        viewModelScope.launch {
            noteRepository.deleteNoteByName(name)
        }
    }

    fun deleteNotesByCategory(category: String){
        viewModelScope.launch {
            noteRepository.deleteNotesByCategory(category)
        }
    }

    fun deleteNoteDate(date: String){
        viewModelScope.launch {
            noteRepository.deleteNoteDate(date)
        }
    }

    fun deleteNoteDateCrossRef(noteId: Long, noteDateId: Long){
        viewModelScope.launch {
            noteRepository.deleteNoteDateCrossRef(noteId, noteDateId)
        }
    }
}