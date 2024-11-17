package com.mensinator.app.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.mensinator.app.IExportImport
import com.mensinator.app.IPeriodDatabaseHelper
import com.mensinator.app.R
import com.mensinator.app.data.DataSource
import com.mensinator.app.settings.ColorSetting.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    private val periodDatabaseHelper: IPeriodDatabaseHelper,
    private val exportImport: IExportImport,
) : ViewModel() {

    private val dataSource = DataSource(isDarkTheme = false)

    private val _viewState = MutableStateFlow(
        ViewState(
            isDarkMode = false,
            periodColor = Color.Yellow,
            selectionColor = Color.Yellow,
            expectedPeriodColor = Color.Yellow,
            periodSelectionColor = Color.Yellow,
            ovulationColor = Color.Yellow,
            expectedOvulationColor = Color.Yellow,
            openColorPickerForSetting = null,

            daysBeforeReminder = 300,

            lutealPhaseCalculationEnabled = true,
        )
    )
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    data class ViewState(
        val isDarkMode: Boolean,

        val periodColor: Color,
        val selectionColor: Color,
        val expectedPeriodColor: Color,
        val periodSelectionColor: Color,
        val ovulationColor: Color,
        val expectedOvulationColor: Color,
        val openColorPickerForSetting: ColorSetting? = null,

        val daysBeforeReminder: Int,

        val lutealPhaseCalculationEnabled: Boolean,
    )

    fun updateDarkModeStatus(isDarkMode: Boolean) {
        _viewState.update { it.copy(isDarkMode = isDarkMode) }
        refreshColors()
    }

    private fun getColor(isDarkMode: Boolean, settingKey: String): Color {
        // TODO: Database calls block the UI!
        val colorName = periodDatabaseHelper.getSettingByKey(settingKey)?.value ?: "Red"
        return dataSource.getColor(isDarkMode, colorName)
    }

    fun refreshColors() {
        val isDarkMode = viewState.value.isDarkMode
        _viewState.update {
            it.copy(
                periodColor = getColor(isDarkMode, PERIOD.settingDbKey),
                selectionColor = getColor(isDarkMode, SELECTION.settingDbKey),
                expectedPeriodColor = getColor(isDarkMode, EXPECTED_PERIOD.settingDbKey),
                periodSelectionColor = getColor(isDarkMode, PERIOD_SELECTION.settingDbKey),
                ovulationColor = getColor(isDarkMode, OVULATION.settingDbKey),
                expectedOvulationColor = getColor(isDarkMode, EXPECTED_OVULATION.settingDbKey),
            )
        }
    }

    fun updateColorSetting(colorSetting: ColorSetting, newColorName: String) {
        periodDatabaseHelper.updateSetting(colorSetting.settingDbKey, newColorName)
        hideColorPicker()
        refreshColors()
    }

    fun openColorPicker(colorSetting: ColorSetting) {
        _viewState.update {
            it.copy(openColorPickerForSetting = colorSetting)
        }
    }

    fun hideColorPicker() {
        _viewState.update {
            it.copy(openColorPickerForSetting = null)
        }
    }
}

enum class ColorSetting(val stringResId: Int, val settingDbKey: String) {
    PERIOD(R.string.period_color, "period_color"),
    SELECTION(R.string.selection_color, "selection_color"),
    EXPECTED_PERIOD(R.string.expected_period_color, "expected_period_color"),
    PERIOD_SELECTION(R.string.period_selection_color, "period_selection_color"),
    OVULATION(R.string.ovulation_color, "ovulation_color"),
    EXPECTED_OVULATION(R.string.expected_ovulation_color, "expected_ovulation_color"),
}