package com.mensinator.app.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.mensinator.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {

    // TODO: Use PeriodDatabaseHelper in here, once we decided on a DI framework

    private val _viewState = MutableStateFlow(
        ViewState(
            periodColor = Color.Yellow,
            xyzColor = Color.Yellow,

            daysBeforeReminder = 300,

            lutealPhaseCalculationEnabled = true,
        )
    )
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    data class ViewState(
        // provide these in groups?
        val periodColor: Color,
        val xyzColor: Color,

        val daysBeforeReminder: Int,

        val lutealPhaseCalculationEnabled: Boolean,
    )

    fun updateColorSetting(colorSetting: ColorSetting, newColor: Color) {
        // TODO: Use PeriodDatabaseHelper in here
        //PeriodDatabaseHelper.updateSetting(colorSetting.settingDbKey, newColor)
        _viewState.update {
            it.copy(periodColor = newColor) // TODO: How to know which color to update at ViewState?
        }
    }
}

// TODO: No clue how to write a color to the db, is it something like settingDbKey?
enum class ColorSetting(val stringResId: Int, val settingDbKey: String) {
    PERIOD(R.string.period_color, "test")
}