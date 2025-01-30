package com.mensinator.app

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.mensinator.app.data.ColorSource
import com.mensinator.app.navigation.displayCutoutExcludingStatusBarsPadding
import com.mensinator.app.settings.ResourceMapper
import com.mensinator.app.settings.StringSetting
import com.mensinator.app.ui.theme.isDarkMode
import kotlinx.collections.immutable.*
import org.koin.compose.koinInject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/*
This function is the initiator of the vertical calendar.
 */
@Composable
fun CalendarScreen(modifier: Modifier) {

    val context = LocalContext.current

    val periodPrediction: IPeriodPrediction = koinInject()
    val ovulationPrediction: IOvulationPrediction = koinInject()
    val dbHelper: IPeriodDatabaseHelper = koinInject()
    val notificationScheduler: INotificationScheduler = koinInject()

    // Days selected in the calendar
    val selectedDates = remember { mutableStateOf(persistentSetOf<LocalDate>()) }

    val actualPeriodDates = remember { mutableStateOf(persistentMapOf<LocalDate, Int>()) }
    val actualOvulationDates = remember { mutableStateOf(persistentSetOf<LocalDate>()) }
    val actualSymptomDates = remember { mutableStateOf(persistentSetOf<LocalDate>()) }

    var ovulationPredictionDate = ovulationPrediction.getPredictedOvulationDate()
    var periodPredictionDate = periodPrediction.getPredictedPeriodDate()
    val periodReminderDays = dbHelper.getSettingByKey("reminder_days")?.value?.toIntOrNull() ?: 2
    var nextPeriodDate = periodPrediction.getPredictedPeriodDate()

    // Trigger notification with custom message
    val initPeriodKeyOrCustomMessage =
        dbHelper.getStringSettingByKey(StringSetting.PERIOD_NOTIFICATION_MESSAGE.settingDbKey)
    val periodMessageText = ResourceMapper.getStringResourceOrCustom(initPeriodKeyOrCustomMessage)

    var selectedIsOvulation = false
    var selectedIsPeriod = false

    val currentMonth = remember { YearMonth.now() }
    val focusedYearMonth = remember { mutableStateOf(currentMonth) }

    val daysOfWeek = daysOfWeek()

    /**
     * Refresh the dates for the current month.
     */
    fun refreshOvulationDates() {
        val year = focusedYearMonth.value.year
        val month = focusedYearMonth.value.monthValue
        actualOvulationDates.value =
            dbHelper.getOvulationDatesForMonth(year, month).toPersistentSet()
    }

    /**
     * Refresh the dates for the current month.
     */
    fun refreshSymptomDates() {
        val year = focusedYearMonth.value.year
        val month = focusedYearMonth.value.monthValue
        actualSymptomDates.value = dbHelper.getSymptomDatesForMonth(year, month).toPersistentSet()
    }

    /**
     * Recalculate the calculations for the current month.
     */
    fun updateCalculations() {
        ovulationPredictionDate = ovulationPrediction.getPredictedOvulationDate()
        periodPredictionDate = periodPrediction.getPredictedPeriodDate()
    }

// Generate placement for calendar and buttons
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .displayCutoutExcludingStatusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true) // Make this row occupy the maximum remaining height
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            val currentMonth = remember { YearMonth.now() }
            val startMonth =
                remember { currentMonth.minusMonths(50) } // Adjust as needed TODO: Fix this  to be dynamic!
            val endMonth =
                remember { currentMonth.plusMonths(50) } // Adjust as needed TODO: Fix this  to be dynamic!

            val state = rememberCalendarState(
                startMonth = startMonth,
                endMonth = endMonth,
                firstVisibleMonth = currentMonth,
            )

            LaunchedEffect(state.firstVisibleMonth) {
                focusedYearMonth.value = state.firstVisibleMonth.yearMonth
                // Load data for the new month
                actualPeriodDates.value = dbHelper.getPeriodDatesForMonthNew(
                    focusedYearMonth.value.year,
                    focusedYearMonth.value.monthValue
                ).toPersistentMap()
                actualOvulationDates.value = dbHelper.getOvulationDatesForMonthNew(
                    focusedYearMonth.value.year,
                    focusedYearMonth.value.monthValue
                ).toPersistentSet()
                actualSymptomDates.value = dbHelper.getSymptomDatesForMonthNew(
                    focusedYearMonth.value.year,
                    focusedYearMonth.value.monthValue
                ).toPersistentSet()
                updateCalculations()
            }

            VerticalCalendar(
                state = state,
                dayContent = { day ->
                    Day(
                        day = day,
                        selectedDates = selectedDates,
                        actualPeriodDates = actualPeriodDates.value,
                        actualOvulationDates = actualOvulationDates.value,
                        actualSymptomDates = actualSymptomDates.value,
                        ovulationPredictionDate = ovulationPredictionDate,
                        periodPredictionDate = periodPredictionDate
                    )
                },
                monthHeader = {
                    MonthTitle(month = it.yearMonth)
                    DaysOfWeekTitle(daysOfWeek = daysOfWeek.toPersistentList())
                }

            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isPeriodButtonEnabled by remember {
            derivedStateOf { selectedDates.value.isNotEmpty() }
        }

        val successSaved = stringResource(id = R.string.successfully_saved_alert)
        Button(
            onClick = {
                /**
                 * Make sure that if two or more days are selected (and at least one is already marked as period),
                 * we should make sure that all days are removed.
                 */
                val datesAlreadyMarkedAsPeriod =
                    selectedDates.value.intersect(actualPeriodDates.value.keys)
                if (datesAlreadyMarkedAsPeriod.isEmpty()) {
                    selectedDates.value.forEach {
                        val periodId = dbHelper.newFindOrCreatePeriodID(it)
                        dbHelper.addDateToPeriod(it, periodId)
                    }
                } else {
                    datesAlreadyMarkedAsPeriod.forEach { dbHelper.removeDateFromPeriod(it) }
                }

                selectedDates.value = persistentSetOf()

                val year = focusedYearMonth.value.year
                val month = focusedYearMonth.value.monthValue
                actualPeriodDates.value = dbHelper.getPeriodDatesForMonth(year, month).toPersistentMap()

                updateCalculations()

                // Schedule notification for reminder
                // Check that reminders should be scheduled (reminder>0) and that the next period is in the future
                // and that it's more then reminderDays left (do not schedule notifications where there's too few reminderDays left until period)
                if (periodReminderDays > 0 && nextPeriodDate != LocalDate.parse("1900-01-01") && nextPeriodDate >= LocalDate.now()) {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {

            for (selectedDate in selectedDates.value) {
                if (selectedDate in actualPeriodDates.value) {
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
            Text(text = text)
        }

        var showSymptomsDialog by remember { mutableStateOf(false) }
        val symptomButtonEnabled by remember {
            derivedStateOf { selectedDates.value.isNotEmpty() }
        }

        Button(
            onClick = {
                showSymptomsDialog = true
            },
            enabled = symptomButtonEnabled,  // Set the state of the Symptoms button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = stringResource(id = R.string.symptoms_button))
            //Text(text = "Symptoms")
        }

        // Show the SymptomsDialog
        if (showSymptomsDialog && selectedDates.value.isNotEmpty()) {
            val activeSymptoms = dbHelper.getAllSymptoms().filter { it.isActive }

            SymptomsDialog(
                date = selectedDates.value.last(),  // Pass the last selected date
                symptoms = activeSymptoms,
                dbHelper = dbHelper,
                onSave = { selectedSymptoms ->
                    val selectedSymptomIds = selectedSymptoms.map { it.id }
                    val datesToUpdate = selectedDates.value.toList()
                    dbHelper.updateSymptomDate(datesToUpdate, selectedSymptomIds)
                    showSymptomsDialog = false
                    refreshSymptomDates()
                    selectedDates.value = persistentSetOf()
                },
                onCancel = {
                    showSymptomsDialog = false
                    selectedDates.value = persistentSetOf()
                }
            )
        }

        //ovulation starts here
        val onlyOneOvulationAllowed = stringResource(id = R.string.only_day_alert)
        val successSavedOvulation = stringResource(id = R.string.success_saved_ovulation)
        val noDateSelectedOvulation = stringResource(id = R.string.no_date_selected_ovulation)
        val ovulationButtonEnabled by remember {
            derivedStateOf { selectedDates.value.size == 1 }
        }
        Button(
            onClick = {
                if (selectedDates.value.size > 1) {
                    Toast.makeText(context, onlyOneOvulationAllowed, Toast.LENGTH_SHORT).show()
                } else if (selectedDates.value.size == 1) {
                    val date = selectedDates.value.first()
                    dbHelper.updateOvulationDate(date)
                    selectedDates.value = persistentSetOf()

                    refreshOvulationDates()
                    updateCalculations()

                    Toast.makeText(context, successSavedOvulation, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, noDateSelectedOvulation, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = ovulationButtonEnabled,  // Set the state of the Ovulation button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            for (selectedDate in selectedDates.value) {
                if (selectedDate in actualOvulationDates.value) {
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
            Text(text = text)
        }

    }
}

/**
 * Display the days of the week.
 */
@Composable
fun DaysOfWeekTitle(daysOfWeek: PersistentList<DayOfWeek>) {
    Spacer(modifier = Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            val dayStringRes = when (dayOfWeek) {
                DayOfWeek.MONDAY -> R.string.mon
                DayOfWeek.TUESDAY -> R.string.tue
                DayOfWeek.WEDNESDAY -> R.string.wed
                DayOfWeek.THURSDAY -> R.string.thu
                DayOfWeek.FRIDAY -> R.string.fri
                DayOfWeek.SATURDAY -> R.string.sat
                DayOfWeek.SUNDAY -> R.string.sun
            }
            Text(
                modifier = Modifier.weight(1f),
                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                textAlign = TextAlign.Center,
                text = stringResource(id = dayStringRes),
            )
        }
    }
}

/**
 * Display the month title.
 */
@Composable
fun MonthTitle(month: YearMonth) {
    val monthStringRes = when (month.month.value) {
        1 -> R.string.january
        2 -> R.string.february
        3 -> R.string.march
        4 -> R.string.april
        5 -> R.string.may
        6 -> R.string.june
        7 -> R.string.july
        8 -> R.string.august
        9 -> R.string.september
        10 -> R.string.october
        11 -> R.string.november
        12 -> R.string.december
        else -> throw IllegalArgumentException("Invalid month value")
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center,
            //text = "${month.month.name} ${month.year}",
            text = "${stringResource(id = monthStringRes)} ${month.year}",
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
    day: CalendarDay,
    selectedDates: MutableState<PersistentSet<LocalDate>>,
    actualPeriodDates: PersistentMap<LocalDate, Int>,
    actualOvulationDates: PersistentSet<LocalDate>,
    actualSymptomDates: PersistentSet<LocalDate>,
    ovulationPredictionDate: LocalDate,
    periodPredictionDate: LocalDate
) {

    val colorMap = ColorSource.getColorMap(isDarkMode())
    val dbHelper: IPeriodDatabaseHelper = koinInject()

    val showCycleNumbersSetting =
        dbHelper.getSettingByKey("cycle_numbers_show")?.value?.toIntOrNull() ?: 1

    if (day.position != DayPosition.MonthDate) {
        // Exclude dates that are not part of the current month
        Box(
            modifier = Modifier
                .aspectRatio(1f) // Maintain grid structure with empty space
        )
        return
    }

    //Colors
    val periodColor =
        dbHelper.getSettingByKey("period_color")?.value?.let { colorMap[it] } ?: colorMap["Red"]!!
    val selectedColor = dbHelper.getSettingByKey("selection_color")?.value?.let { colorMap[it] }
        ?: colorMap["LightGray"]!!
    val nextPeriodColor =
        dbHelper.getSettingByKey("expected_period_color")?.value?.let { colorMap[it] }
            ?: colorMap["Yellow"]!!
    val ovulationColor = dbHelper.getSettingByKey("ovulation_color")?.value?.let { colorMap[it] }
        ?: colorMap["Blue"]!!
    val nextOvulationColor =
        dbHelper.getSettingByKey("expected_ovulation_color")?.value?.let { colorMap[it] }
            ?: colorMap["Magenta"]!!

    val backgroundColor = when {
        day.date in selectedDates.value -> selectedColor
        day.date in actualPeriodDates.keys -> periodColor
        day.date.isEqual(periodPredictionDate) -> nextPeriodColor
        day.date in actualOvulationDates -> ovulationColor
        day.date.isEqual(ovulationPredictionDate) -> nextOvulationColor
        else -> Color.Transparent
    }


    val borderSize = when {
        day.date.isEqual(LocalDate.now()) -> 1.dp
        else -> 0.dp
    }

    val borderColor = when {
        day.date.isEqual(LocalDate.now()) -> Color.LightGray
        else -> Color.Transparent
    }

    val fontStyleType = when {
        day.date.isEqual(LocalDate.now()) -> FontWeight.Bold
        else -> FontWeight.Normal

    }

    //Dates to track
    val isSelected = day.date in selectedDates.value
    val hasSymptomDate = day.date in actualSymptomDates

    Box(
        modifier = Modifier
            .aspectRatio(1f) // This ensures the cells remain square.
            //.size(circleSize)
            .background(
                backgroundColor,
                shape = MaterialTheme.shapes.small
            )
            .border(borderSize, color = borderColor, shape = CircleShape)
            .clickable {
                val newSelectedDates = if (isSelected) {
                    selectedDates.value - day.date
                } else {
                    selectedDates.value + day.date
                }.toPersistentSet()
                selectedDates.value = newSelectedDates
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            fontWeight = fontStyleType,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )

        // Add symptom circles
        if (hasSymptomDate) {
            val symptomsForDay = dbHelper.getSymptomColorForDate(day.date)

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy((-5).dp) // Overlapping circles
            ) {
                symptomsForDay.forEach { symptom ->
                    val symptomColor = colorMap[symptom] ?: Color.Black

                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .background(symptomColor, CircleShape)
                    )
                }
            }
        }

        if (showCycleNumbersSetting == 1) {
            val cycleNumber = calculateCycleNumber(day.date, dbHelper)
            if (cycleNumber > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            Color.Transparent,
                        )
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = cycleNumber.toString(),
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 8.sp,
                            textAlign = TextAlign.Left
                        ),
                        modifier = Modifier.padding(2.dp)
                    )
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
