package com.mensinator.app.database.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mensinator.app.database.Prediction
import com.mensinator.app.database.repositories.PredictionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PredictionViewModel(private val predictionRepository: PredictionRepository): ViewModel() {

    private val _predictions = MutableStateFlow<List<Prediction>>(emptyList())
    val prediction: StateFlow<List<Prediction>> = _predictions

    //----- READ -----

    fun loadByType(type: String){
        viewModelScope.launch {
            _predictions.value = predictionRepository.getPredictionsByType(type)
        }
    }

    fun loadByCycle(cycleId: Long){
        viewModelScope.launch {
            _predictions.value = predictionRepository.getPredictionsByCycle(cycleId)
        }
    }

    fun loadByMonth(startDate: String, endDate: String){
        viewModelScope.launch {
            _predictions.value = predictionRepository.getPredictionsByMonth(startDate, endDate)
        }
    }

    fun loadByTypeCycle(type: String, cycleId: Long){
        viewModelScope.launch {
            _predictions.value = predictionRepository.getPredictionsByTypeCycle(type, cycleId)
        }
    }

    fun loadByTypeMonth(type: String, startDate: String, endDate: String){
        viewModelScope.launch {
            _predictions.value = predictionRepository.getPredictionsByTypeMonth(type, startDate, endDate)
        }
    }


    //----- CREATE -----

    fun addPrediction(date: String, type: String, cycleId: Long){
        viewModelScope.launch {
            predictionRepository.insertPrediction(date, type, cycleId)
        }
    }


    //----- UPDATE -----


    //----- DELETE -----

    fun deleteByCycle(cycleId: Long){
        viewModelScope.launch {
            predictionRepository.deletePredictionsByCycle(cycleId)
        }
    }

    fun deleteByMonth(startDate: String, endDate: String){
        viewModelScope.launch {
            predictionRepository.deletePredictionsByMonth(startDate, endDate)
        }
    }

    fun deleteByTypeCycle(type: String, cycleId: Long){
        viewModelScope.launch {
            predictionRepository.deletePredictionsByTypeCycle(type, cycleId)
        }
    }

    fun deleteByTypeMonth(type: String, startDate: String, endDate: String){
        viewModelScope.launch {
            predictionRepository.deletePredictionsByTypeMonth(type, startDate, endDate)
        }
    }

    fun deleteByDate(date: String){
        viewModelScope.launch {
            predictionRepository.deletePredictionsByDate(date)
        }
    }
}