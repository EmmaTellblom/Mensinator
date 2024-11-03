package com.mensinator.app.database.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mensinator.app.database.Cycle
import com.mensinator.app.database.repositories.CycleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CycleViewModel(private val cycleRepository: CycleRepository): ViewModel() {

    private val _cycles = MutableStateFlow<List<Cycle>>(emptyList())
    val cycles: StateFlow<List<Cycle>> = _cycles

    private val _dateCycles = MutableStateFlow<List<Cycle>>(emptyList())
    val dateCycles: StateFlow<List<Cycle>> = _dateCycles


    //----- READ -----

    fun loadCycles(){
        viewModelScope.launch {
            _cycles.value = cycleRepository.getAllCycles()
        }
    }

    fun loadCyclesByDate(startDate: String, endDate: String){
        viewModelScope.launch {
            _dateCycles.value = cycleRepository.getAllCyclesByInterval(startDate, endDate)
        }
    }


    //----- CREATE -----

    fun addCycle(startDate: String, endDate: String, length: Long, lutealPhaseLength: Long){
        viewModelScope.launch {
            cycleRepository.insertCycle(startDate, endDate, length, lutealPhaseLength)
        }
    }

    fun addCycleByStartDate(startDate: String){
        viewModelScope.launch {
            cycleRepository.insertCycleByStartDate(startDate)
        }
    }


    //----- UPDATE -----

    fun changeCycleEndDate(endDate: String, id: Long){
        viewModelScope.launch {
            cycleRepository.setCycleEndDate(endDate, id)
            loadCycles()
        }
    }

    fun changeCycleLength(length: Long, id: Long){
        viewModelScope.launch {
            cycleRepository.setCycleLength(length, id)
            loadCycles()
        }
    }

    fun changeCycleLutealLength(lutealPhaseLength: Long, id: Long){
        viewModelScope.launch{
            cycleRepository.setCycleLutealLength(lutealPhaseLength, id)
            loadCycles()
        }
    }


    //----- DELETE -----

    fun deleteCycleById(id: Long){
        viewModelScope.launch {
            cycleRepository.deleteCycleById(id)
            loadCycles()
        }
    }

    fun deleteCycleByStartDate(startDate: String){
        viewModelScope.launch {
            cycleRepository.deleteCycleByStartDate(startDate)
            loadCycles()
        }
    }
}