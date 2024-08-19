package com.mensinator.app.database.repositories

import com.mensinator.app.database.Database
import com.mensinator.app.database.Prediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PredictionRepository(private val database: Database) {

    //----- READ -----

    suspend fun getPredictionsByType(type: String): List<Prediction> {
        return withContext(Dispatchers.IO){
            database.predictionQueries.getPredictionsByType(type).executeAsList()
        }
    }

    suspend fun getPredictionsByCycle(cycleId: Long): List<Prediction> {
        return withContext(Dispatchers.IO){
            database.predictionQueries.getPredictionsByCycle(cycleId).executeAsList()
        }
    }

    suspend fun getPredictionsByMonth(startDate: String, endDate: String): List<Prediction> {
        return withContext(Dispatchers.IO){
            database.predictionQueries.getPredictionsByMonth(startDate, endDate).executeAsList()
        }
    }

    suspend fun getPredictionsByTypeCycle(type: String, cycleId: Long): List<Prediction> {
        return withContext(Dispatchers.IO){
            database.predictionQueries.getPredictionsByTypeCycle(type, cycleId).executeAsList()
        }
    }

    suspend fun getPredictionsByTypeMonth(type: String, startDate: String, endDate: String): List<Prediction> {
        return withContext(Dispatchers.IO){
            database.predictionQueries.getPredictionsByTypeMonth(type, startDate, endDate).executeAsList()
        }
    }


    //----- CREATE -----

    suspend fun insertPrediction(date: String, type: String, cycleId: Long){
        withContext(Dispatchers.IO){
            database.predictionQueries.insertPrediction(date, type, cycleId)
        }
    }


    //----- UPDATE -----


    //----- DELETE -----

    suspend fun deletePredictionsByCycle(cycleId: Long) {
        withContext(Dispatchers.IO){
            database.predictionQueries.deletePredictionsByCycle(cycleId)
        }
    }

    suspend fun deletePredictionsByMonth(startDate: String, endDate: String) {
        withContext(Dispatchers.IO){
            database.predictionQueries.deletePredictionsByMonth(startDate, endDate)
        }
    }

    suspend fun deletePredictionsByTypeCycle(type: String, cycleId: Long) {
        withContext(Dispatchers.IO){
            database.predictionQueries.deletePredictionsByTypeCycle(type, cycleId)
        }
    }

    suspend fun deletePredictionsByTypeMonth(type: String, startDate: String, endDate: String) {
        withContext(Dispatchers.IO){
            database.predictionQueries.deletePredictionsByTypeMonth(type, startDate, endDate)
        }
    }

    suspend fun deletePredictionsByDate(date: String) {
        withContext(Dispatchers.IO){
            database.predictionQueries.deletePredictionsByDate(date)
        }
    }
}