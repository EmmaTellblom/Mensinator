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
import java.util.Locale


/*
This file creates the calendar. A sort of "main screen".
 */

@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val dbHelper = remember { PeriodDatabaseHelper(context) }
    val currentMonth = remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val selectedDates = remember { mutableStateOf(setOf<LocalDate>()) }
    val periodDates = remember { mutableStateOf(emptyMap<LocalDate, Int>()) }
    val symptomDates = remember { mutableStateOf(emptySet<LocalDate>()) }
    var averageCycleLength by remember { mutableDoubleStateOf(0.0) }
    var averagePeriodLength by remember { mutableDoubleStateOf(0.0) }
    var nextPeriodStart by remember { mutableStateOf("Not enough data") }
    var periodCount by remember { mutableIntStateOf(0) }
    var showSymptomsDialog by remember { mutableStateOf(false) }
    var symptoms by remember { mutableStateOf(emptyList<Symptom>()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }  // Track the selected date for the SymptomsDialog
    var showCreateNewSymptomDialog by remember { mutableStateOf(false) }  // State to show the CreateNewSymptomDialog
    var showStatisticsDialog by remember { mutableStateOf(false) }  // State to show the StatisticsDialog
    var showFAQDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showExportImportDialog by remember { mutableStateOf(false) }

    val periodColorSetting = dbHelper.getSettingByKey("period_color")
    val selectedColorSetting = dbHelper.getSettingByKey("selected_color")
    val selectedPeriodColorSetting = dbHelper.getSettingByKey("period_selection_color")
    val symptomColorSetting = dbHelper.getSettingByKey("symptom_color")
    val nextPeriodColorSetting = dbHelper.getSettingByKey("expected_period_color")

    val periodColor = periodColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.Red
    val selectedColor = selectedColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.LightGray
    val symptomColor = symptomColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.Black
    val nextPeriodColor = nextPeriodColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.Yellow
    val selectedPeriodColor = selectedPeriodColorSetting?.value?.let { Color(it.toColorInt()) } ?: Color.DarkGray

    // Fetch symptoms from the database
    LaunchedEffect(Unit) {
        symptoms = dbHelper.getAllActiveSymptoms()
    }

    // Function to update statistics
    fun updateStatistics() {
        val allDates = dbHelper.getAllPeriodDates()
        val dates = allDates.keys.sorted()
        val periodLengths = mutableListOf<Long>()
        val cycleLengths = mutableListOf<Long>()

        if (dates.isNotEmpty()) {
            var currentPeriodId = allDates[dates.first()]
            var periodStartDate = dates.first()
            var periodEndDate: LocalDate? = null

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

                if (i > 0 && allDates[date] != allDates[dates[i - 1]]) {
                    val cycleLength = date.toEpochDay() - dates[i - 1].toEpochDay()
                    cycleLengths.add(cycleLength)
                }
            }

            if (periodEndDate != null) {
                periodLengths.add(periodEndDate.toEpochDay() - periodStartDate.toEpochDay() + 1)
            }

            averageCycleLength = if (cycleLengths.isNotEmpty()) cycleLengths.average() else 0.0
            averagePeriodLength = if (periodLengths.isNotEmpty()) periodLengths.average() else 0.0

            val lastPeriodDate = dates.last()
            nextPeriodStart = if (cycleLengths.isNotEmpty()) {
                lastPeriodDate.plusDays(averageCycleLength.toLong()).toString()
            } else {
                "Not enough data"
            }
        } else {
            averageCycleLength = 0.0
            averagePeriodLength = 0.0
            nextPeriodStart = "Not enough data"
        }

        periodCount = dbHelper.getPeriodCount()
    }

    // Function to refresh symptom dates
    fun refreshSymptomDates() {
        val year = currentMonth.value.year
        val month = currentMonth.value.monthValue
        symptomDates.value = dbHelper.getSymptomDatesForMonth(year, month)
        Log.d("CalendarScreen", "Symptom dates for $year-$month: ${symptomDates.value}")  // Added log for debugging
    }

    // Update button state based on selected dates
    val isSymptomsButtonEnabled by remember { derivedStateOf { selectedDates.value.isNotEmpty() } }

    LaunchedEffect(currentMonth.value) {
        val year = currentMonth.value.year
        val month = currentMonth.value.monthValue
        periodDates.value = dbHelper.getPeriodDatesForMonth(year, month)
        symptomDates.value = dbHelper.getSymptomDatesForMonth(year, month)
        Log.d("CalendarScreen", "Symptom dates for $year-$month: ${symptomDates.value}")
        updateStatistics()
    }

    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                currentMonth.value = currentMonth.value.minusMonths(1)
            }) {
                Text(text = "Previous")
            }
            Text(
                text = "${currentMonth.value.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.value.year}",
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
                    if (dayOfMonth > 0 && dayOfMonth <= daysInMonth) {
                        val dayDate = currentMonth.value.withDayOfMonth(dayOfMonth)
                        val isSelected = dayDate in selectedDates.value
                        val hasExistingDate = dayDate in periodDates.value
                        val hasSymptomDate = dayDate in symptomDates.value

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                                .clickable {
                                    if (isSelected) {
                                        selectedDates.value = selectedDates.value - dayDate
                                    } else {
                                        selectedDates.value = selectedDates.value + dayDate
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val backgroundColor = when {
                                hasExistingDate && isSelected -> selectedPeriodColor
                                hasExistingDate -> periodColor
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
                                        .size(8.dp)  // Size of the small bubble
                                        .background(symptomColor, CircleShape)  // Black bubble for dates with symptoms
                                        .align(Alignment.TopEnd)
                                )
                            }

                            if (dayDate.toString() == nextPeriodStart) {
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

                            // Mark today's date with a black border and bold font
                            if (dayDate == LocalDate.now()) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .border(2.dp, Color.Black, CircleShape)
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
                            val periodId = dbHelper.findOrCreatePeriodId(date)
                            dbHelper.addDateToPeriod(date, periodId)
                            Log.d("CalendarScreen", "Added date $date with periodId $periodId")
                        }
                    }

                    selectedDates.value = setOf()

                    val year = currentMonth.value.year
                    val month = currentMonth.value.monthValue
                    periodDates.value = dbHelper.getPeriodDatesForMonth(year, month)
                    Log.d("CalendarScreen", "Refreshed period dates for $year-$month")

                    updateStatistics()
                    Toast.makeText(context, "Changes saved successfully", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = "Add or remove dates")
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

        // Show the SymptomsDialog
        if (showSymptomsDialog && selectedDate != null) {
            SymptomsDialog(
                date = selectedDate!!,  // Pass the selected date to the SymptomsDialog
                symptoms = symptoms,
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
                nextPeriodStart = nextPeriodStart,
                periodCount = periodCount,
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

        Spacer(modifier = Modifier.weight(1f))
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
            }
        )
    }
}