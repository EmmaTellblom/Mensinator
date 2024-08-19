package com.mensinator.app.database.repositories

import com.mensinator.app.database.Database
import com.mensinator.app.database.Period
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PeriodRepository(private val database: Database) {

    //----- READ -----

    suspend fun getPeriodByDate(date: String): Period? {
        return withContext(Dispatchers.IO){
            database.periodQueries.getPeriodByDate(date).executeAsOneOrNull()
        }
    }

    suspend fun getAllPeriodsByCycle(cycleId: Long): List<Period> {
        return withContext(Dispatchers.IO){
            database.periodQueries.getAllPeriodsByCycle(cycleId).executeAsList()
        }
    }


    //----- CREATE -----

    suspend fun insertPeriod(date: String, cycleId: Long){
        withContext(Dispatchers.IO){
            database.periodQueries.insertPeriod(date, cycleId)
        }
    }


    //----- UPDATE -----


    //----- DELETE -----

    suspend fun deletePeriodByDate(date: String){
        withContext(Dispatchers.IO){
            database.periodQueries.deletePeriodByDate(date)
        }
    }

    suspend fun deletePeriodsByCycle(cycleId: Long){
        withContext(Dispatchers.IO){
            database.periodQueries.deletePeriodsByCycle(cycleId)
        }
    }
}