package com.mensinator.app.database.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mensinator.app.database.App_setting
import com.mensinator.app.database.repositories.AppSettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppSettingViewModel(private val appSettingRepository: AppSettingRepository): ViewModel() {

    private val _appSettingsCategory = MutableStateFlow<List<App_setting>>(emptyList())
    val appSettingsCategory: StateFlow<List<App_setting>> = _appSettingsCategory


    //----- READ -----

    fun loadAllSettings(){
        viewModelScope.launch {
            _appSettingsCategory.value = appSettingRepository.getAllSettings()
        }
    }

    fun loadSettingsCategory(category: String){
        viewModelScope.launch {
            _appSettingsCategory.value = appSettingRepository.getAllSettingsByCategory(category)
        }
    }


    //----- CREATE -----

    fun addSetting(label: String, value: String, category: String){
        viewModelScope.launch {
            appSettingRepository.insertAppSetting(label, value, category)
        }
    }


    //----- UPDATE -----

    fun changeSettingValue(value: String, label: String){
        viewModelScope.launch {
            appSettingRepository.setAppSettingValue(value, label)
        }
    }


    //----- DELETE -----

    fun deleteSettingByLabel(label: String){
        viewModelScope.launch {
            appSettingRepository.deleteAppSettingByLabel(label)
        }
    }

    fun deleteSettingByCategory(category: String){
        viewModelScope.launch {
            appSettingRepository.deleteAppSettingsByCategory(category)
        }
    }
}