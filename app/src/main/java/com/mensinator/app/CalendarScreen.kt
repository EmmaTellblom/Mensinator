package com.mensinator.app

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import androidx.compose.ui.res.stringResource
import androidx.work.Constraints
import androidx.work.NetworkType

/*
This file creates the calendar. A sort of "main screen".
 */

@Composable
fun CalendarScreen() {
    val context = LocalContext.current

    // For accessing database functions
    val dbHelper = remember { PeriodDatabaseHelper(context) }
    // For accessing calculation functions
    val calcHelper = remember { Calculations(context) }

    val currentMonth = remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    // Days selected in the calendar
    val selectedDates = remember { mutableStateOf(setOf<LocalDate>()) }

    // All period dates in the database
    val periodDates = remember { mutableStateOf(emptyMap<LocalDate, Int>()) }

    // Ovulation date for current month in the calendar
    val ovulationDates = remember { mutableStateOf(emptySet<LocalDate>()) }
    // All days with symptoms for current month in the calendar
    val symptomDates = remember { mutableStateOf(emptySet<LocalDate>()) }

    var periodCount by remember { mutableIntStateOf(0) } // Count of all period cycles in the database
    var ovulationCount by remember { mutableIntStateOf(0) } // Count of all ovulation dates in the database


    // All active symptoms in the database
    var symptoms by remember { mutableStateOf(emptyList<Symptom>()) }

    // Dialogs
    var showCreateNewSymptomDialog by remember { mutableStateOf(false) }  // State to show the CreateNewSymptomDialog
    var showStatisticsDialog by remember { mutableStateOf(false) }  // State to show the StatisticsDialog
    var showFAQDialog by remember { mutableStateOf(false) } // State to show the FAQDialog
    var showSettingsDialog by remember { mutableStateOf(false) } // State to show the SettingsDialog
    var showExportImportDialog by remember { mutableStateOf(false) } // State to show the ExportImportDialog
    var showSymptomsDialog by remember { mutableStateOf(false) } // State to show the SymptomsDialog
    var showManageSymptomsDialog by remember { mutableStateOf(false) } // State to show the ManageSymptomsDialog - Activate/Deactivate symptoms and colors

    // Previous ovulation date
    var lastOvulationDate by remember { mutableStateOf<LocalDate?>(null) }
    // Follicle growth days
    var follicleGrowthDays by remember { mutableStateOf("0") }

    // Oldest first date of period in the database
    val oldestPeriodDate = dbHelper.getOldestPeriodDate() // Used to calculate cycle numbers

    // If set to 1, show cycle numbers on calendar
    // If set to 0, do not show cycle numbers
    val showCycleNumbersSetting = dbHelper.getSettingByKey("cycle_numbers_show")?.value?.toIntOrNull() ?: 1
    // Cycle number of date
    var cycleNumber: Int

    val isScreenProtectionEnabled = remember { mutableStateOf(true) }

    // How many days before next period a notification should be sent to user
    // If set to 0, do not send notification
    // From app_settings in the database
    val reminderDays = dbHelper.getSettingByKey("reminder_days")?.value?.toIntOrNull() ?: 2

    // Variables which will contain predicted dates for period and ovulation
    var nextPeriodStartCalculated by remember { mutableStateOf("Not enough data") }
    var nextOvulationCalculated by remember { mutableStateOf("Not enough data") }

    // The first date of previous period
    var previousFirstPeriodDate by remember { mutableStateOf<LocalDate?>(null) }

    // Color map to map string from database to HEX color in UI
    // These HEX-values can be changed if needed
    val colorMap = mapOf(
        "Red" to Color(0xFFEF5350),        // Softer red
        "Green" to Color(0xFF66BB6A),      // Softer green
        "Blue" to Color(0xFF42A5F5),       // Softer blue
        "Yellow" to Color(0xFFFFEB3B),     // Softer yellow
        "Cyan" to Color(0xFF4DD0E1),       // Softer cyan
        "Magenta" to Color(0xFFAB47BC),    // Softer magenta
        "Black" to Color(0xFF212121),      // Softer black (dark gray)
        "White" to Color(0xFFF5F5F5),      // Softer white (light gray)
        "DarkGray" to Color(0xFF616161),   // Softer dark gray
        "LightGray" to Color(0xFFBDBDBD)  // Softer light gray
    )

    // Colors from app_settings in the database
    val periodColor = dbHelper.getSettingByKey("period_color")?.value?.let { colorMap[it] } ?: colorMap["Red"]!!
    val selectedColor = dbHelper.getSettingByKey("selected_color")?.value?.let { colorMap[it] } ?: colorMap["LightGray"]!!
    val nextPeriodColor = dbHelper.getSettingByKey("expected_period_color")?.value?.let { colorMap[it] } ?: colorMap["Yellow"]!!
    val selectedPeriodColor = dbHelper.getSettingByKey("period_selection_color")?.value?.let { colorMap[it] } ?: colorMap["DarkGray"]!!
    val ovulationColor = dbHelper.getSettingByKey("ovulation_color")?.value?.let { colorMap[it] } ?: colorMap["Blue"]!!
    val nextOvulationColor = dbHelper.getSettingByKey("expected_ovulation_color")?.value?.let { colorMap[it] } ?: colorMap["Magenta"]!!

    // Initializing symptom indicator color
    var symptomColor: Color

    // Fetch symptoms from the database
    LaunchedEffect(Unit) {
        symptoms = dbHelper.getAllActiveSymptoms()
        val newLocale = dbHelper.getSettingByKey("lang")?.value ?: "en"
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(newLocale)
        )
    }

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
    }

    // Function to recalculate calculations
    fun updateCalculations() {
        periodCount = dbHelper.getPeriodCount()
        ovulationCount = dbHelper.getOvulationCount()

        nextPeriodStartCalculated = if (periodCount>=2) { // If there is at least 2 cycles, then we can calculate next period
            calcHelper.calculateNextPeriod()
        } else {
            "Not enough data"
        }

        // Ovulation statistics
        lastOvulationDate = dbHelper.getNewestOvulationDate()
        ovulationCount = dbHelper.getOvulationCount()

        // Predict the next ovulation date
        // Make sure there is at least one period and one ovulation date
        // Make sure the last ovulation date is before the last first period date
        if (ovulationCount >= 1 && periodCount >= 1 && (lastOvulationDate.toString()<previousFirstPeriodDate.toString())) {
            follicleGrowthDays = calcHelper.averageFollicalGrowthInDays()
            nextOvulationCalculated = previousFirstPeriodDate?.plusDays(follicleGrowthDays.toLong()).toString()

        } else { // If Ovulation is after previous first period date and prediction exists for Period, calculate next ovulation based on calculated start of period
            if(lastOvulationDate.toString()>previousFirstPeriodDate.toString() && (nextPeriodStartCalculated != "Not enough data")){
                follicleGrowthDays = calcHelper.averageFollicalGrowthInDays()
                if(follicleGrowthDays!="Not enough data"){
                    nextOvulationCalculated = LocalDate.parse(nextPeriodStartCalculated).plusDays(follicleGrowthDays.toLong()).toString()
                }
            }
            else{ // There is not enough data to make ovulation calculation
                nextOvulationCalculated = "Not enough data"
            }
        }
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                currentMonth.value = currentMonth.value.minusMonths(1)
            }) {
                //Text(stringResource(id = R.string.previous))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            // Get the current locale from AppCompatDelegate
            val localeList = AppCompatDelegate.getApplicationLocales()
            val currentLocale = if (localeList.isEmpty) {
                Locale.ENGLISH // Fallback to English if locale list is empty
            } else {
                localeList[0] // Retrieve the first locale from the list
            }

            Text(
                text = "${currentMonth.value.month.getDisplayName(TextStyle.FULL, currentLocale)} ${currentMonth.value.year}".capitalized(),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                currentMonth.value = currentMonth.value.plusMonths(1)
            }) {
                //Text(text = stringResource(id = R.string.next))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

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

        for (week in 0..5) {
            Row {
                for (day in 0..6) {
                    val dayOfMonth = week * 7 + day - dayOffset + 1
                    if (dayOfMonth in 1..daysInMonth) {
                        val dayDate = currentMonth.value.withDayOfMonth(dayOfMonth)
                        val isSelected = dayDate in selectedDates.value
                        val hasPeriodDate = dayDate in periodDates.value
                        val hasSymptomDate = dayDate in symptomDates.value
                        val hasOvulationDate = dayDate in ovulationDates.value
                        val hasOvulationDateCalculated = dayDate.toString() == nextOvulationCalculated
                        val hasPeriodDateCalculated = dayDate.toString() == nextPeriodStartCalculated

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                //.padding(8.dp)
                                .clickable {
                                    if (isSelected) {
                                        selectedDates.value -= dayDate
                                    } else {
                                        selectedDates.value += dayDate
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            // If date is a calculated period date
                            if (dayDate.toString().trim() == nextPeriodStartCalculated.trim() && !hasPeriodDate) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(nextPeriodColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            // If date is a calculated ovulation date (and not an ovulation by user)
                            if (dayDate.toString() == nextOvulationCalculated && !hasOvulationDate) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(nextOvulationColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        color = Color.Black,
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
                                    .size(32.dp)
                                    .background(backgroundColor, CircleShape)
                            )

                            // Symptom indicator in the top right corner
                            if (hasSymptomDate) {
                                val noSymptomsForDay = dbHelper.getSymptomColorForDate(dayDate)

                                if(noSymptomsForDay.size == 1){
                                    val colorName = noSymptomsForDay[0]
                                    symptomColor = colorMap[colorName] ?: Color.Black
                                }
                                else{
                                    // Display a grey circle if multiple symptoms
                                    symptomColor = Color.Gray
                                }

                                Box(
                                    modifier = Modifier
                                        .offset(x = (-8).dp, y = (0).dp)
                                        .size(8.dp)  // Size of the small bubble
                                        .border(1.dp, Color.Black, CircleShape)
                                        .background(
                                            symptomColor,
                                            CircleShape
                                        )
                                        .align(Alignment.TopEnd)
                                )

                            }

                            // Mark today's date with a black border and bold font
                            if (dayDate == LocalDate.now()) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .border(1.dp, Color.Black, CircleShape)
                                        .background(Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else { // Regular dates
                                Text(
                                    text = dayOfMonth.toString(),
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .background(Color.Transparent)
                                )
                            }

                    // Here is cycle numbers
                    if(oldestPeriodDate != null && showCycleNumbersSetting == 1) {
                        if (dayDate >= oldestPeriodDate && dayDate <= LocalDate.now()) {
                            previousFirstPeriodDate = dbHelper.getFirstPreviousPeriodDate(dayDate)
                            if (previousFirstPeriodDate != null) {
                                // Calculate the number of days between the firstLastPeriodDate and dayDate
                                cycleNumber = ChronoUnit.DAYS.between(previousFirstPeriodDate, dayDate)
                                    .toInt() + 1

                                // Render UI elements based on cycleNumber or other logic
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(
                                            Color.Transparent,
                                            //CircleShape
                                        )
                                        .align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = cycleNumber.toString(),
                                        style = androidx.compose.ui.text.TextStyle(
                                            color = Color.Black,
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
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    previousFirstPeriodDate = dbHelper.getFirstPreviousPeriodDate(firstDayOfNextMonth)

                    updateCalculations()

                    // Schedule notification for reminder
                    // Check that reminders should be scheduled (reminder>0) and that the next period is in the future
                    // and that its more then reminderDays left (do not schedule notifications where there's to few reminderdays left until period)
                    if(reminderDays>0 && nextPeriodStartCalculated != "Not enough data" && nextPeriodStartCalculated>=LocalDate.now().toString())
                    {
                        sendNotification(context, reminderDays, LocalDate.parse(nextPeriodStartCalculated))
                    }
                    Toast.makeText(context, successSaved, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isPeriodsButtonEnabled,  // Set the state of the Periods button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = stringResource(id = R.string.period_button))
        }

        val noDataSelected = stringResource(id = R.string.no_data_selected)
        Button(
            onClick = {
                //selectedDate = selectedDates.value.firstOrNull()  // Select the first date as the date for the SymptomsDialog
                if (selectedDates.value.isNotEmpty()) {
                    showSymptomsDialog = true
                } else {
                    Toast.makeText(context,noDataSelected , Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isSymptomsButtonEnabled,  // Set the state of the Symptoms button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = stringResource(id = R.string.symptoms_button))
        }

        //ovulation starts here
        val onlyDayAlert = stringResource(id = R.string.only_day_alert)
        val successSavedOvulation = stringResource(id = R.string.success_saved_ovulation)
        val noDateSelectedOvulation = stringResource(id = R.string.no_date_selected_ovulation)

        Button(
            onClick = {
                if(selectedDates.value.size > 1){
                    Toast.makeText(context, onlyDayAlert, Toast.LENGTH_SHORT).show()
                }
                else if(selectedDates.value.size == 1){
                    val date = selectedDates.value.first()
                    dbHelper.updateOvulationDate(date)
                    refreshOvulationDates()

                    Toast.makeText(context, successSavedOvulation, Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(context, noDateSelectedOvulation, Toast.LENGTH_SHORT).show()
                }
                selectedDates.value = emptySet()
                updateCalculations()

                // Schedule notification for reminder
                if(reminderDays>0 && nextPeriodStartCalculated != "Not enough data" && nextPeriodStartCalculated>=LocalDate.now().toString()){
                    sendNotification(context, reminderDays, LocalDate.parse(nextPeriodStartCalculated))
                }
            },
            enabled = isOvulationButtonEnabled,  // Set the state of the Ovulation button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
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
                },
                onCreateNewSymptom = {
                    showCreateNewSymptomDialog = true
                }
            )
        }

        if (showCreateNewSymptomDialog) {
            CreateNewSymptomDialog(
                newSymptom = "",  // Pass an empty string for new symptoms
                onSave = { newSymptomName ->
                    dbHelper.createNewSymptom(newSymptomName)
                    symptoms = dbHelper.getAllActiveSymptoms()
                    showCreateNewSymptomDialog = false  // Close the new symptom dialog
                },
                onCancel = {
                    showCreateNewSymptomDialog = false  // Close the new symptom dialog
                }
            )
        }

        // Show the StatisticsDialog
        if (showStatisticsDialog) {
            StatisticsDialog(
                //nextPeriodStart = nextPeriodStartCalculated, // This needs to stay in CalendarScreen due to being marked in Calendar, so we can send it to statistics
                follicleGrowthDays = follicleGrowthDays, // This is calculated in CalendarScreen for Ovulation, so we can send it to statistics
                //nextPredictedOvulation = nextOvulationCalculated, // This needs to stay in CalendarScreen due to being marked in Calendar, so we can send it to statistics
                onDismissRequest = { showStatisticsDialog = false }
            )
        }

        // Show the FAQ Dialog
        if (showFAQDialog) {
            FAQDialog(
                onDismissRequest = { showFAQDialog = false }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                onDismissRequest = { showSettingsDialog = false }
            )
        }

        if (showManageSymptomsDialog) {
            ManageSymptom(
                onSave = {
                    // Refresh symptoms when saving
                    refreshSymptomDates()
                    Log.d("TAG", "Saved clicked on ManageSymptoms")
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

    } // Here is the menu (FAB) right bottom corner
    NestedFAB(
        onStatisticsClick = {
            showStatisticsDialog = true
        },
        onFAQClick = {
            showFAQDialog = true
        },
        onSettingsClick = {
            showSettingsDialog = true
        },
        onExportImportClick = {
            showExportImportDialog = true
        },
        onManageSymptomsClick = {
            showManageSymptomsDialog = true
        }
    )
}

// Function to create a notification for upcoming periods
// used if reminderDays > 0
fun sendNotification(context: Context, daysForReminding: Int, periodDate: LocalDate) {
    // Calculate the notification date (periodDate - daysForReminding)
    val notificationDate = periodDate.minusDays(daysForReminding.toLong())

    // Check if the notification date is in the past
    if (notificationDate.isBefore(LocalDate.now())) {
        Log.d("CalendarScreen", "Notification not scheduled because the reminder date is in the past")
        return
    }

    // Convert the LocalDate to a timestamp (milliseconds since epoch)
    val notificationTimeMillis = notificationDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val currentTimeMillis = System.currentTimeMillis()

    // Calculate the delay in milliseconds
    val delayMillis = notificationTimeMillis - currentTimeMillis

    // Handle negative delay
    if (delayMillis < 0) {
        Log.d("CalendarScreen", "Notification not scheduled because the calculated delay is negative")
        return
    }

    // Convert delayMillis to the scheduled date and time - This is for troubleshooting the notification
    val futureInstant = Instant.ofEpochMilli(currentTimeMillis + delayMillis)
    val futureDateTime = LocalDateTime.ofInstant(futureInstant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formattedDateTime = futureDateTime.format(formatter)

    Log.d("CalendarScreen", "Notification is scheduled to be sent at: $formattedDateTime")

    // Create a tag for the notification work request
    val notificationTag = "period_reminder_notification"

    // Get the WorkManager instance
    val workManager = WorkManager.getInstance(context)

    // Cancel existing work requests with the same tag
    workManager.cancelAllWorkByTag(notificationTag)

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No network required
        .build()

    // Create a work request to send the notification
    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setConstraints(constraints)
        .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
        .addTag(notificationTag) // Tag the work request
        .build()

    // Enqueue the work request
    workManager.enqueue(workRequest)
    Log.d("CalendarScreen", "Work request enqueued with delay: $delayMillis")
}

fun String.capitalized(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else it.toString()
    }
}