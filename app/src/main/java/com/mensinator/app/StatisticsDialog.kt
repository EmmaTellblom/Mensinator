package com.mensinator.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate



@Composable
fun StatisticsDialog(
    nextPeriodStart: String, // This actually needs to be calculated in CalendarScreen due to the calendar
    follicleGrowthDays: String,
    nextPredictedOvulation: String?, // This actually needs to be calculated in CalendarScreen due to the calendar
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val dbHelper = remember { PeriodDatabaseHelper(context) }
    val calcHelper = remember { Calculations(context) }
    val averageCycleLength = calcHelper.averageCycleLength()
    val periodCount = dbHelper.getPeriodCount()
    val ovulationCount = dbHelper.getOvulationCount()
    val averagePeriodLength = calcHelper.averagePeriodLength()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Statistics")
        },
        text = {
            Column {
                Text(
                    text = "Number of periods tracked: $periodCount",
                    fontSize = 16.sp
                )
                Text(
                    text = "Average cycle length: ${"%.1f".format(averageCycleLength)} days",
                    fontSize = 16.sp
                )
                Text(
                    text = "Average period length: ${"%.1f".format(averagePeriodLength)} days",
                    fontSize = 16.sp
                )
                Text(
                    text = if (nextPeriodStart < LocalDate.now().toString()) {
                        "Next assumed period: $nextPeriodStart \n\nAssumed period date has passed!"
                    } else {
                        "Next assumed period: $nextPeriodStart"
                    },
                    fontSize = 16.sp
                )
                // Ovulation statistics
                Text(
                    text = "\nNumber of ovulation tracked: $ovulationCount",
                    fontSize = 16.sp
                )
                Text(
                    text = "Average ovulation day: $follicleGrowthDays",
                    fontSize = 16.sp
                )
                Text(
                    text = nextPredictedOvulation?.let {
                        "Next predicted ovulation date: $it"
                    } ?: "Not enough data to predict next ovulation",
                    fontSize = 16.sp
                )
//                val avgLutealLength = dbHelper.getAverageLutealLength()
//                Text(
//                    text = "Average luteal phase length: $avgLutealLength", //TODO
//
//                    fontSize = 16.sp
//                )
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text("Close")
            }
        },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}
