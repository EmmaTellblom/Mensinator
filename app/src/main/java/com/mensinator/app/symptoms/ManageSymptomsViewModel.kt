package com.mensinator.app.symptoms

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mensinator.app.business.IPeriodDatabaseHelper
import com.mensinator.app.data.Symptom
import com.mensinator.app.data.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageSymptomsViewModel(
    @SuppressLint("StaticFieldLeak") private val appContext: Context,
    private val periodDatabaseHelper: IPeriodDatabaseHelper,
) : ViewModel() {


    private val _viewState = MutableStateFlow(
        ViewState()
    )
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    data class ViewState(
        val allSymptoms: List<Symptom> = listOf(),
        val activeSymptoms: List<Symptom> = listOf(),
        val showCreateSymptomDialog: Boolean = false,
        val symptomToRename: Symptom? = null,
        val symptomToDelete: Symptom? = null,
    )

    suspend fun refreshData() {
        withContext(Dispatchers.IO) {
            val allSymptoms = periodDatabaseHelper.getAllSymptoms()
            _viewState.update {
                it.copy(
                    allSymptoms = periodDatabaseHelper.getAllSymptoms(),
                    activeSymptoms = allSymptoms.filter { it.isActive },
                )
            }
        }
    }

    fun createNewSymptom(name: String) {
        periodDatabaseHelper.createNewSymptom(name)
        viewModelScope.launch { refreshData() }
    }

    fun updateSymptom(id: Int, active: Int, color: String) {
        periodDatabaseHelper.updateSymptom(id, active, color)
        viewModelScope.launch { refreshData() }
    }

    fun renameSymptom(id: Int, name: String) {
        periodDatabaseHelper.renameSymptom(id, name)
        viewModelScope.launch { refreshData() }
    }

    fun deleteSymptom(id: Int) {
        periodDatabaseHelper.deleteSymptom(id)
        viewModelScope.launch { refreshData() }
    }

    fun showCreateSymptomDialog(show: Boolean) {
        _viewState.update { it.copy(showCreateSymptomDialog = show) }
    }

    fun setSymptomToRename(symptom: Symptom?) {
        _viewState.update { it.copy(symptomToRename = symptom) }
    }

    fun setSymptomToDelete(symptom: Symptom?) {
        _viewState.update { it.copy(symptomToDelete = symptom) }
    }
}
