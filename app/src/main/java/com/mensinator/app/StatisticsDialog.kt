package com.mensinator.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDate


@Composable
fun StatisticsDialog(
    nextPeriodStart: String, // This actually needs to be calculated in CalendarScreen due to the calendar
    follicleGrowthDays: String,
    nextPredictedOvulation: String?, // This actually needs to be calculated in CalendarScreen due to the calendar
) {
    val context = LocalContext.current
    val dbHelper = remember { PeriodDatabaseHelper(context) }
    val calcHelper = remember { Calculations(context) }
    val averageCycleLength = calcHelper.averageCycleLength()
    val periodCount = dbHelper.getPeriodCount()
    val ovulationCount = dbHelper.getOvulationCount()
    val averagePeriodLength = calcHelper.averagePeriodLength()
    val avgLutealLength = calcHelper.averageLutealLength()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.statistics_title),
            fontSize = 25.sp,
            modifier = Modifier.padding(start = 5.dp, bottom = 5.dp),
            fontWeight = FontWeight.Bold
        )
        Column(
            modifier = Modifier
                //.padding(16.dp)  // Padding around the text content
                .fillMaxWidth()
        ) {
            RowOfText(
                stringResource(id = R.string.period_count),
                periodCount.toString() + " " + stringResource(id = R.string.days)
            )

            RowOfText(
                stringResource(id = R.string.average_cycle_length),
                averageCycleLength.toString() + " " + stringResource(id = R.string.days)
            )

            RowOfText(
                stringResource(id = R.string.average_period_length),
                averagePeriodLength.toString() + " " + stringResource(id = R.string.days)
            )

            RowOfText(
                if (nextPeriodStart < LocalDate.now().toString()) {
                    stringResource(id = R.string.next_period_start_past)
                } else {
                    stringResource(id = R.string.next_period_start_future)
                },
                nextPeriodStart
            )

            RowOfText(
                stringResource(id = R.string.ovulation_count),
                ovulationCount.toString()
            )

            RowOfText(
                stringResource(id = R.string.average_ovulation_day),
                follicleGrowthDays
            )

            nextPredictedOvulation?.let {
                RowOfText(
                    stringResource(id = R.string.next_predicted_ovulation),
                    it
                )
            }

            RowOfText(
                stringResource(id = R.string.average_luteal_length),
                avgLutealLength.toString()
            )
        }
    }
}

@Composable
fun RowOfText(stringOne: String, stringTwo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp, start = 5.dp, end = 5.dp, bottom = 7.dp)
    ) {
        Text(
            text = stringOne,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringTwo,
            fontSize = 17.sp
        )
    }
}
/*
confirmButton = {
    Button(onClick = onDismissRequest) {
        Text(stringResource(id = R.string.close_button))
    }
},
modifier = Modifier
//.padding(16.dp)
    .fillMaxWidth()
)
}
*/