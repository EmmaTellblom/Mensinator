package com.mensinator.app.database.repositories

import com.mensinator.app.database.Database
import com.mensinator.app.database.App_setting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AppSettingRepository(private val database: Database) {

    //----- READ -----

    suspend fun getAllSettingsByCategory(category: String): List<App_setting> {
        return withContext(Dispatchers.IO){
            database.appSettingQueries.getAllSettingsByCategory(category).executeAsList()
        }
    }


    //----- CREATE -----

    suspend fun insertAppSetting(label: String, value: String, category: String){
        withContext(Dispatchers.IO){
            database.appSettingQueries.insertAppSetting(label, value, category)
        }
    }


    //----- UPDATE -----

    suspend fun setAppSettingValue(value: String, label: String){
        withContext(Dispatchers.IO){
            database.appSettingQueries.setAppSettingValue(value, label)
        }
    }


    //----- DELETE -----

    suspend fun deleteAppSettingByLabel(label: String){
        withContext(Dispatchers.IO){
            database.appSettingQueries.deleteAppSettingByLabel(label)
        }
    }

    suspend fun deleteAppSettingsByCategory(category: String){
        withContext(Dispatchers.IO){
            database.appSettingQueries.deleteAppSettingsByCategory(category)
        }
    }
}