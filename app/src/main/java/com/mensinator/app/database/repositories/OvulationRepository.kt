package com.mensinator.app.database.repositories

import com.mensinator.app.database.Database
import com.mensinator.app.database.Ovulation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OvulationRepository(private val database: Database) {

    //----- READ -----

    suspend fun getOvulationByDate(date: String): Ovulation? {
       return withContext(Dispatchers.IO){
           database.ovulationQueries.getOvulationByDate(date).executeAsOneOrNull()
       }
   }

    suspend fun getOvulationByCycle(cycleId: Long): Ovulation? {
        return withContext(Dispatchers.IO){
            database.ovulationQueries.getOvulationByCycle(cycleId).executeAsOneOrNull()
        }
    }


    //----- CREATE -----

    suspend fun insertOvulation(date: String, cycleId: Long){
        withContext(Dispatchers.IO){
            database.ovulationQueries.insertOvulation(date, cycleId)
        }
    }


    //----- UPDATE -----


    //----- DELETE -----

    suspend fun deleteOvulationByDate(date: String){
        withContext(Dispatchers.IO){
            database.ovulationQueries.deleteOvulationByDate(date)
        }
    }

    suspend fun deleteOvulationByCycle(cycleId: Long){
        withContext(Dispatchers.IO){
            database.ovulationQueries.deleteOvulationByCycle(cycleId)
        }
    }
}