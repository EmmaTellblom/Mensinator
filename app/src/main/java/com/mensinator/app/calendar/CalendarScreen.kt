package com.mensinator.app.calendar

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.mensinator.app.R
import com.mensinator.app.business.IPeriodDatabaseHelper
import com.mensinator.app.calendar.CalendarViewModel.UiAction
import com.mensinator.app.extensions.stringRes
import com.mensinator.app.settings.ColorSetting
import com.mensinator.app.ui.navigation.displayCutoutExcludingStatusBarsPadding
import com.mensinator.app.ui.theme.Black
import com.mensinator.app.ui.theme.DarkGrey
import com.mensinator.app.ui.theme.isDarkMode
import kotlinx.collections.immutable.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    modifier: Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
    setToolbarOnClick: (() -> Unit) -> Unit,
) {
    val onAction = { uiAction: UiAction -> viewModel.onAction(uiAction) }

    val state = viewModel.viewState.collectAsStateWithLifecycle()
    val isDarkMode = isDarkMode()

    val currentYearMonth = YearMonth.now()
    val calendarState = rememberCalendarState(
        startMonth = currentYearMonth.minusMonths(30),
        endMonth = currentYearMonth.plusMonths(30),
        firstVisibleMonth = currentYearMonth,
    )
    val coroutineScope = rememberCoroutineScope()
    val showSymptomsDialog = remember { mutableStateOf(false) }

    LaunchedEffect(isDarkMode) { viewModel.updateDarkModeStatus(isDarkMode) }

    LaunchedEffect(Unit) {
        setToolbarOnClick {
            coroutineScope.launch {
                calendarState.animateScrollToMonth(YearMonth.now())
            }
        }
    }

    // Generate placement for calendar and buttons
    Column(
        modifier = modifier
            .fillMaxSize()
            .displayCutoutExcludingStatusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        LaunchedEffect(calendarState.firstVisibleMonth) {
            viewModel.onAction(UiAction.UpdateFocusedYearMonth(calendarState.firstVisibleMonth.yearMonth))
        }

        VerticalCalendar(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true) // Make this row occupy the maximum remaining height
                .padding(top = 16.dp),
            state = calendarState,
            dayContent = { day ->
                Day(
                    viewState = state,
                    onAction = onAction,
                    day = day,
                )
            },
            monthHeader = {
                MonthTitle(yearMonth = it.yearMonth)
                Spacer(modifier = Modifier.height(4.dp))
                DaysOfWeekTitle(daysOfWeek = daysOfWeek().toPersistentList())
            }
        )

        Spacer(modifier = Modifier.height(2.dp))

        FlowRow(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            val buttonModifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
            PeriodButton(state, onAction, buttonModifier)
            SymptomButton(showSymptomsDialog, state, buttonModifier)
            OvulationButton(state, onAction, buttonModifier)
        }

        // Show the SymptomsDialog
        if (showSymptomsDialog.value && state.value.selectedDays.isNotEmpty()) {
            val activeSymptoms = state.value.activeSymptoms
            val date = state.value.selectedDays.last()

            EditSymptomsForDaysDialog(
                date = date,  // Pass the last selected date
                symptoms = activeSymptoms,
                currentlyActiveSymptomIds = state.value.activeSymptomIdsForLatestSelectedDay,
                onSave = { selectedSymptoms ->
                    val selectedSymptomIds = selectedSymptoms.map { it.id }
                    showSymptomsDialog.value = false
                    viewModel.onAction(
                        UiAction.UpdateSymptomDates(
                            days = state.value.selectedDays,
                            selectedSymptomIds = selectedSymptomIds.toPersistentList()
                        )
                    )
                },
                onCancel = {
                    showSymptomsDialog.value = false
                    viewModel.onAction(UiAction.SelectDays(persistentSetOf()))
                }
            )
        }
    }
}

@Composable
private fun PeriodButton(
    state: State<CalendarViewModel.ViewState>,
    onAction: (uiAction: UiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedIsPeriod = false
    val isPeriodButtonEnabled by remember {
        derivedStateOf { state.value.selectedDays.isNotEmpty() }
    }
    val successSaved = stringResource(id = R.string.successfully_saved_alert)
    Button(
        onClick = {
            onAction(
                UiAction.UpdatePeriodDates(
                    currentPeriodDays = state.value.periodDates,
                    selectedDays = state.value.selectedDays
                )
            )
            Toast.makeText(context, successSaved, Toast.LENGTH_SHORT).show()
        },
        enabled = isPeriodButtonEnabled,
        modifier = modifier
    ) {
        for (selectedDate in state.value.selectedDays) {
            if (selectedDate in state.value.periodDates) {
                selectedIsPeriod = true
                break
            }
        }

        val text = when {
            selectedIsPeriod && isPeriodButtonEnabled -> {
                stringResource(id = R.string.period_button_selected)
            }
            !selectedIsPeriod && isPeriodButtonEnabled -> {
                stringResource(id = R.string.period_button_not_selected)
            }
            else -> stringResource(id = R.string.period_button)
        }
        ButtonText(text)
    }
}

@Composable
private fun SymptomButton(
    showSymptomsDialog: MutableState<Boolean>,
    state: State<CalendarViewModel.ViewState>,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { showSymptomsDialog.value = true },
        enabled = state.value.selectedDays.isNotEmpty(),
        modifier = modifier
    ) {
        ButtonText(stringResource(id = R.string.symptoms_button))
    }
}

