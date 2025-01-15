package com.mensinator.app


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.mensinator.app.data.ColorSource
import com.mensinator.app.navigation.displayCutoutExcludingStatusBarsPadding
import com.mensinator.app.settings.ResourceMapper
import com.mensinator.app.settings.StringSetting
import com.mensinator.app.ui.theme.isDarkMode
import org.koin.compose.koinInject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import com.kizitonwose.calendar.compose.*
import com.kizitonwose.calendar.core.*
import java.util.*
import java.time.format.TextStyle
/*
This file creates the calendar. A sort of "main screen".
 */
@Composable
fun CalendarScreen(modifier: Modifier) {
    val context = LocalContext.current
    val dbHelper: IPeriodDatabaseHelper = koinInject()
    val ovulationPrediction: IOvulationPrediction = koinInject()
    val periodPrediction: IPeriodPrediction = koinInject()
    val notificationScheduler: INotificationScheduler = koinInject()

    var nextPeriodDate = periodPrediction.getPredictedPeriodDate()
    var ovulationPredictionDate = ovulationPrediction.getPredictedOvulationDate()

    val currentMonth = remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    // Days selected in the calendar
    val selectedDates = remember { mutableStateOf(setOf<LocalDate>()) }

    // All period dates in the database
    val periodDates = remember { mutableStateOf(emptyMap<LocalDate, Int>()) }

    // Ovulation date for current month in the calendar
    val ovulationDates = remember { mutableStateOf(emptySet<LocalDate>()) }
    // All days with symptoms for current month in the calendar
    val symptomDates = remember { mutableStateOf(emptySet<LocalDate>()) }

    // All active symptoms in the database
    var symptoms by remember { mutableStateOf(emptyList<Symptom>()) }

    // Dialogs
    var showSymptomsDialog by remember { mutableStateOf(false) } // State to show the SymptomsDialog

    // Oldest first date of period in the database
    val oldestPeriodDate = dbHelper.getOldestPeriodDate() // Used to calculate cycle numbers

    // If set to 1, show cycle numbers on calendar
    // If set to 0, do not show cycle numbers
    val showCycleNumbersSetting =
        dbHelper.getSettingByKey("cycle_numbers_show")?.value?.toIntOrNull() ?: 1
    // Cycle number of date
    var cycleNumber: Int

    // How many days before next period a notification should be sent to user
    // If set to 0, do not send notification
    // From app_settings in the database
    val reminderDays = dbHelper.getSettingByKey("reminder_days")?.value?.toIntOrNull() ?: 2

    // The first date of previous period
    var previousFirstPeriodDate by remember { mutableStateOf<LocalDate?>(null) }
    val colorMap = ColorSource.getColorMap(isDarkMode())

    // Trigger notification with custom message
    val initPeriodKeyOrCustomMessage = dbHelper.getStringSettingByKey(StringSetting.PERIOD_NOTIFICATION_MESSAGE.settingDbKey)
    val periodMessageText = ResourceMapper.getStringResourceOrCustom(initPeriodKeyOrCustomMessage)

    val circleSize = 30.dp

    // Colors from app_settings in the database
    val periodColor =
        dbHelper.getSettingByKey("period_color")?.value?.let { colorMap[it] } ?: colorMap["Red"]!!
    val selectedColor = dbHelper.getSettingByKey("selection_color")?.value?.let { colorMap[it] }
        ?: colorMap["LightGray"]!!
    val nextPeriodColor =
        dbHelper.getSettingByKey("expected_period_color")?.value?.let { colorMap[it] }
            ?: colorMap["Yellow"]!!
    val selectedPeriodColor =
        dbHelper.getSettingByKey("period_selection_color")?.value?.let { colorMap[it] }
            ?: colorMap["DarkGray"]!!
    val ovulationColor = dbHelper.getSettingByKey("ovulation_color")?.value?.let { colorMap[it] }
        ?: colorMap["Blue"]!!
    val nextOvulationColor =
        dbHelper.getSettingByKey("expected_ovulation_color")?.value?.let { colorMap[it] }
            ?: colorMap["Magenta"]!!

    // Initializing symptom indicator color
    var symptomColor: Color

    var selectedIsOvulation = false
    var selectedIsPeriod = false

    // Function to refresh symptom dates
    fun refreshSymptomDates() {
        val year = currentMonth.value.year
        val month = currentMonth.value.monthValue
        symptomDates.value = dbHelper.getSymptomDatesForMonth(year, month)
    }

    // Function to refresh ovulation dates
    fun refreshOvulationDates() {
        val year = currentMonth.value.year
        val month = currentMonth.value.monthValue
        ovulationDates.value = dbHelper.getOvulationDatesForMonth(year, month).toSet()
        Log.d("CalendarScreen", "Refresh Ovulation Dates called. OvulationDates: $")
    }

    // Function to recalculate calculations
    fun updateCalculations() {
        ovulationPredictionDate = ovulationPrediction.getPredictedOvulationDate()
        nextPeriodDate = periodPrediction.getPredictedPeriodDate()
    }

    // Fetch symptoms from the database AND update data for calculations in stats screen
    LaunchedEffect(Unit) {
        updateCalculations() // Call updateCalculations on launch
        symptoms = dbHelper.getAllSymptoms().filter { it.isActive }
    }

    // Update button state based on selected dates
    val isSymptomsButtonEnabled by remember { derivedStateOf { selectedDates.value.isNotEmpty() } }
    val isOvulationButtonEnabled by remember {
        derivedStateOf {
            selectedDates.value.size == 1 && (containsOvulationDate(
                selectedDates.value,
                ovulationDates.value
            ) || !containsPeriodDate(selectedDates.value, periodDates.value))
        }
    } // Ovulation can only occur on one day
    val isPeriodsButtonEnabled by remember {
        derivedStateOf {
            selectedDates.value.isNotEmpty() && (containsPeriodDate(
                selectedDates.value,
                periodDates.value
            ) || !containsOvulationDate(selectedDates.value, ovulationDates.value))
        }
    }

    // Here is where the calendar is generated
    LaunchedEffect(currentMonth.value) {
        val year = currentMonth.value.year
        val month = currentMonth.value.monthValue
        periodDates.value = dbHelper.getPeriodDatesForMonth(year, month)
        symptomDates.value = dbHelper.getSymptomDatesForMonth(year, month)
        ovulationDates.value = dbHelper.getOvulationDatesForMonth(year, month)
        updateCalculations()
    }


    //UI Implementation
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

            var currentMonth by remember { mutableStateOf(YearMonth.now()) }
            val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
            val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed

            val state = rememberCalendarState(
                startMonth = startMonth,
                endMonth = endMonth,
                firstVisibleMonth = currentMonth,
                firstDayOfWeek = DayOfWeek.MONDAY
            )

            VerticalCalendar(
                state = state,
                dayContent = { day ->
                    // Customize the day view here
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            //.background(
                                //if (day.date in selectedDates.value) selectedColor else Color.Transparent,
                                //CircleShape
                            //)
                            .clickable {
                                selectedDates.value = if (day.date in selectedDates.value) {
                                    selectedDates.value - day.date
                                } else {
                                    selectedDates.value + day.date
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                //color = if (day.date.month == calendarState.firstVisibleMonth.month) Color.Black else Color.Gray
                            )
                        )
                    }
                },

                monthHeader = {
                    MonthTitle(month = currentMonth)
                    DaysOfWeekTitle(daysOfWeek = daysOfWeek()) // Use the title as month header
                },
                //monthHeader = { month ->
                    // Customize the month header
                    //val currentMonth = remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }

                    //Text(
                        //text = "${month.year}-${month.month}",
                   //     text = "hej",
                   //     style = MaterialTheme.typography.bodyMedium.copy(
                   //         fontWeight = FontWeight.Bold,
                   //         textAlign = TextAlign.Center
                   //     ),
                   //     modifier = Modifier.fillMaxWidth()
                   // )
                //},
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        val emptyClick = stringResource(id = R.string.statistics_title)
        val successSaved = stringResource(id = R.string.successfully_saved_alert)
        Button(
            onClick = {
                if (selectedDates.value.isEmpty()) {
                    Toast.makeText(context, emptyClick, Toast.LENGTH_SHORT).show()
                    return@Button
                }

                /**
                 * Make sure that if two or more days are selected (and at least one is already marked as period),
                 * we should make sure that all days are removed.
                 */
                val datesAlreadyMarkedAsPeriod =
                    selectedDates.value.intersect(periodDates.value.keys)
                if (datesAlreadyMarkedAsPeriod.isEmpty()) {
                    selectedDates.value.forEach {
                        val periodId = dbHelper.newFindOrCreatePeriodID(it)
                        dbHelper.addDateToPeriod(it, periodId)
                    }
                } else {
                    datesAlreadyMarkedAsPeriod.forEach { dbHelper.removeDateFromPeriod(it) }
                }

                selectedDates.value = setOf()

                val year = currentMonth.value.year
                val month = currentMonth.value.monthValue
                periodDates.value = dbHelper.getPeriodDatesForMonth(year, month)

                // Calculate the first day of the next month
                val firstDayOfNextMonth = if (month == 12) {
                    LocalDate.of(year + 1, 1, 1) // January 1st of next year
                } else {
                    LocalDate.of(year, month + 1, 1) // First of the next month in the same year
                }
                // Recalculate the previous periods first day the first day of the next month
                previousFirstPeriodDate = dbHelper.getFirstPreviousPeriodDate(firstDayOfNextMonth)

                updateCalculations()

                // Schedule notification for reminder
                // Check that reminders should be scheduled (reminder>0) and that the next period is in the future
                // and that it's more then reminderDays left (do not schedule notifications where there's too few reminderDays left until period)
                if (reminderDays > 0 && nextPeriodDate != LocalDate.parse("1900-01-01") && nextPeriodDate >= LocalDate.now()) {
                    newSendNotification(
                        context,
                        notificationScheduler,
                        reminderDays,
                        nextPeriodDate,
                        periodMessageText
                    )
                }
                Toast.makeText(context, successSaved, Toast.LENGTH_SHORT).show()
            },
            enabled = isPeriodsButtonEnabled,  // Set the state of the Periods button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {

            for (selectedDate in selectedDates.value) {
                if (selectedDate in periodDates.value) {
                    selectedIsPeriod = true
                    break
                }
            }

            val text = when {
                selectedIsPeriod && isPeriodsButtonEnabled -> {
                    stringResource(id = R.string.period_button_selected)
                }
                !selectedIsPeriod && isPeriodsButtonEnabled -> {
                    stringResource(id = R.string.period_button_not_selected)
                }
                else -> stringResource(id = R.string.period_button)
            }
            Text(text = text)
        }

        val noDataSelected = stringResource(id = R.string.no_data_selected)
        Button(
            onClick = {
                if (selectedDates.value.isNotEmpty()) {
                    showSymptomsDialog = true
                } else {
                    Toast.makeText(context, noDataSelected, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isSymptomsButtonEnabled,  // Set the state of the Symptoms button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = stringResource(id = R.string.symptoms_button))
        }

        //ovulation starts here
        val onlyDayAlert = stringResource(id = R.string.only_day_alert)
        val successSavedOvulation = stringResource(id = R.string.success_saved_ovulation)
        val noDateSelectedOvulation = stringResource(id = R.string.no_date_selected_ovulation)

        Button(
            onClick = {
                if (selectedDates.value.size > 1) {
                    Toast.makeText(context, onlyDayAlert, Toast.LENGTH_SHORT).show()
                } else if (selectedDates.value.size == 1) {
                    val date = selectedDates.value.first()
                    dbHelper.updateOvulationDate(date)
                    refreshOvulationDates()

                    Toast.makeText(context, successSavedOvulation, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, noDateSelectedOvulation, Toast.LENGTH_SHORT).show()
                }
                selectedDates.value = emptySet()
                updateCalculations()

                // Schedule notification for reminder
                if (reminderDays > 0 && nextPeriodDate != LocalDate.parse("1900-01-01") && nextPeriodDate >= LocalDate.now()) {
                    newSendNotification(
                        context,
                        notificationScheduler,
                        reminderDays,
                        nextPeriodDate,
                        periodMessageText
                    )
                }
            },
            enabled = isOvulationButtonEnabled,  // Set the state of the Ovulation button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            for (selectedDate in selectedDates.value) {
                if (selectedDate in ovulationDates.value) {
                    selectedIsOvulation = true
                    break
                }
            }
            val text = when {
                selectedIsOvulation && isOvulationButtonEnabled -> {
                    stringResource(id = R.string.ovulation_button_selected)
                }
                !selectedIsOvulation && isOvulationButtonEnabled -> {
                    stringResource(id = R.string.ovulation_button_not_selected)
                }
                else -> stringResource(id = R.string.ovulation_button)
            }
            Text(text = text)
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
                    selectedDates.value = emptySet()
                },
                onCancel = {
                    showSymptomsDialog = false
                    selectedDates.value = emptySet()
                }
            )
        }

        //Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}

/*
@Composable
fun MonthTitle(month: YearMonth) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + month.year,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
}*/

@Composable
fun MonthTitle(month: YearMonth) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            text = month.month.name + " " + month.year.toString(),
        )
    }
}


//Return true if selected dates
fun containsPeriodDate(selectedDates: Set<LocalDate>, periodDates: Map<LocalDate, Int>): Boolean {
    return selectedDates.any { selectedDate ->
        periodDates.containsKey(selectedDate)
    }
}

fun containsOvulationDate(selectedDates: Set<LocalDate>, ovulationDates: Set<LocalDate>): Boolean {
    return selectedDates.any { selectedDate ->
        ovulationDates.contains(selectedDate)
    }
}

fun newSendNotification(context: Context, scheduler: INotificationScheduler, daysForReminding: Int, periodDate: LocalDate, messageText: String) {
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


