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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/*
This file creates the calendar. A sort of "main screen".
 */

@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val dbHelper = remember { PeriodDatabaseHelper(context) }
    val calcHelper = remember { Calculations(context) }
    val currentMonth = remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val selectedDates = remember { mutableStateOf(setOf<LocalDate>()) }
    val periodDates = remember { mutableStateOf(emptyMap<LocalDate, Int>()) }
    val symptomDates = remember { mutableStateOf(emptySet<LocalDate>()) }
    val ovulationDates = remember { mutableStateOf(emptySet<LocalDate>()) }
    var averageCycleLength by remember { mutableDoubleStateOf(0.0) }
    var averagePeriodLength by remember { mutableDoubleStateOf(0.0) }
    var periodCount by remember { mutableIntStateOf(0) }
    var ovulationCount by remember { mutableIntStateOf(0) }
    var showSymptomsDialog by remember { mutableStateOf(false) }
    var symptoms by remember { mutableStateOf(emptyList<Symptom>()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }  // Track the selected date for the SymptomsDialog
    var showCreateNewSymptomDialog by remember { mutableStateOf(false) }  // State to show the CreateNewSymptomDialog
    var showStatisticsDialog by remember { mutableStateOf(false) }  // State to show the StatisticsDialog
    var showFAQDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showExportImportDialog by remember { mutableStateOf(false) }
    var showManageSymptomsDialog by remember { mutableStateOf(false) }
    var lastOvulationDate by remember { mutableStateOf<LocalDate?>(null) }
    var follicleGrowthDays by remember { mutableStateOf("0") }

    var cycleNumber: Int
    val oldestPeriodDate = dbHelper.getOldestPeriodDate()

    // app_settings from the database
    val periodColorSetting = dbHelper.getSettingByKey("period_color")
    val selectedColorSetting = dbHelper.getSettingByKey("selected_color")
    val selectedPeriodColorSetting = dbHelper.getSettingByKey("period_selection_color")
    val nextPeriodColorSetting = dbHelper.getSettingByKey("expected_period_color")
    val ovulationColorSetting = dbHelper.getSettingByKey("ovulation_color")
    val nextOvulationColorSetting = dbHelper.getSettingByKey("expected_ovulation_color")

    val showCycleNumbersSetting = dbHelper.getSettingByKey("cycle_numbers_show")
    val showCycleNumbersSettingValue = showCycleNumbersSetting?.value?.toIntOrNull() ?: 1

    val periodHistoryNumber = dbHelper.getSettingByKey("period_history")
    val periodHistoryNumberValue = periodHistoryNumber?.value?.toIntOrNull() ?: 5


    val reminderDays = dbHelper.getSettingByKey("reminder_days")?.value?.toIntOrNull() ?: 2

    // Variables used for predicting/calculating luteal, period and ovulation dates
    val lutealPeriodCalculation = dbHelper.getSettingByKey("luteal_period_calculation")?.value?.toIntOrNull() ?: 0

    // Variables which will contain predicted dates for period and ovulation
    var nextPeriodStartCalculated by remember { mutableStateOf("Not enough data") }
    var nextOvulationCalculated by remember { mutableStateOf("Not enough data") }

    // The first date of previous period
    var firstLastPeriodDate by remember { mutableStateOf<LocalDate?>(null) }

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

    // Colors for days in calendar
    val periodColor = periodColorSetting?.value?.let { colorMap[it] } ?: colorMap["Red"]!!
    val selectedColor = selectedColorSetting?.value?.let { colorMap[it] } ?: colorMap["LightGray"]!!
    val nextPeriodColor = nextPeriodColorSetting?.value?.let { colorMap[it] } ?: colorMap["Yellow"]!!
    val selectedPeriodColor = selectedPeriodColorSetting?.value?.let { colorMap[it] } ?: colorMap["DarkGray"]!!
    val ovulationColor = ovulationColorSetting?.value?.let { colorMap[it] } ?: colorMap["Blue"]!!
    val nextOvulationColor = nextOvulationColorSetting?.value?.let { colorMap[it] } ?: colorMap["Magenta"]!!
    var symptomColor: Color




    // Fetch symptoms from the database
    LaunchedEffect(Unit) {
        symptoms = dbHelper.getAllActiveSymptoms()
    }

    // Function to update statistics. Previously statistics were on main screen.
    // We should probably move this ?
    // TODO: We should remove this and let the statistics be calculated in statisticsdialog?
    fun updateStatistics() {
        val allDates = dbHelper.getAllPeriodDates()
        val dates = allDates.keys.sorted()
        val periodLengths = mutableListOf<Long>()

        if (dates.isNotEmpty()) {
            var currentPeriodId = allDates[dates.first()]
            var periodStartDate = dates.first()
            var periodEndDate: LocalDate? = null

            //Calculate average cycle length for last X periods (set in settings)
            // TODO REMOVE periodHistoryNumberValue
            val listPeriodDates = dbHelper.getLatestXPeriodStart(periodHistoryNumberValue)
            val cycleLengths = mutableListOf<Long>()
            for (i in 0 until listPeriodDates.size - 1) {
                val cycleLength = ChronoUnit.DAYS.between(listPeriodDates[i], listPeriodDates[i + 1])
                cycleLengths.add(cycleLength)
            }
            // Calculate the average cycle length
            averageCycleLength = cycleLengths.average()

            //Calculate average days for period
            for (i in dates.indices) {
                val date = dates[i]
                val periodId = allDates[date] ?: continue

                if (periodId == currentPeriodId) {
                    periodEndDate = date
                } else {
                    if (periodEndDate != null) {
                        periodLengths.add(periodEndDate.toEpochDay() - periodStartDate.toEpochDay() + 1)
                    }
                    periodStartDate = date
                    currentPeriodId = periodId
                    periodEndDate = date
                }

            }

            if (periodEndDate != null) {
                periodLengths.add(periodEndDate.toEpochDay() - periodStartDate.toEpochDay() + 1)
            }

            averagePeriodLength = if (periodLengths.isNotEmpty()) periodLengths.average() else 0.0
            // Calculate the next period, input is setting on how to calculate next period
            nextPeriodStartCalculated = calcHelper.calculateNextPeriod(lutealPeriodCalculation)
            //Log.d("CalendarScreen", "Next period start: $nextPeriodStartCalculated")

        } else {
            averageCycleLength = 0.0
            averagePeriodLength = 0.0
            nextPeriodStartCalculated = "Not enough data"
        }

        periodCount = dbHelper.getPeriodCount()
        ovulationCount = dbHelper.getOvulationCount()

        // Ovulation statistics
        val ovulationDatesList = dbHelper.getAllOvulationDates().sorted()
        lastOvulationDate = ovulationDatesList.lastOrNull()
        ovulationCount = ovulationDatesList.size

        // Predict the next ovulation date
        //Make sure there is at least one period and one ovulation date
        //Make sure the last ovulation date is before the first last period date
        if (lastOvulationDate != null && periodCount >= 1 && (lastOvulationDate.toString()<firstLastPeriodDate.toString())) {
            // Calculate the expected ovulation date using the number of cycles from the settings
            follicleGrowthDays = calcHelper.averageFollicalGrowthInDays()
            nextOvulationCalculated = firstLastPeriodDate?.plusDays(follicleGrowthDays.toLong()).toString()
            //Log.d("CalendarScreen", "Growth days in statistics 1: $follicleGrowthDays")
            //Log.d("CalendarScreen", "Next predicted ovulation: $nextPredictedOvulation")

        } else { //If Ovulation is after first last period date and prediction exists for Ovulation and Period
            if(lastOvulationDate.toString()>firstLastPeriodDate.toString() && (nextOvulationCalculated != "Not enough data" && nextPeriodStartCalculated != "Not enough data")){
                //Log.d("CalendarScreen", "Last ovulationdate: " + lastOvulationDate.toString() + " FirstLastPeriodDate: " + firstLastPeriodDate.toString())
                //Log.d("CalendarScreen", "Will calculate according to next expected period")
                follicleGrowthDays = calcHelper.averageFollicalGrowthInDays()
                //Log.d("CalendarScreen", "Growth days in statistics 2: $growthDays")
                if(nextPeriodStartCalculated!="Not enough data"){
                    nextOvulationCalculated = LocalDate.parse(nextPeriodStartCalculated).plusDays(follicleGrowthDays.toLong()).toString()
                }
                //Log.d("CalendarScreen", "Next predicted ovulation: $nextPredictedOvulation")
            }
            else{
                //val test = "Last ovulationdate: " + lastOvulationDate.toString() + " FirstLastPeriodDate: " + firstLastPeriodDate.toString()
                //Log.d("CalendarScreen", "Here is test: $test")
                nextOvulationCalculated = "Not enough data"
            }

        }

    }


    // Function to refresh symptom dates
    fun refreshSymptomDates() {
        val year = currentMonth.value.year
        val month = currentMonth.value.monthValue
        symptomDates.value = dbHelper.getSymptomDatesForMonth(year, month)
    }
    fun refreshOvulationDates() {
        val year = currentMonth.value.year
        val month = currentMonth.value.monthValue
        ovulationDates.value = dbHelper.getOvulationDatesForMonth(year, month).toSet()
//        val dates = dbHelper.getOvulationDatesForMonth(year, month).sorted()  // Rename to `dates`
//        ovulationDates.value = dates.toSet()
//        updateStatistics()
    }

    // Update button state based on selected dates
    val isSymptomsButtonEnabled by remember { derivedStateOf { selectedDates.value.isNotEmpty() } }
    val isOvulationButtonEnabled by remember { derivedStateOf { selectedDates.value.size == 1 } }
    val isPeriodsButtonEnabled by remember { derivedStateOf { selectedDates.value.isNotEmpty() } }


    // Here is where the calendar is generated
    LaunchedEffect(currentMonth.value) {
        val year = currentMonth.value.year
        val month = currentMonth.value.monthValue
        periodDates.value = dbHelper.getPeriodDatesForMonth(year, month)
        symptomDates.value = dbHelper.getSymptomDatesForMonth(year, month)
        ovulationDates.value = dbHelper.getOvulationDatesForMonth(year, month)
        //Log.d("CalendarScreen", "Symptom dates for $year-$month: ${symptomDates.value}")
        updateStatistics()
    }

    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()
    )
    {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                currentMonth.value = currentMonth.value.minusMonths(1)
            }) {
                Text(text = "Previous")
            }
            Text(
                text = "${currentMonth.value.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${currentMonth.value.year}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                currentMonth.value = currentMonth.value.plusMonths(1)
            }) {
                Text(text = "Next")
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
                        val hasExistingDate = dayDate in periodDates.value
                        val hasSymptomDate = dayDate in symptomDates.value
                        val hasOvulationDate = dayDate in ovulationDates.value

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
                        ) { // Colors for 'special' dates
                            val backgroundColor = when {
                                hasExistingDate && isSelected -> selectedPeriodColor
                                hasExistingDate -> periodColor
                                hasOvulationDate && isSelected -> selectedPeriodColor
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
                                Log.d("CalendarScreen", "Symptom colors for $dayDate: $noSymptomsForDay")

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
                                        //.padding(16.dp)
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
                            // If date is a period date
                            if (dayDate.toString().trim() == nextPeriodStartCalculated.trim() && !hasExistingDate) {
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
                            // If date is predicted ovulation date (and not an ovulation by user)
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
                    if(oldestPeriodDate != null && showCycleNumbersSettingValue == 1) {
                        if (dayDate >= oldestPeriodDate && dayDate <= LocalDate.now()) {
                            firstLastPeriodDate = dbHelper.getFirstPreviousPeriodDate(dayDate)
                            if (firstLastPeriodDate != null) {
                                // Calculate the number of days between the firstLastPeriodDate and dayDate
                                cycleNumber = ChronoUnit.DAYS.between(firstLastPeriodDate, dayDate)
                                    .toInt() + 1
                                //Log.d("CalendarScreen", "CycleNumber for $dayDate: $cycleNumber")

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

        Button(
            onClick = {
                if (selectedDates.value.isEmpty()) {
                    Toast.makeText(context, "No dates to save or remove", Toast.LENGTH_SHORT).show()
                } else {
                    for (date in selectedDates.value) {
                        if (date in periodDates.value) {
                            dbHelper.removeDateFromPeriod(date)
                            //Log.d("CalendarScreen", "Removed date $date")
                        } else {
                            val periodId = dbHelper.newFindOrCreatePeriodID(date)
                            dbHelper.addDateToPeriod(date, periodId)
                            //Log.d("CalendarScreen", "Added date $date with periodId $periodId")
                        }
                    }

                    selectedDates.value = setOf()

                    val year = currentMonth.value.year
                    val month = currentMonth.value.monthValue
                    periodDates.value = dbHelper.getPeriodDatesForMonth(year, month)
                    //Log.d("CalendarScreen", "Refreshed period dates for $year-$month")


                    // Calculate the first day of the next month
                    val firstDayOfNextMonth = if (month == 12) {
                        LocalDate.of(year + 1, 1, 1) // January 1st of next year
                    } else {
                        LocalDate.of(year, month + 1, 1) // First of the next month in the same year
                    }
                    // Recalculate the first or last period date using the first day of the next month
                    firstLastPeriodDate = dbHelper.getFirstPreviousPeriodDate(firstDayOfNextMonth)

                    updateStatistics()
                    // Schedule notification for reminder
                    if(reminderDays>0 && nextPeriodStartCalculated != "Not enough data" && nextPeriodStartCalculated>=LocalDate.now().toString()){
                        sendNotification(context, reminderDays, LocalDate.parse(nextPeriodStartCalculated))
                    }
                    Toast.makeText(context, "Changes saved successfully", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isPeriodsButtonEnabled,  // Set the state of the Periods button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = "Period")
        }

        Button(
            onClick = {
                selectedDate = selectedDates.value.firstOrNull()  // Select the first date as the date for the SymptomsDialog
                if (selectedDate != null) {
                    showSymptomsDialog = true
                } else {
                    Toast.makeText(context, "No dates selected", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isSymptomsButtonEnabled,  // Set the state of the Symptoms button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = "Symptoms")
        }

        //ovulation starts here
        Button(
            onClick = {
                if(selectedDates.value.size > 1){
                    Toast.makeText(context, "Only one day can be ovulation!", Toast.LENGTH_SHORT).show()
                }
                else if(selectedDates.value.size == 1){
                    val date = selectedDates.value.first()
                    dbHelper.updateOvulationDate(date)
                    refreshOvulationDates()

                    Toast.makeText(context, "Ovulation saved successfully", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(context, "No date selected for ovulation", Toast.LENGTH_SHORT).show()
                }
                selectedDates.value = emptySet()
                updateStatistics()

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
            Text(text = "Ovulation")
        }
        //ovulation ends here

        // Show the SymptomsDialog
        if (showSymptomsDialog && selectedDate != null) {
            val activeSymptoms = dbHelper.getAllActiveSymptoms()
            SymptomsDialog(
                date = selectedDate!!,  // Pass the selected date to the SymptomsDialog
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
        //TODO: We should remove this and let the statistics be calculated in the statistics file
        //TODO: using functions from the database helper
        if (showStatisticsDialog) {
            StatisticsDialog(
                nextPeriodStart = nextPeriodStartCalculated, // This needs to stay in CalendarScreen due to being marked in Calendar
                follicleGrowthDays = follicleGrowthDays, // TODO REMOVE!
                nextPredictedOvulation = nextOvulationCalculated, // This needs to stay in CalendarScreen due to being marked in Calendar
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

        if(showManageSymptomsDialog){
            ManageSymptom(
                onDismissRequest = { showManageSymptomsDialog = false }
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

fun sendNotification(context: Context, daysForReminding: Int, periodDate: LocalDate) {
    val reminderDate = periodDate.plusDays(daysForReminding.toLong())
    val delayMillis = reminderDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()

    Log.d("CalendarScreen", "Reminder time: $delayMillis")

    // Create a tag for the notification work request
    val notificationTag = "period_reminder_notification"

    // Get the WorkManager instance
    val workManager = WorkManager.getInstance(context)

    // Cancel existing work requests with the same tag
    workManager.cancelAllWorkByTag(notificationTag)

    // Create a work request to send the notification
    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
        .addTag(notificationTag) // Tag the work request
        .build()

    // Enqueue the work request
    workManager.enqueue(workRequest)
    Log.d("CalendarScreen", "Work request enqueued with delay: $delayMillis")
}