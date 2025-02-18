package com.mensinator.app.calendar

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.yearMonth
import com.mensinator.app.business.IOvulationPrediction
import com.mensinator.app.business.IPeriodDatabaseHelper
import com.mensinator.app.business.IPeriodPrediction
import com.mensinator.app.business.PeriodId
import com.mensinator.app.data.ColorSource
import com.mensinator.app.data.Symptom
import com.mensinator.app.data.isActive
import com.mensinator.app.extensions.pickBestContrastTextColorForThisBackground
import com.mensinator.app.settings.ColorSetting
import com.mensinator.app.settings.IntSetting
import com.mensinator.app.settings.StringSetting
import com.mensinator.app.ui.ResourceMapper
import com.mensinator.app.ui.theme.Black
import com.mensinator.app.ui.theme.DarkGrey
import kotlinx.collections.immutable.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val dbHelper: IPeriodDatabaseHelper,
    @SuppressLint("StaticFieldLeak") private val appContext: Context,
    private val periodPrediction: IPeriodPrediction,
    private val ovulationPrediction: IOvulationPrediction,
) : ViewModel() {

    private val _viewState = MutableStateFlow(
        ViewState()
    )
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            val showCycleNumbersSetting =
                (dbHelper.getSettingByKey("cycle_numbers_show")?.value?.toIntOrNull() ?: 1) == 1
            val initPeriodKeyOrCustomMessage =
                dbHelper.getStringSettingByKey(StringSetting.PERIOD_NOTIFICATION_MESSAGE.settingDbKey)
            val periodMessageText =
                ResourceMapper.getStringResourceOrCustom(initPeriodKeyOrCustomMessage, appContext)

            _viewState.update {
                it.copy(
                    showCycleNumbers = showCycleNumbersSetting,
                    periodPredictionDate = periodPrediction.getPredictedPeriodDate(),
                    ovulationPredictionDate = ovulationPrediction.getPredictedOvulationDate(),
                    symptomDates = dbHelper.getSymptomDatesForMonthNew(
                        it.focusedYearMonth.year,
                        it.focusedYearMonth.monthValue
                    ).toPersistentSet(),
                    periodDates = dbHelper.getPeriodDatesForMonthNew(
                        it.focusedYearMonth.year,
                        it.focusedYearMonth.monthValue
                    ).toPersistentMap(),
                    ovulationDates = dbHelper.getOvulationDatesForMonthNew(
                        it.focusedYearMonth.year,
                        it.focusedYearMonth.monthValue
                    ).toPersistentSet(),
                    periodReminderDays = dbHelper.getSettingByKey(IntSetting.REMINDER_DAYS.settingDbKey)?.value?.toIntOrNull()
                        ?: 2,
                    activeSymptoms = dbHelper.getAllSymptoms()
                        .filter { it.isActive }
                        .toPersistentSet(),
                    periodMessageText = periodMessageText,
                    symptomsForDates = dbHelper.getSymptomsForDates(),
                )
            }
        }
    }

    fun updateDarkModeStatus(isDarkMode: Boolean) = viewModelScope.launch {
        _viewState.update {
            it.copy(
                isDarkMode = isDarkMode,
                calendarColors = getCalendarColorMap(isDarkMode)
            )
        }
    }

    fun onAction(uiAction: UiAction): Unit = when (uiAction) {
        is UiAction.UpdateFocusedYearMonth -> {
            _viewState.update {
                it.copy(focusedYearMonth = uiAction.focusedYearMonth)
            }
            deselectDatesIfFocusChangedTooMuch(uiAction.focusedYearMonth)
            refreshData()
        }
        is UiAction.SelectDays -> {
            viewModelScope.launch {
                _viewState.update {
                    val activeSymptoms = if (uiAction.days.isEmpty()) {
                        persistentSetOf()
                    } else {
                        dbHelper.getActiveSymptomIdsForDate(uiAction.days.last()).toPersistentSet()
                    }
                    it.copy(
                        selectedDays = uiAction.days,
                        activeSymptomIdsForLatestSelectedDay = activeSymptoms
                    )
                }
            }
            Unit
        }
        is UiAction.UpdateSymptomDates -> {
            dbHelper.updateSymptomDate(uiAction.days.toList(), uiAction.selectedSymptomIds)
            onAction(UiAction.SelectDays(persistentSetOf()))
            refreshData()
        }
        is UiAction.UpdateOvulationDay -> {
            dbHelper.updateOvulationDate(uiAction.ovulationDay)
            onAction(UiAction.SelectDays(persistentSetOf()))
            refreshData()
        }
        is UiAction.UpdatePeriodDates -> {
            /**
             * Make sure that if two or more days are selected (and at least one is already marked as period),
             * we should make sure that all days are removed.
             */
            val datesAlreadyMarkedAsPeriod =
                uiAction.selectedDays.intersect(uiAction.currentPeriodDays.keys)
            if (datesAlreadyMarkedAsPeriod.isEmpty()) {
                uiAction.selectedDays.forEach {
                    val periodId = dbHelper.newFindOrCreatePeriodID(it)
                    dbHelper.addDateToPeriod(it, periodId)
                }
            } else {
                datesAlreadyMarkedAsPeriod.forEach { dbHelper.removeDateFromPeriod(it) }
            }
            onAction(UiAction.SelectDays(persistentSetOf()))
            refreshData()
        }
    }

    /**
     * To avoid the user not seeing what they are editing, we deselect the data when the calendar
     * focus was changed too much. Possibly annoying as data could get discarded.
     */
    private fun deselectDatesIfFocusChangedTooMuch(newFocus: YearMonth) {
        val selectedDateYearMonths = viewState.value.selectedDays.map { it.yearMonth }
        if (selectedDateYearMonths.isEmpty()) return

        val minSelectedMonth = selectedDateYearMonths.min()
        val maxSelectedMonth = selectedDateYearMonths.max()

        val minAllowedMonth = minSelectedMonth.minusMonths(2)
        val maxAllowedMonth = maxSelectedMonth.plusMonths(1)

        if (newFocus >= minAllowedMonth && newFocus <= maxAllowedMonth) return

        _viewState.update {
            it.copy(
                selectedDays = persistentSetOf(),
                activeSymptomIdsForLatestSelectedDay = persistentSetOf()
            )
        }
    }

    private suspend fun getCalendarColorMap(isDarkMode: Boolean): CalendarColors {
        val settingColors = ColorSetting.entries.associateWith {
            val backgroundColor = ColorSource.getColor(
                isDarkMode,
                dbHelper.getSettingByKey(it.settingDbKey)?.value ?: "LightGray"
            )
            val textColor = backgroundColor.pickBestContrastTextColorForThisBackground(
                isDarkMode,
                DarkGrey,
                Black
            )

            ColorCombination(backgroundColor, textColor)
        }

        val symptomColors = dbHelper.getAllSymptoms().associateWith {
            ColorSource.getColor(
                isDarkMode,
                it.color
            )
        }

        return CalendarColors(settingColors, symptomColors)
    }

    data class ViewState(
        val isDarkMode: Boolean = false,

        val showCycleNumbers: Boolean = false,
        val focusedYearMonth: YearMonth = YearMonth.now(),
        val periodPredictionDate: LocalDate? = null,
        val ovulationPredictionDate: LocalDate? = null,
        val symptomDates: PersistentSet<LocalDate> = persistentSetOf(),
        val periodDates: PersistentMap<LocalDate, Int> = persistentMapOf(),
        val ovulationDates: PersistentSet<LocalDate> = persistentSetOf(),
        val periodReminderDays: Int? = null,
        val activeSymptoms: PersistentSet<Symptom> = persistentSetOf(),
        val periodMessageText: String? = null,
        val selectedDays: PersistentSet<LocalDate> = persistentSetOf(),
        val activeSymptomIdsForLatestSelectedDay: PersistentSet<Int> = persistentSetOf(),
        val symptomsForDates: Map<LocalDate, Set<Symptom>> = mapOf(),
        val calendarColors: CalendarColors = CalendarColors(mapOf(), mapOf())
    )

    data class CalendarColors(
        val settingColors: Map<ColorSetting, ColorCombination>,
        val symptomColors: Map<Symptom, Color>,
    )

    sealed class UiAction {
        data class UpdateFocusedYearMonth(val focusedYearMonth: YearMonth) : UiAction()
        data class SelectDays(val days: PersistentSet<LocalDate>) : UiAction()
        data class UpdateSymptomDates(
            val days: PersistentSet<LocalDate>,
            val selectedSymptomIds: PersistentList<Int>
        ) : UiAction()

        data class UpdateOvulationDay(val ovulationDay: LocalDate) : UiAction()
        data class UpdatePeriodDates(
            val currentPeriodDays: PersistentMap<LocalDate, PeriodId>,
            val selectedDays: PersistentSet<LocalDate>
        ) : UiAction()
    }
}

