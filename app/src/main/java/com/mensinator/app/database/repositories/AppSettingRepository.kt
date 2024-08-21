package com.mensinator.app.database.repositories

import com.mensinator.app.database.MensinatorDB
import com.mensinator.app.database.App_setting
import com.mensinator.app.database.AppCategory
import com.mensinator.app.database.Colors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



class AppSettingRepository(private val database: MensinatorDB, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

    init {
        insertPredefinedSettings();
    }

    private fun insertPredefinedSettings(){
        if(database.appSettingQueries.getAllSettings().executeAsList().isEmpty()){
            //Color settings
            database.appSettingQueries.insertAppSetting("period_color", Colors.RED.value, AppCategory.COLORS.value)
            database.appSettingQueries.insertAppSetting("selection_color", Colors.LIGHT_GRAY.value, AppCategory.COLORS.value)
            database.appSettingQueries.insertAppSetting("period_selection_color", Colors.DARK_GRAY.value, AppCategory.COLORS.value)
            database.appSettingQueries.insertAppSetting("expected_period_color", Colors.YELLOW.value, AppCategory.COLORS.value)
            database.appSettingQueries.insertAppSetting("ovulation_color", Colors.BLUE.value, AppCategory.COLORS.value)
            database.appSettingQueries.insertAppSetting("expected_ovulation_color", Colors.MAGENTA.value, AppCategory.COLORS.value)

            //Reminder settings
            database.appSettingQueries.insertAppSetting("reminder_days", "0", AppCategory.REMINDERS.value)

            //Other settings
            database.appSettingQueries.insertAppSetting("luteal_period_calculation", "0", AppCategory.OTHERS.value)
            database.appSettingQueries.insertAppSetting("period_history", "5", AppCategory.OTHERS.value)
            database.appSettingQueries.insertAppSetting("ovulation_history", "5", AppCategory.OTHERS.value)
            database.appSettingQueries.insertAppSetting("language", "en", AppCategory.OTHERS.value)
            database.appSettingQueries.insertAppSetting("cycle_numbers_show", "1", AppCategory.OTHERS.value)
        }
    }

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