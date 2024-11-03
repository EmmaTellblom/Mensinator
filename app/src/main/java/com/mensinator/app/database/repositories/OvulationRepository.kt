package com.mensinator.app.database.repositories

import com.mensinator.app.database.MensinatorDB
import com.mensinator.app.database.Ovulation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OvulationRepository(private val database: MensinatorDB, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

    //----- READ -----

    suspend fun getAllOvulations(): List<Ovulation> {
        return withContext(dispatcher){
            database.ovulationQueries.getAllOvulations().executeAsList()
        }
    }

    suspend fun getOvulationByDate(date: String): Ovulation? {
       return withContext(dispatcher){
           database.ovulationQueries.getOvulationByDate(date).executeAsOneOrNull()
       }
   }

    suspend fun getOvulationByCycle(cycleId: Long): Ovulation? {
        return withContext(dispatcher){
            database.ovulationQueries.getOvulationByCycle(cycleId).executeAsOneOrNull()
        }
    }


    //----- CREATE -----

    suspend fun insertOvulation(date: String, cycleId: Long){
        withContext(dispatcher){
            database.ovulationQueries.insertOvulation(date, cycleId)
        }
    }


    //----- UPDATE -----


    //----- DELETE -----

    suspend fun deleteOvulationByDate(date: String){
        withContext(dispatcher){
            database.ovulationQueries.deleteOvulationByDate(date)
        }
    }

    suspend fun deleteOvulationByCycle(cycleId: Long){
        withContext(dispatcher){
            database.ovulationQueries.deleteOvulationByCycle(cycleId)
        }
    }
}