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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

/*
This file creates the calendar. A sort of "main screen".
 */
@Composable
fun CalendarScreen(modifier: Modifier) {

    val context = LocalContext.current

    val dbHelper: IPeriodDatabaseHelper = koinInject()
    //val refreshOvulationDates: () -> Unit = koinInject()

    // Days selected in the calendar
    val selectedDates = remember { mutableStateOf(setOf<LocalDate>()) }

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


            VerticalCalendar(
                state = state,
                dayContent = { day -> Day(day, selectedDates) },
                monthHeader = {
                    MonthTitle(month = it.yearMonth)
                    DaysOfWeekTitle(daysOfWeek = daysOfWeek)
                }

            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                //TODO!
            },
            enabled = true,  // Set the state of the Periods button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {

            Text(text = "Period")
        }

        Button(
            onClick = {
                //TODO!
            },
            enabled = true,  // Set the state of the Symptoms button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = "Symptoms")
        }

        //ovulation starts here
        val onlyOneOvulationAllowed = stringResource(id = R.string.only_day_alert)
        val successSavedOvulation = stringResource(id = R.string.success_saved_ovulation)
        val noDateSelectedOvulation = stringResource(id = R.string.no_date_selected_ovulation)

        Button(
            onClick = {
                if (selectedDates.value.size > 1) {
                    Toast.makeText(context, onlyOneOvulationAllowed, Toast.LENGTH_SHORT).show()
                } else if (selectedDates.value.size == 1) {
                    val date = selectedDates.value.first()
                    dbHelper.updateOvulationDate(date)
                    selectedDates.value = setOf()
                    //refreshOvulationDates() TODO: Does this need to be done?

                    Toast.makeText(context, successSavedOvulation, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, noDateSelectedOvulation, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = true,  // Set the state of the Ovulation button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {

            Text(text = "Ovulation")
        }

    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
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

@Composable
fun Day(day: CalendarDay, selectedDates: MutableState<Set<LocalDate>>) {
    val colorMap = ColorSource.getColorMap(isDarkMode())
    val dbHelper: IPeriodDatabaseHelper = koinInject()
    val periodPrediction: IPeriodPrediction = koinInject()
    val ovulationPrediction: IOvulationPrediction = koinInject()

    val circleSize = 30.dp

    //Colors
    val selectedColor = dbHelper.getSettingByKey("selection_color")?.value?.let { colorMap[it] }
        ?: colorMap["LightGray"]!!
    val nextPeriodColor =
        dbHelper.getSettingByKey("expected_period_color")?.value?.let { colorMap[it] }
            ?: colorMap["Yellow"]!!
    val nextOvulationColor =
        dbHelper.getSettingByKey("expected_ovulation_color")?.value?.let { colorMap[it] }
            ?: colorMap["Magenta"]!!

    //Dates to track
    val isSelected = day.date in selectedDates.value
    var nextPeriodDate = periodPrediction.getPredictedPeriodDate()
    var ovulationPredictionDate = ovulationPrediction.getPredictedOvulationDate()

    if (day.date.isEqual(LocalDate.now())) {
        val isSelected = day.date in selectedDates.value
        val selectedColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .size(circleSize)
                .border(1.dp, color = Color.LightGray, CircleShape)
                .background(
                    //if (isSelected) selectedColor else Color.Transparent,
                    if(day.date.isEqual(nextPeriodDate)) nextPeriodColor
                        else if(day.date.isEqual(ovulationPredictionDate)) nextOvulationColor
                        else if (isSelected) selectedColor
                        else Color.Transparent,
                    shape = CircleShape
                )
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
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }

    else {
        Box(
            modifier = Modifier
                .aspectRatio(1f) // This ensures the cells remain square.
                .background(
                    //if (isSelected) selectedColor else Color.Transparent,
                    if(day.date.isEqual(nextPeriodDate)) nextPeriodColor
                    else if(day.date.isEqual(ovulationPredictionDate)) nextOvulationColor
                    else if (isSelected) selectedColor
                    else Color.Transparent,
                    shape = MaterialTheme.shapes.small
                )
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
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}