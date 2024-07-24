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
import androidx.core.graphics.toColorInt
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale


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

    var cycleNumber: Int
    val oldestPeriodDate = dbHelper.getOldestPeriodDate()

    val periodColorSetting = dbHelper.getSettingByKey("period_color")
    val selectedColorSetting = dbHelper.getSettingByKey("selected_color")
    val selectedPeriodColorSetting = dbHelper.getSettingByKey("period_selection_color")
    val symptomColorSetting = dbHelper.getSettingByKey("symptom_color")
    val nextPeriodColorSetting = dbHelper.getSettingByKey("expected_period_color")
    val ovulationColorSetting = dbHelper.getSettingByKey("ovulation_color")
    val nextOvulationColorSetting = dbHelper.getSettingByKey("expected_ovulation_color")

    val lutealPeriodCalculation = dbHelper.getSettingByKey("luteal_period_calculation")
    var nextPeriodStartCalculated by remember { mutableStateOf("Not enough data") }
    var nextPredictedOvulation by remember { mutableStateOf("Not enough data") }
    var firstLastPeriodDate by remember { mutableStateOf<LocalDate?>(null) }

    val periodColor = periodColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.Red
    val selectedColor = selectedColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.LightGray
    val symptomColor = symptomColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.Black
    val nextPeriodColor = nextPeriodColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.Yellow
    val selectedPeriodColor = selectedPeriodColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.DarkGray
    val ovulationColor = ovulationColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.Blue
    val nextOvulationColor = nextOvulationColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.Magenta
    val lutealPeriodCalculationValue = lutealPeriodCalculation?.value?.toIntOrNull() ?: 0
    //Log.d("CalendarScreen", "Luteal period calculation value: $lutealPeriodCalculationValue")

    // Fetch symptoms from the database
    LaunchedEffect(Unit) {
        symptoms = dbHelper.getAllActiveSymptoms()
    }

    // Function to update statistics
    fun updateStatistics() {
        val allDates = dbHelper.getAllPeriodDates()
        val dates = allDates.keys.sorted()
        val periodLengths = mutableListOf<Long>()

        if (dates.isNotEmpty()) {
            var currentPeriodId = allDates[dates.first()]
            var periodStartDate = dates.first()
            var periodEndDate: LocalDate? = null

            //Calculate average cycle length for last 5 periods
            val listPeriodDates = dbHelper.getLatestXPeriodStart(5)
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
            nextPeriodStartCalculated = calcHelper.calculateNextPeriod(lutealPeriodCalculationValue)
            Log.d("CalendarScreen", "Next period start: $nextPeriodStartCalculated")

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
        if (lastOvulationDate != null && periodCount >= 1 && (lastOvulationDate.toString()<firstLastPeriodDate.toString())) {
            val growthDays = calcHelper.averageFollicalGrowthInDays(5)
            nextPredictedOvulation = firstLastPeriodDate?.plusDays(growthDays.toLong()).toString()
            Log.d("CalendarScreen", "Next predicted ovulation: $nextPredictedOvulation")

        } else {
            if(lastOvulationDate.toString()>firstLastPeriodDate.toString() && (nextPredictedOvulation != "Not enough data" && nextPeriodStartCalculated != "Not enough data")){
                Log.d("CalendarScreen", "Last ovulationdate: " + lastOvulationDate.toString() + " FirstLastPeriodDate: " + firstLastPeriodDate.toString())
                Log.d("CalendarScreen", "Will calculate according to next expected period")
                val growthDays = calcHelper.averageFollicalGrowthInDays(5)
                nextPredictedOvulation = LocalDate.parse(nextPeriodStartCalculated).plusDays(growthDays.toLong()).toString()
                Log.d("CalendarScreen", "Next predicted ovulation: $nextPredictedOvulation")
            }
            else{
                val test = "Last ovulationdate: " + lastOvulationDate.toString() + " FirstLastPeriodDate: " + firstLastPeriodDate.toString()
                Log.d("CalendarScreen", "Here is test: $test")
                nextPredictedOvulation = "Not enough data"
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
                        ) {
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
                                Box(
                                    modifier = Modifier
                                        //.padding(16.dp)
                                        .offset(x = (-8).dp, y = (0).dp)
                                        .size(6.dp)  // Size of the small bubble
                                        .background(
                                            symptomColor,
                                            CircleShape
                                        )  // Black bubble for dates with symptoms
                                        .align(Alignment.TopEnd)
                                )
                            }

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

                            if (dayDate.toString() == nextPredictedOvulation && !hasOvulationDate) {

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
                            } else {
                                Text(
                                    text = dayOfMonth.toString(),
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .background(Color.Transparent)
                                )
                            }
                    if(oldestPeriodDate != null) {
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
                            Log.d("CalendarScreen", "Removed date $date")
                        } else {
                            val periodId = dbHelper.newFindOrCreatePeriodID(date)
                            dbHelper.addDateToPeriod(date, periodId)
                            Log.d("CalendarScreen", "Added date $date with periodId $periodId")
                        }
                    }

                    selectedDates.value = setOf()

                    val year = currentMonth.value.year
                    val month = currentMonth.value.monthValue
                    periodDates.value = dbHelper.getPeriodDatesForMonth(year, month)
                    Log.d("CalendarScreen", "Refreshed period dates for $year-$month")


                    // Calculate the first day of the next month
                    val firstDayOfNextMonth = if (month == 12) {
                        LocalDate.of(year + 1, 1, 1) // January 1st of next year
                    } else {
                        LocalDate.of(year, month + 1, 1) // First of the next month in the same year
                    }
                    // Recalculate the first or last period date using the first day of the next month
                    firstLastPeriodDate = dbHelper.getFirstPreviousPeriodDate(firstDayOfNextMonth)

                    updateStatistics()
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
        if (showStatisticsDialog) {
            StatisticsDialog(
                averageCycleLength = averageCycleLength,
                averagePeriodLength = averagePeriodLength,
                nextPeriodStart = nextPeriodStartCalculated,
                periodCount = periodCount,
                ovulationCount = ovulationCount,
                //averageOvulationCycleLength = averageOvulationCycleLength,
                nextPredictedOvulation = nextPredictedOvulation,
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

    }
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

