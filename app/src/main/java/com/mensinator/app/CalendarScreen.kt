package com.mensinator.app

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.mensinator.app.navigation.displayCutoutExcludingStatusBarsPadding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import com.kizitonwose.calendar.core.*
import com.mensinator.app.data.ColorSource
import com.mensinator.app.ui.theme.isDarkMode
import org.koin.compose.koinInject
import java.util.*
import java.time.format.TextStyle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

/*
This function is the initiator of the vertical calendar.
 */
@Composable
fun CalendarScreen(modifier: Modifier) {

    val context = LocalContext.current

    val dbHelper: IPeriodDatabaseHelper = koinInject()

    // Days selected in the calendar
    val selectedDates = remember { mutableStateOf(setOf<LocalDate>()) }

    val actualPeriodDates = remember { mutableStateOf(emptyMap<LocalDate, Int>()) }
    val actualOvulationDates = remember { mutableStateOf(emptySet<LocalDate>()) }
    val actualSymptomDates = remember { mutableStateOf(emptySet<LocalDate>()) }

    val currentMonth = remember { YearMonth.now() }
    val focusedYearMonth = remember { mutableStateOf(currentMonth) }

    fun refreshOvulationDates() {
        val year = focusedYearMonth.value.year
        val month = focusedYearMonth.value.monthValue
        actualOvulationDates.value = dbHelper.getOvulationDatesForMonth(year, month).toSet()
    }

    // Function to refresh symptom dates
    fun refreshSymptomDates() {
        val year = focusedYearMonth.value.year
        val month = focusedYearMonth.value.monthValue
        actualSymptomDates.value = dbHelper.getSymptomDatesForMonth(year, month)
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

            val currentMonth = remember { YearMonth.now() }
            val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
            val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
            val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY) // TODO: Store and fetch from database

            val state = rememberCalendarState(
                startMonth = startMonth,
                endMonth = endMonth,
                firstVisibleMonth = currentMonth,
                firstDayOfWeek = daysOfWeek.first()
            )

            LaunchedEffect(state.firstVisibleMonth) {
                focusedYearMonth.value = state.firstVisibleMonth.yearMonth
                // Load data for the new month
                actualPeriodDates.value =
                    dbHelper.getPeriodDatesForMonthNew(focusedYearMonth.value.year, focusedYearMonth.value.monthValue)
                actualOvulationDates.value =
                    dbHelper.getOvulationDatesForMonthNew(focusedYearMonth.value.year, focusedYearMonth.value.monthValue)
                actualSymptomDates.value =
                    dbHelper.getSymptomDatesForMonthNew(focusedYearMonth.value.year, focusedYearMonth.value.monthValue)
            }



            VerticalCalendar(
                state = state,
                dayContent = { day -> Day(day, selectedDates,
                    actualPeriodDates.value, actualOvulationDates.value, actualSymptomDates.value
                ) },
                monthHeader = {
                    MonthTitle(month = it.yearMonth)
                    DaysOfWeekTitle(daysOfWeek = daysOfWeek)
                }

            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val periodButtonEnabled by remember {
            derivedStateOf { selectedDates.value.isNotEmpty() }
        }
        Button(
            onClick = {
                //TODO!
            },
            enabled = periodButtonEnabled,  // Set the state of the Periods button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {

            Text(text = "Period")
        }
        var showSymptomsDialog by remember { mutableStateOf(false) }
        val symptomButtonEnabled by remember {
            derivedStateOf { selectedDates.value.isNotEmpty() }
        }

        Button(
            onClick = {
                //TODO!
                showSymptomsDialog = true
            },
            enabled = symptomButtonEnabled,  // Set the state of the Symptoms button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = "Symptoms")
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
                    selectedDates.value = setOf()

                    refreshOvulationDates()

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

            Text(text = "Ovulation")
        }

    }
}

// Helper function to display the days of the week
@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Spacer(modifier = Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                //fontSize = 14.sp,
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}

// Helper function to display the month title
@Composable
fun MonthTitle(month: YearMonth) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center,
            text = "${month.month.name} ${month.year}",
            style = MaterialTheme.typography.titleLarge, // Adjust text style as needed
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.primary,
            thickness = 2.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// Helper function to display a day
@Composable
fun Day(day: CalendarDay, selectedDates: MutableState<Set<LocalDate>>, actualPeriodDates: Map<LocalDate, Int>,
        actualOvulationDates: Set<LocalDate>, actualSymptomDates: Set<LocalDate>) {
    val colorMap = ColorSource.getColorMap(isDarkMode())
    val dbHelper: IPeriodDatabaseHelper = koinInject()
    val periodPrediction: IPeriodPrediction = koinInject()
    val ovulationPrediction: IOvulationPrediction = koinInject()

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
        day.date in actualPeriodDates.keys -> periodColor
        day.date in selectedDates.value -> selectedColor
        day.date.isEqual(periodPrediction.getPredictedPeriodDate()) -> nextPeriodColor
        day.date in actualOvulationDates -> ovulationColor
        day.date.isEqual(ovulationPrediction.getPredictedOvulationDate()) -> nextOvulationColor
        else -> Color.Transparent
    }

    val circleSize = when {
        day.date.isEqual(LocalDate.now()) -> 30.dp
        else -> 0.dp
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
                    selectedDates.value = if (isSelected) {
                        selectedDates.value - day.date
                    } else {
                        selectedDates.value + day.date
                    }
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            fontWeight = fontStyleType,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}