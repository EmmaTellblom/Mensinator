package com.mensinator.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun StatisticsDialog(
    averageCycleLength: Double,
    averagePeriodLength: Double,
    nextPeriodStart: String,
    periodCount: Int,
    onDismissRequest: () -> Unit
) {
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