package com.mensinator.app

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.mensinator.app.navigation.displayCutoutExcludingStatusBarsPadding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
//import com.kizitonwose.calendar.compose.*
import com.kizitonwose.calendar.core.*
import java.util.*
import java.time.format.TextStyle

/*
This file creates the calendar. A sort of "main screen".
 */
@Composable
fun CalendarScreen(modifier: Modifier) {
    //val context = LocalContext.current


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
            val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)

            val state = rememberCalendarState(
                startMonth = startMonth,
                endMonth = endMonth,
                firstVisibleMonth = currentMonth,
                firstDayOfWeek = daysOfWeek.first()
            )


            VerticalCalendar(
                state = state,
                dayContent = { Day(it) },
                monthHeader = {
                    DaysOfWeekTitle(daysOfWeek = daysOfWeek) // Use the title as month header
                }

            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

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
            },
            enabled = true,  // Set the state of the Symptoms button
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = "Symptoms")
        }


        Button(
            onClick = {

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
fun Day(day: CalendarDay) {
    Box(
        modifier = Modifier
            .aspectRatio(1f), // This is important for square sizing!
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.date.dayOfMonth.toString())
    }
}