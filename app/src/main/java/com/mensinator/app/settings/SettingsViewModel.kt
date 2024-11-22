package com.mensinator.app.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.mensinator.app.IExportImport
import com.mensinator.app.IPeriodDatabaseHelper
import com.mensinator.app.R
import com.mensinator.app.data.ColorSource
import com.mensinator.app.settings.ColorSetting.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    private val periodDatabaseHelper: IPeriodDatabaseHelper,
    @SuppressLint("StaticFieldLeak") private val appContext: Context,
    private val exportImport: IExportImport,
) : ViewModel() {
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

            showImportDialog = false,
            showExportDialog = false,
            defaultImportFilePath = exportImport.getDefaultImportFilePath(),
            exportFilePath = exportImport.getDocumentsExportFilePath(),

            showFaqDialog = false,
            appVersion = getAppVersion(appContext),
            dbVersion = periodDatabaseHelper.getDBVersion(),
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

        val showImportDialog: Boolean,
        val showExportDialog: Boolean,
        val defaultImportFilePath: String,
        val exportFilePath: String,

        val showFaqDialog: Boolean,
        val appVersion: String,
        val dbVersion: String,
    )

    fun init() {
        refreshColors()
        refreshInts()
        refreshBooleans()
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

    private fun refreshBooleans() {
        _viewState.update {
            it.copy(
                lutealPhaseCalculationEnabled = getBoolean(BooleanSetting.LUTEAL_PHASE_CALCULATION),
                showCycleNumbers = getBoolean(BooleanSetting.SHOW_CYCLE_NUMBERS),
                preventScreenshots = getBoolean(BooleanSetting.PREVENT_SCREENSHOTS),
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

    fun updateBooleanSetting(booleanSetting: BooleanSetting, newValue: Boolean) {
        val dbValue = if (newValue) "1" else "0"
        periodDatabaseHelper.updateSetting(booleanSetting.settingDbKey, dbValue)
        refreshBooleans()
    }


    fun showFaqDialog(show: Boolean) {
        _viewState.update { it.copy(showFaqDialog = show) }
    }

    private fun getColor(isDarkMode: Boolean, settingKey: String): Color {
        // TODO: Database calls block the UI!
        val colorName = periodDatabaseHelper.getSettingByKey(settingKey)?.value ?: "Red"
        return ColorSource.getColor(isDarkMode, colorName)
    }

    private fun getInt(settingKey: String): Int {
        val int = periodDatabaseHelper.getSettingByKey(settingKey)?.value ?: "0"
        return int.toIntOrNull() ?: 0
    }

    private fun getBoolean(booleanSetting: BooleanSetting): Boolean {
        val dbValue =
            periodDatabaseHelper.getSettingByKey(booleanSetting.settingDbKey)?.value ?: "0"
        val value = dbValue == "1" //
        return value
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            // Returns the version name, e.g., "1.8.4"
            packageInfo.versionName ?: throw PackageManager.NameNotFoundException()
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown" // Fallback if the version name is not found
        }
    }

    fun showImportDialog(show: Boolean) {
        _viewState.update { it.copy(showImportDialog = show) }
    }

    fun showExportDialog(show: Boolean) {
        _viewState.update { it.copy(showExportDialog = show) }
    }

    fun handleImport(importPath: String) {
        try {
            exportImport.importDatabase(importPath)
            Toast.makeText(
                appContext,
                "Data imported successfully from $importPath",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                appContext,
                "Error during import: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("Import", "Import error: ${e.message}", e)
        }
    }

    fun handleExport(exportPath: String) {
        try {
            exportImport.exportDatabase(exportPath)
            Toast.makeText(
                appContext,
                "Data exported successfully to $exportPath",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                appContext,
                "Error during export: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("Export", "Export error: ${e.message}", e)
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

enum class IntSetting(val stringResId: Int, val settingDbKey: String) {
    REMINDER_DAYS(R.string.days_before_reminder, "reminder_days"),
    PERIOD_HISTORY(R.string.period_history, "period_history"),
    OVULATION_HISTORY(R.string.ovulation_history, "ovulation_history"),
}

enum class BooleanSetting(val stringResId: Int, val settingDbKey: String) {
    LUTEAL_PHASE_CALCULATION(R.string.luteal_phase_calculation, "luteal_period_calculation"),
    SHOW_CYCLE_NUMBERS(R.string.cycle_numbers_show, "cycle_numbers_show"),
    PREVENT_SCREENSHOTS(R.string.screen_protection, "screen_protection"),
}