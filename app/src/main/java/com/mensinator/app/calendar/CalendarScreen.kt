package com.mensinator.app.calendar

import android.content.Context
import android.util.Log
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.mensinator.app.R
import com.mensinator.app.business.INotificationScheduler
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
    val context = LocalContext.current
    val notificationScheduler: INotificationScheduler = koinInject()

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
                    onAction = { uiAction -> viewModel.onAction(uiAction) },
                    day = day,
                )
            },
            monthHeader = {
                MonthTitle(yearMonth = it.yearMonth)
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
            PeriodButton(state, viewModel, context, notificationScheduler, buttonModifier)
            SymptomButton(showSymptomsDialog, state, buttonModifier)
            OvulationButton(state, context, viewModel, buttonModifier)
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
    viewModel: CalendarViewModel,
    context: Context,
    notificationScheduler: INotificationScheduler,
    modifier: Modifier = Modifier
) {
    var selectedIsPeriod = false
    val isPeriodButtonEnabled by remember {
        derivedStateOf { state.value.selectedDays.isNotEmpty() }
    }
    val successSaved = stringResource(id = R.string.successfully_saved_alert)
    Button(
        onClick = {
            viewModel.onAction(
                UiAction.UpdatePeriodDates(
                    currentPeriodDays = state.value.periodDates,
                    selectedDays = state.value.selectedDays
                )
            )

            // Schedule notification for reminder
            // Check that reminders should be scheduled (reminder>0)
            // and that it's more then reminderDays left (do not schedule notifications where there's too few reminderDays left until period)
            val periodReminderDays = state.value.periodReminderDays ?: 2
            val nextPeriodDate = state.value.periodPredictionDate
            val periodMessageText = state.value.periodMessageText
            if (periodReminderDays > 0 && nextPeriodDate != null && periodMessageText != null) {
                newSendNotification(
                    context,
                    notificationScheduler,
                    periodReminderDays,
                    nextPeriodDate,
                    periodMessageText
                )
            }
            Toast.makeText(context, successSaved, Toast.LENGTH_SHORT).show()
        },
        enabled = isPeriodButtonEnabled,  // Set the state of the Periods button
        modifier = modifier//.fillMaxWidth()
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
        Text(
            text = text,
            textAlign = TextAlign.Center,
        )
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
        enabled = state.value.selectedDays.isNotEmpty(),  // Set the state of the Symptoms button
        modifier = modifier//.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.symptoms_button),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OvulationButton(
    state: State<CalendarViewModel.ViewState>,
    context: Context,
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    // ovulation starts here
    var selectedIsOvulation = false
    val onlyOneOvulationAllowed = stringResource(id = R.string.only_day_alert)
    val successSavedOvulation = stringResource(id = R.string.success_saved_ovulation)
    val noDateSelectedOvulation = stringResource(id = R.string.no_date_selected_ovulation)
    val ovulationButtonEnabled by remember {
        derivedStateOf { state.value.selectedDays.size == 1 }
    }
    Button(
        onClick = {
            if (state.value.selectedDays.size > 1) {
                Toast.makeText(context, onlyOneOvulationAllowed, Toast.LENGTH_SHORT).show()
            } else if (ovulationButtonEnabled) {
                viewModel.onAction(UiAction.UpdateOvulationDay(state.value.selectedDays.first()))
                Toast.makeText(context, successSavedOvulation, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, noDateSelectedOvulation, Toast.LENGTH_SHORT).show()
            }
        },
        enabled = ovulationButtonEnabled,  // Set the state of the Ovulation button
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
        Text(
            text = text,
            textAlign = TextAlign.Center,
        )
    }
}


/**
 * Display the days of the week.
 */
@Composable
private fun DaysOfWeekTitle(daysOfWeek: PersistentList<DayOfWeek>) {
    Spacer(modifier = Modifier.height(4.dp))
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

    val settingColors = state.calendarColors.settingColors
    val dbHelper: IPeriodDatabaseHelper = koinInject()

    /**
     * Make sure the cells don't occupy so much space in landscape or on relatively big screens.
     */
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val heightClass = windowSizeClass.windowHeightSizeClass
    val widthClass = windowSizeClass.windowWidthSizeClass
    val aspectRatioModifier = when {
        heightClass == WindowHeightSizeClass.EXPANDED && widthClass == WindowWidthSizeClass.COMPACT -> {
            Modifier.aspectRatio(1f) // Ensure cells remain square
        }
        else -> {
            Modifier.aspectRatio(2f) // Make cells less tall
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

    val border = if (day.date.isEqual(LocalDate.now())) {
        BorderStroke(1.5.dp, fallbackColors.textColor.copy(alpha = 0.5f))
    } else null

    val fontStyleType = when {
        day.date.isEqual(LocalDate.now()) -> FontWeight.Bold
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
                val cycleNumber = calculateCycleNumber(day.date, dbHelper)
                if (cycleNumber > 0) {
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
fun calculateCycleNumber(day: LocalDate, dbHelper: IPeriodDatabaseHelper): Int {
    val lastPeriodStartDate = dbHelper.getFirstPreviousPeriodDate(day)
    if (lastPeriodStartDate == null) {
        // There are now passed periods from days date
        return 0
    }
    if (day > LocalDate.now()) {
        // Don't generate cycle numbers for future dates
        return 0
    }
    return ChronoUnit.DAYS.between(lastPeriodStartDate, day).toInt() + 1
}

//Return true if selected dates
//fun containsPeriodDate(selectedDates: Set<LocalDate>, periodDates: Map<LocalDate, Int>): Boolean {
//    return selectedDates.any { selectedDate ->
//        periodDates.containsKey(selectedDate)
//    }
//}
//
//fun containsOvulationDate(selectedDates: Set<LocalDate>, ovulationDates: Set<LocalDate>): Boolean {
//    return selectedDates.any { selectedDate ->
//        ovulationDates.contains(selectedDate)
//    }
//}

/**
 * Schedule a notification for a given date.
 */
fun newSendNotification(
    context: Context,
    scheduler: INotificationScheduler,
    daysForReminding: Int,
    periodDate: LocalDate,
    messageText: String
) {
    val notificationDate = periodDate.minusDays(daysForReminding.toLong())
    if (notificationDate.isBefore(LocalDate.now())) {
        Log.d(
            "CalendarScreen",
            "Notification not scheduled because the reminder date is in the past"
        )
        Toast.makeText(
            context,
            "Notification not scheduled because the date to remind you will be in the past",
            Toast.LENGTH_SHORT
        ).show()
    } else {
        //Schedule notification
        scheduler.scheduleNotification(notificationDate, messageText)
        Log.d("CalendarScreen", "Notification scheduled for $notificationDate")
    }
}
