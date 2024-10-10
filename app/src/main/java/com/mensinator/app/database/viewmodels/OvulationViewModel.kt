package com.mensinator.app.database.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mensinator.app.database.Ovulation
import com.mensinator.app.database.repositories.OvulationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OvulationViewModel(private val ovulationRepository: OvulationRepository): ViewModel() {

    private val _ovulation = MutableStateFlow<Ovulation?>(null)
    val ovulation: StateFlow<Ovulation?> = _ovulation


    //----- READ -----

    fun loadByDate(date: String){
        viewModelScope.launch {
            _ovulation.value = ovulationRepository.getOvulationByDate(date)
        }
    }

    fun loadByCycle(cycleId: Long){
        viewModelScope.launch {
            _ovulation.value = ovulationRepository.getOvulationByCycle(cycleId)
        }
    }


    //----- CREATE -----

    fun addOvulation(date: String, cycleId: Long){
        viewModelScope.launch {
            ovulationRepository.insertOvulation(date, cycleId)
        }
    }


    //----- UPDATE -----


    //----- DELETE -----

    fun deleteByDate(date: String){
        viewModelScope.launch {
            ovulationRepository.deleteOvulationByDate(date)
        }
    }

    fun deleteByCycle(cycleId: Long){
        viewModelScope.launch {
            ovulationRepository.deleteOvulationByCycle(cycleId)
        }
    }
}