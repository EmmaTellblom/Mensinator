package com.mensinator.app.database.repositories

import com.mensinator.app.database.MensinatorDB
import com.mensinator.app.database.Prediction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PredictionRepository(private val database: MensinatorDB, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

    //----- READ -----

    suspend fun getAllPredictions(): List<Prediction> {
        return withContext(dispatcher){
            database.predictionQueries.getAllPredictions().executeAsList()
        }
    }

    suspend fun getPredictionsByType(type: String): List<Prediction> {
        return withContext(dispatcher){
            database.predictionQueries.getPredictionsByType(type).executeAsList()
        }
    }

    suspend fun getPredictionsByCycle(cycleId: Long): List<Prediction> {
        return withContext(dispatcher){
            database.predictionQueries.getPredictionsByCycle(cycleId).executeAsList()
        }
    }

    suspend fun getPredictionsByMonth(startDate: String, endDate: String): List<Prediction> {
        return withContext(dispatcher){
            database.predictionQueries.getPredictionsByMonth(startDate, endDate).executeAsList()
        }
    }

    suspend fun getPredictionsByTypeCycle(type: String, cycleId: Long): List<Prediction> {
        return withContext(dispatcher){
            database.predictionQueries.getPredictionsByTypeCycle(type, cycleId).executeAsList()
        }
    }

    suspend fun getPredictionsByTypeMonth(type: String, startDate: String, endDate: String): List<Prediction> {
        return withContext(dispatcher){
            database.predictionQueries.getPredictionsByTypeMonth(type, startDate, endDate).executeAsList()
        }
    }


    //----- CREATE -----

    suspend fun insertPrediction(date: String, type: String, cycleId: Long){
        withContext(dispatcher){
            database.predictionQueries.insertPrediction(date, type, cycleId)
        }
    }


    //----- UPDATE -----


    //----- DELETE -----

    suspend fun deletePredictionsByCycle(cycleId: Long) {
        withContext(dispatcher){
            database.predictionQueries.deletePredictionsByCycle(cycleId)
        }
    }

    suspend fun deletePredictionsByMonth(startDate: String, endDate: String) {
        withContext(dispatcher){
            database.predictionQueries.deletePredictionsByMonth(startDate, endDate)
        }
    }

    suspend fun deletePredictionsByTypeCycle(type: String, cycleId: Long) {
        withContext(dispatcher){
            database.predictionQueries.deletePredictionsByTypeCycle(type, cycleId)
        }
    }

    suspend fun deletePredictionsByTypeMonth(type: String, startDate: String, endDate: String) {
        withContext(dispatcher){
            database.predictionQueries.deletePredictionsByTypeMonth(type, startDate, endDate)
        }
    }

    suspend fun deletePredictionsByDate(date: String) {
        withContext(dispatcher){
            database.predictionQueries.deletePredictionsByDate(date)
        }
    }
}