@Composable
private fun OvulationButton(
    state: State<CalendarViewModel.ViewState>,
    onAction: (uiAction: UiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedIsOvulation = false
    val successSavedOvulation = stringResource(id = R.string.success_saved_ovulation)
    val ovulationButtonEnabled by remember {
        derivedStateOf { state.value.selectedDays.size == 1 }
    }
    Button(
        onClick = {
            onAction(UiAction.UpdateOvulationDay(state.value.selectedDays.first()))
            Toast.makeText(context, successSavedOvulation, Toast.LENGTH_SHORT).show()
        },
        enabled = ovulationButtonEnabled,
        modifier = modifier
    ) {
        for (selectedDate in state.value.selectedDays) {
            if (selectedDate in state.value.ovulationDates) {
                selectedIsOvulation = true
                break
            }
        }
        val text = when {
            selectedIsOvulation && ovulationButtonEnabled -> {
                stringResource(id = R.string.ovulation_button_selected)
            }
            !selectedIsOvulation && ovulationButtonEnabled -> {
                stringResource(id = R.string.ovulation_button_not_selected)
            }
            else -> stringResource(id = R.string.ovulation_button)
        }
        ButtonText(text)
    }
}

@Composable
private fun ButtonText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2
    )
}


/**
 * Display the days of the week.
 */
@Composable
private fun DaysOfWeekTitle(daysOfWeek: PersistentList<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                textAlign = TextAlign.Center,
                text = stringResource(id = dayOfWeek.stringRes),
            )
        }
    }
}

/**
 * Display the month title.
 */
@Composable
private fun MonthTitle(yearMonth: YearMonth) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center,
            text = "${stringResource(id = yearMonth.month.stringRes)} ${yearMonth.year}",
            style = MaterialTheme.typography.titleLarge, // Adjust text style as needed
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.primary,
            thickness = 2.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * Display a day in the calendar.
 */
@Composable
fun Day(
    viewState: State<CalendarViewModel.ViewState>,
    onAction: (uiAction: UiAction) -> Unit,
    day: CalendarDay,
) {
    val state = viewState.value
    val localDateNow = remember { LocalDate.now() }

    val settingColors = state.calendarColors.settingColors
    val dbHelper: IPeriodDatabaseHelper = koinInject()

    /**
     * Make sure the cells don't occupy so much space in landscape or on big (wide) screens.
     */
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val wideWindow =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val aspectRatioModifier = when {
        wideWindow -> {
            Modifier.aspectRatio(2f) // Make cells less tall
        }
        else -> {
            Modifier.aspectRatio(1f) // Ensure cells remain square
        }
    }

    if (day.position != DayPosition.MonthDate) {
        // Exclude dates that are not part of the current month
        Box(
            modifier = aspectRatioModifier // Maintain grid structure with empty space
        )
        return
    }

    val fallbackColors = if (isDarkMode()) {
        ColorCombination(DarkGrey, Color.White)
    } else {
        ColorCombination(Color.Transparent, Black)
    }
    val dayColors = when {
        day.date in state.selectedDays -> settingColors[ColorSetting.SELECTION]
        day.date in state.periodDates.keys -> settingColors[ColorSetting.PERIOD]
        state.periodPredictionDate?.isEqual(day.date) == true -> settingColors[ColorSetting.EXPECTED_PERIOD]
        day.date in state.ovulationDates -> settingColors[ColorSetting.OVULATION]
        state.ovulationPredictionDate?.isEqual(day.date) == true -> settingColors[ColorSetting.EXPECTED_OVULATION]
        else -> null
    } ?: fallbackColors

    val border = if (day.date.isEqual(localDateNow)) {
        BorderStroke(1.5.dp, fallbackColors.textColor.copy(alpha = 0.5f))
    } else null

    val fontStyleType = when {
        day.date.isEqual(localDateNow) -> FontWeight.Bold
        else -> FontWeight.Normal
    }

    // Dates to track
    val isSelected = day.date in state.selectedDays
    val hasSymptomDate = day.date in state.symptomDates

    val shape = MaterialTheme.shapes.small
    Surface(
        modifier = aspectRatioModifier
            .padding(2.dp)
            .clip(shape)
            .clickable {
                val newSelectedDates = if (isSelected) {
                    state.selectedDays - day.date
                } else {
                    state.selectedDays + day.date
                }.toPersistentSet()
                onAction(UiAction.SelectDays(newSelectedDates))
            },
        shape = shape,
        color = dayColors.backgroundColor,
        border = border
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day.date.dayOfMonth.toString(),
                fontWeight = fontStyleType,
                color = dayColors.textColor
            )

            // Add symptom circles
            if (hasSymptomDate) {
                val symptomsForDay = state.symptomDates.getOrDefault(day.date, setOf())

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy((-5).dp) // Overlap circles
                ) {
                    symptomsForDay.forEach { symptom ->
                        val symptomColor = state.calendarColors.symptomColors[symptom] ?: Color.Red

                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .background(symptomColor, CircleShape)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.25f), CircleShape)
                        )
                    }
                }
            }

            if (state.showCycleNumbers) {
                calculateCycleNumber(day.date, localDateNow, dbHelper)?.let { cycleNumber ->
                    Surface(
                        shape = shape,
                        color = Color.Transparent,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = cycleNumber.toString(),
                            fontSize = 8.sp,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = dayColors.textColor,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calculate the cycle number for a given date.
 */
fun calculateCycleNumber(day: LocalDate, now: LocalDate, dbHelper: IPeriodDatabaseHelper): Int? {
    // Don't generate cycle numbers for future dates
    if (day > now) return null

    val lastPeriodStartDate = dbHelper.getFirstPreviousPeriodDate(day) ?: return null

    return ChronoUnit.DAYS.between(lastPeriodStartDate, day).toInt() + 1
}
