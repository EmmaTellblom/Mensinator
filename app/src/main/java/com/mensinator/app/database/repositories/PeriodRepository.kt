package com.mensinator.app.database.repositories

import com.mensinator.app.database.MensinatorDB
import com.mensinator.app.database.Period
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PeriodRepository(private val database: MensinatorDB, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

    //----- READ -----

    suspend fun getAllPeriods(): List<Period> {
        return withContext(dispatcher){
            database.periodQueries.getAllPeriods().executeAsList()
        }
    }

    suspend fun getPeriodByDate(date: String): Period? {
        return withContext(dispatcher){
            database.periodQueries.getPeriodByDate(date).executeAsOneOrNull()
        }
    }

    suspend fun getAllPeriodsByCycle(cycleId: Long): List<Period> {
        return withContext(dispatcher){
            database.periodQueries.getAllPeriodsByCycle(cycleId).executeAsList()
        }
    }


    //----- CREATE -----

    suspend fun insertPeriod(date: String, cycleId: Long){
        withContext(dispatcher){
            database.periodQueries.insertPeriod(date, cycleId)
        }
    }


    //----- UPDATE -----


    //----- DELETE -----

    suspend fun deletePeriodByDate(date: String){
        withContext(dispatcher){
            database.periodQueries.deletePeriodByDate(date)
        }
    }

    suspend fun deletePeriodsByCycle(cycleId: Long){
        withContext(dispatcher){
            database.periodQueries.deletePeriodsByCycle(cycleId)
        }
    }
}