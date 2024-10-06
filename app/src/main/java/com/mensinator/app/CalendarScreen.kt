package com.mensinator.app


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.mensinator.app.data.DataSource
import com.mensinator.app.ui.theme.isDarkMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/*
This file creates the calendar. A sort of "main screen".
 */

@Composable
fun CalendarScreen(
) {
    val context = LocalContext.current

    // new objects for prediction
    val periodPrediction = PeriodPrediction(context)
    var nextPeriodDate = periodPrediction.getPredictedPeriodDate()
    val ovulationPrediction = OvulationPrediction(context)
    var ovulationPredictionDate = ovulationPrediction.getPredictedOvulationDate()

    // For accessing database functions
    val dbHelper = remember { PeriodDatabaseHelper(context) }

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
    val colorMap = DataSource(isDarkMode()).colorMap

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
        symptoms = dbHelper.getAllActiveSymptoms()
        val newLocale = dbHelper.getSettingByKey("lang")?.value ?: "en"
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(newLocale)
        )
    }

    // Update button state based on selected dates
    val isSymptomsButtonEnabled by remember { derivedStateOf { selectedDates.value.isNotEmpty() } }
    val isOvulationButtonEnabled by remember { derivedStateOf { selectedDates.value.size == 1 } } // Ovulation can only occur on one day
    val isPeriodsButtonEnabled by remember { derivedStateOf { selectedDates.value.isNotEmpty() } }

    // Here is where the calendar is generated
    LaunchedEffect(currentMonth.value) {
        val year = currentMonth.value.year
        val month = currentMonth.value.monthValue
        periodDates.value = dbHelper.getPeriodDatesForMonth(year, month)
        symptomDates.value = dbHelper.getSymptomDatesForMonth(year, month)
        ovulationDates.value = dbHelper.getOvulationDatesForMonth(year, month)
        updateCalculations()
    }

    val monthName = mapOf(
        1 to R.string.january,
        2 to R.string.february,
        3 to R.string.march,
        4 to R.string.april,
        5 to R.string.may,
        6 to R.string.june,
        7 to R.string.july,
        8 to R.string.august,
        9 to R.string.september,
        10 to R.string.october,
        11 to R.string.november,
        12 to R.string.december
    )

    //List of Days of Week
    val daysOfWeek = listOf(
        stringResource(id = R.string.mon),
        stringResource(id = R.string.tue),
        stringResource(id = R.string.wed),
        stringResource(id = R.string.thu),
        stringResource(id = R.string.fri),
        stringResource(id = R.string.sat),
        stringResource(id = R.string.sun)
    )

    //UI Implementation
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    )
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                currentMonth.value = currentMonth.value.minusMonths(1)
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }

            Text(
                text = "${monthName[currentMonth.value.month.value]?.let { stringResource(id = it) }} ${currentMonth.value.year}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                currentMonth.value = currentMonth.value.plusMonths(1)
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        val firstDayOfMonth = currentMonth.value.withDayOfMonth(1).dayOfWeek
        val daysInMonth = currentMonth.value.lengthOfMonth()
        val dayOffset = (firstDayOfMonth.value - DayOfWeek.MONDAY.value + 7) % 7

        Spacer(modifier = Modifier.padding(5.dp))

        for (week in 0..5) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                for (day in 0..6) {
                    val dayOfMonth = week * 7 + day - dayOffset + 1
                    if (dayOfMonth in 1..daysInMonth) {
                        val dayDate = currentMonth.value.withDayOfMonth(dayOfMonth)
                        val isSelected = dayDate in selectedDates.value
                        val hasPeriodDate = dayDate in periodDates.value
                        val hasSymptomDate = dayDate in symptomDates.value
                        val hasOvulationDate = dayDate in ovulationDates.value
                        val hasOvulationDateCalculated =
                            dayDate == if(ovulationPredictionDate != LocalDate.parse("1900-01-01")) ovulationPredictionDate else false
                        val hasPeriodDateCalculated =
                            dayDate == if(nextPeriodDate != LocalDate.parse("1900-01-01")) nextPeriodDate else false

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (isSelected) {
                                        selectedDates.value -= dayDate
                                    } else {
                                        selectedDates.value += dayDate
                                    }
                                }
                                .drawWithContent {
                                    drawContent() // Draw the content first
                                    val strokeWidth = 1.dp.toPx()
                                    val y = strokeWidth / 2 // Adjust y to start from the top
                                    drawLine(
                                        color = Color.LightGray, // Replace with your desired color
                                        strokeWidth = strokeWidth,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y)
                                    )
                                }
                                .padding(bottom = 15.dp, top = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            // If date is a calculated period date
                            if (dayDate == nextPeriodDate && !hasPeriodDate) {
                                Box(
                                    modifier = Modifier
                                        .size(circleSize)
                                        .background(nextPeriodColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            // If date is a calculated ovulation date (and not an ovulation by user)
                            if (dayDate.toString() == ovulationPredictionDate.toString() && !hasOvulationDate) {
                                Box(
                                    modifier = Modifier
                                        .size(circleSize)
                                        .background(nextOvulationColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            val backgroundColor = when {
                                hasPeriodDateCalculated && isSelected -> selectedPeriodColor
                                hasOvulationDateCalculated && isSelected -> selectedPeriodColor
                                hasPeriodDate && isSelected -> selectedPeriodColor
                                hasOvulationDate && isSelected -> selectedPeriodColor
                                hasPeriodDate -> periodColor
                                hasOvulationDate -> ovulationColor
                                isSelected -> selectedColor
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .size(circleSize)
                                    .background(backgroundColor, CircleShape)
                            )

                            // Symptom indicator in the top right corner
                            if (hasSymptomDate) {
                                val noSymptomsForDay = dbHelper.getSymptomColorForDate(dayDate)

                                Row(
                                    modifier = Modifier
                                        .offset(y = 12.dp)
                                        .align(Alignment.BottomCenter),
                                    horizontalArrangement = Arrangement.spacedBy((-5).dp)  // Negative spacing for overlap
                                ) {
                                    noSymptomsForDay.forEach { symp ->
                                        symptomColor = colorMap[symp] ?: Color.Black

                                        Box(
                                            modifier = Modifier
                                                .size(11.dp)  // Size of the small bubble
                                                .background(symptomColor, CircleShape)
                                        )
                                    }
                                }

                            }

                            // Mark today's date with a black border and bold font
                            if (dayDate == LocalDate.now()) {
                                Box(
                                    modifier = Modifier
                                        .size(circleSize)
                                        .border(1.dp, Color.LightGray, CircleShape)
                                        .background(Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else { // Regular dates
                                Text(
                                    text = dayOfMonth.toString(),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .background(Color.Transparent)
                                )
                            }

                            // Here is cycle numbers
                            if (oldestPeriodDate != null && showCycleNumbersSetting == 1) {
                                if (dayDate >= oldestPeriodDate && dayDate <= LocalDate.now()) {
                                    previousFirstPeriodDate =
                                        dbHelper.getFirstPreviousPeriodDate(dayDate)
                                    if (previousFirstPeriodDate != null) {
                                        // Calculate the number of days between the firstLastPeriodDate and dayDate
                                        cycleNumber = ChronoUnit.DAYS.between(
                                            previousFirstPeriodDate,
                                            dayDate
                                        )
                                            .toInt() + 1

                                        // Render UI elements based on cycleNumber or other logic
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
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.weight(0.2f))
        }

        val emptyClick = stringResource(id = R.string.statistics_title)
        val successSaved = stringResource(id = R.string.successfully_saved_alert)
        Button(
            onClick = {
                if (selectedDates.value.isEmpty()) {
                    Toast.makeText(context, emptyClick, Toast.LENGTH_SHORT).show()
                } else {
                    for (date in selectedDates.value) {
                        if (date in periodDates.value) {
                            dbHelper.removeDateFromPeriod(date)
                        } else {
                            val periodId = dbHelper.newFindOrCreatePeriodID(date)
                            dbHelper.addDateToPeriod(date, periodId)
                        }
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
                    previousFirstPeriodDate =
                        dbHelper.getFirstPreviousPeriodDate(firstDayOfNextMonth)

                    updateCalculations()

                    // Schedule notification for reminder
                    // Check that reminders should be scheduled (reminder>0) and that the next period is in the future
                    // and that its more then reminderDays left (do not schedule notifications where there's to few reminderdays left until period)
                    if (reminderDays > 0 && nextPeriodDate != LocalDate.parse("1900-01-01") && nextPeriodDate >= LocalDate.now()) {
                        newSendNotification(
                            context,
                            reminderDays,
                            nextPeriodDate
                        )
                    }
                    Toast.makeText(context, successSaved, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isPeriodsButtonEnabled,  // Set the state of the Periods button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = stringResource(id = R.string.period_button))
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
                        reminderDays,
                        nextPeriodDate
                    )
                }
            },
            enabled = isOvulationButtonEnabled,  // Set the state of the Ovulation button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = stringResource(id = R.string.ovulation_button))
        }

        // Show the SymptomsDialog
        if (showSymptomsDialog && selectedDates.value.isNotEmpty()) {
            val activeSymptoms = dbHelper.getAllActiveSymptoms()

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

        Spacer(modifier = Modifier.weight(1f))

    }
}

fun newSendNotification(context: Context, daysForReminding: Int, periodDate: LocalDate) {

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
        val scheduler = NotificationScheduler(context)
        scheduler.scheduleNotification(notificationDate)
        Log.d("CalendarScreen", "Notification scheduled for $notificationDate")

    }

}