package com.mensinator.app.database.repositories

import com.mensinator.app.database.Cycle
import com.mensinator.app.database.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CycleRepository(private val database: Database) {

    //----- READ -----

    suspend fun getAllCycles(): List<Cycle> {
        return withContext(Dispatchers.IO){
            database.cycleQueries.getAllCycles().executeAsList()
        }
    }

    suspend fun getAllCyclesByInterval(startDate: String, endDate: String): List<Cycle> {
        return withContext(Dispatchers.IO){
            database.cycleQueries.getAllCyclesByInterval(startDate, endDate).executeAsList()
        }
    }


    //----- CREATE -----

    suspend fun insertCycle(startDate: String, endDate: String, length: Long, lutealPhaseLength: Long) {
        withContext(Dispatchers.IO){
            database.cycleQueries.insertCycle(startDate, endDate, length, lutealPhaseLength)
        }
    }

    suspend fun insertCycleByStartDate(startDate: String){
        withContext(Dispatchers.IO){
            database.cycleQueries.insertCycleByStartDate(startDate)
        }
    }


    //----- UPDATE -----

    suspend fun setCycleEndDate(endDate: String, id: Long){
        withContext(Dispatchers.IO){
            database.cycleQueries.setCycleEndDate(endDate, id)
        }
    }

    suspend fun setCycleLength(length: Long, id: Long){
        withContext(Dispatchers.IO){
            database.cycleQueries.setCycleLength(length, id)
        }
    }

    suspend fun setCycleLutealLength(lutealPhaseLength: Long, id: Long){
        withContext(Dispatchers.IO){
            database.cycleQueries.setCycleLutealLength(lutealPhaseLength, id)
        }
    }


    //----- DELETE -----

    suspend fun deleteCycleById(id: Long){
        withContext(Dispatchers.IO) {
            database.cycleQueries.deleteCycleById(id)
        }
    }

    suspend fun deleteCycleByStartDate(startDate: String){
        withContext(Dispatchers.IO) {
            database.cycleQueries.deleteCycleByStartDate(startDate)
        }
    }
}