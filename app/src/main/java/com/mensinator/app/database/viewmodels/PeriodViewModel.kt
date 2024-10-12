package com.mensinator.app.database.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mensinator.app.database.Period
import com.mensinator.app.database.repositories.PeriodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PeriodViewModel(private val periodRepository: PeriodRepository): ViewModel() {

    private val _periods = MutableStateFlow<List<Period>>(emptyList())
    val periods: StateFlow<List<Period>> = _periods

    private val _period = MutableStateFlow<Period?>(null)
    val period: StateFlow<Period?> = _period


    //----- READ -----

    fun loadByDate(date: String){
        viewModelScope.launch {
            _period.value = periodRepository.getPeriodByDate(date)
        }
    }

    fun loadByCycle(cycleId: Long){
        viewModelScope.launch {
            _periods.value = periodRepository.getAllPeriodsByCycle(cycleId)
        }
    }


    //----- CREATE -----

    fun addPeriod(date: String, cycleId: Long){
        viewModelScope.launch {
            periodRepository.insertPeriod(date, cycleId)
        }
    }


    //----- UPDATE -----


    //----- DELETE -----

    fun deleteByDate(date: String){
        viewModelScope.launch {
            periodRepository.deletePeriodByDate(date)
        }
    }

    fun deleteByCycle(cycleId: Long){
        viewModelScope.launch {
            periodRepository.deletePeriodsByCycle(cycleId)
        }
    }
}