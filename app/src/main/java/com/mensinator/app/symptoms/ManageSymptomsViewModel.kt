package com.mensinator.app.symptoms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mensinator.app.business.IPeriodDatabaseHelper
import com.mensinator.app.data.Symptom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageSymptomsViewModel(
    private val periodDatabaseHelper: IPeriodDatabaseHelper,
) : ViewModel() {

    private val _viewState = MutableStateFlow(
        ViewState()
    )
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    data class ViewState(
        val allSymptoms: List<Symptom> = listOf(),
        val showCreateSymptomDialog: Boolean = false,
        val symptomToRename: Symptom? = null,
        val symptomToDelete: Symptom? = null,
    )

    fun onAction(uiAction: UiAction) = when (uiAction) {
        UiAction.HideCreationDialog -> _viewState.update { it.copy(showCreateSymptomDialog = false) }
        UiAction.ShowCreationDialog -> _viewState.update { it.copy(showCreateSymptomDialog = true) }

        UiAction.HideDeletionDialog -> _viewState.update { it.copy(symptomToDelete = null) }
        is UiAction.ShowDeletionDialog -> _viewState.update { it.copy(symptomToDelete = uiAction.symptom ) }

        UiAction.HideRenamingDialog -> _viewState.update { it.copy(symptomToRename = null) }
        is UiAction.ShowRenamingDialog -> _viewState.update { it.copy( symptomToRename = uiAction.symptom ) }

        is UiAction.CreateSymptom -> createNewSymptom(uiAction.name)
        is UiAction.UpdateSymptom -> updateSymptom(uiAction.symptom)
        is UiAction.DeleteSymptom -> deleteSymptom(uiAction.symptom)
        is UiAction.RenameSymptom -> renameSymptom(uiAction.symptom)
    }

    suspend fun refreshData() {
        withContext(Dispatchers.IO) {
            _viewState.update {
                it.copy(
                    allSymptoms = periodDatabaseHelper.getAllSymptoms(),
                )
            }
        }
    }

    private fun createNewSymptom(name: String) {
        periodDatabaseHelper.createNewSymptom(name)
        viewModelScope.launch { refreshData() }
    }

    private fun updateSymptom(symptom: Symptom) {
        periodDatabaseHelper.updateSymptom(symptom.id, symptom.active, symptom.color)
        viewModelScope.launch { refreshData() }
    }

    private fun renameSymptom(symptom: Symptom) {
        periodDatabaseHelper.renameSymptom(symptom.id, symptom.name)
        viewModelScope.launch { refreshData() }
    }

    private fun deleteSymptom(symptom: Symptom) {
        periodDatabaseHelper.deleteSymptom(symptom.id)
        viewModelScope.launch { refreshData() }
    }

    sealed class UiAction {
        data object HideRenamingDialog : UiAction()
        data class ShowRenamingDialog(val symptom: Symptom): UiAction()

        data object HideDeletionDialog : UiAction()
        data class ShowDeletionDialog(val symptom: Symptom): UiAction()

        data object HideCreationDialog : UiAction()
        data object ShowCreationDialog: UiAction()

        data class CreateSymptom(val name: String): UiAction()
        data class UpdateSymptom(val symptom: Symptom): UiAction()
        data class DeleteSymptom(val symptom: Symptom): UiAction()
        data class RenameSymptom(val symptom: Symptom): UiAction()
    }
}
