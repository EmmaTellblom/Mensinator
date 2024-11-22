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

            daysBeforeReminder = -1,
            daysForPeriodHistory = -1,
            daysForOvulationHistory = -1,
            openIntPickerForSetting = null,

            lutealPhaseCalculationEnabled = false,
            showCycleNumbers = false,
            preventScreenshots = false,
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
        val daysForPeriodHistory: Int,
        val daysForOvulationHistory: Int,
        val openIntPickerForSetting: IntSetting? = null,

        val lutealPhaseCalculationEnabled: Boolean,
        val showCycleNumbers: Boolean,
        val preventScreenshots: Boolean,
    )

    fun init() {
        refreshColors()
        refreshInts()
    }

    fun updateDarkModeStatus(isDarkMode: Boolean) {
        _viewState.update { it.copy(isDarkMode = isDarkMode) }
        refreshColors()
    }

    private fun refreshColors() {
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

    private fun refreshInts() {
        _viewState.update {
            it.copy(
                daysBeforeReminder = getInt(IntSetting.REMINDER_DAYS.settingDbKey),
                daysForPeriodHistory = getInt(IntSetting.PERIOD_HISTORY.settingDbKey),
                daysForOvulationHistory = getInt(IntSetting.OVULATION_HISTORY.settingDbKey),
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

    private fun getColor(isDarkMode: Boolean, settingKey: String): Color {
        // TODO: Database calls block the UI!
        val colorName = periodDatabaseHelper.getSettingByKey(settingKey)?.value ?: "Red"
        return dataSource.getColor(isDarkMode, colorName)
    }

    private fun getInt(settingKey: String): Int {
        val int = periodDatabaseHelper.getSettingByKey(settingKey)?.value ?: "0"
        return int.toIntOrNull() ?: 0
    }

    fun hideIntPicker() {
        _viewState.update { it.copy(openIntPickerForSetting = null) }
    }

    fun openIntPicker(intSetting: IntSetting) {
        _viewState.update { it.copy(openIntPickerForSetting = intSetting) }
    }

    fun updateIntSetting(intSetting: IntSetting, newNumber: Int) {
        periodDatabaseHelper.updateSetting(intSetting.settingDbKey, newNumber.toString())
        hideIntPicker()
        refreshInts()
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

enum class IntSetting(val stringResId: Int, val settingDbKey: String) {
    REMINDER_DAYS(R.string.days_before_reminder, "reminder_days"),
    PERIOD_HISTORY(R.string.period_history, "period_history"),
    OVULATION_HISTORY(R.string.ovulation_history, "ovulation_history"),
}