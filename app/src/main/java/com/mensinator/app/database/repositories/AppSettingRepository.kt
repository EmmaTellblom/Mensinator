package com.mensinator.app.database.repositories

import com.mensinator.app.database.MensinatorDB
import com.mensinator.app.database.App_setting
import com.mensinator.app.database.AppCategory
import com.mensinator.app.database.Colors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



class AppSettingRepository(private val database: MensinatorDB, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {



    //----- READ -----

    suspend fun getAllSettings(): List<App_setting> {
        return withContext(dispatcher){
            database.appSettingQueries.getAllSettings().executeAsList()
        }
    }

    suspend fun getAllSettingsByCategory(category: String): List<App_setting> {
        return withContext(dispatcher){
            database.appSettingQueries.getAllSettingsByCategory(category).executeAsList()
        }
    }


    //----- CREATE -----

    suspend fun insertAppSetting(label: String, value: String, category: String){
        withContext(dispatcher){
            database.appSettingQueries.insertAppSetting(label, value, category)
        }
    }


    //----- UPDATE -----

    suspend fun setAppSettingValue(value: String, label: String){
        withContext(dispatcher){
            database.appSettingQueries.setAppSettingValue(value, label)
        }
    }


    //----- DELETE -----

    suspend fun deleteAppSettingByLabel(label: String){
        withContext(dispatcher){
            database.appSettingQueries.deleteAppSettingByLabel(label)
        }
    }

    suspend fun deleteAppSettingsByCategory(category: String){
        withContext(dispatcher){
            database.appSettingQueries.deleteAppSettingsByCategory(category)
        }
    }
}