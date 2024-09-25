package com.mensinator.app

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDate


@Composable
fun StatisticsDialog(
) {
    val context = LocalContext.current
    val dbHelper = remember { PeriodDatabaseHelper(context) }
    val calcHelper = remember { Calculations(context) }
    val averageCycleLength = calcHelper.averageCycleLength()
    val periodCount = dbHelper.getPeriodCount()
    val ovulationCount = dbHelper.getOvulationCount()
    val averagePeriodLength = calcHelper.averagePeriodLength()
    val avgLutealLength = calcHelper.averageLutealLength()
    val nextPeriodStart = GlobalState.nextPeriodStartCalculated
    val nextPredictedOvulation = GlobalState.nextOvulationCalculated
    val follicleGrowthDays = calcHelper.averageFollicalGrowthInDays()

    val ovulationPrediction = OvulationPrediction(context)
    var ovulationPredictionDate = ovulationPrediction.getPredictedOvulationDate()

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
        RowOfText(
            stringResource(id = R.string.period_count),
            periodCount.toString()
        )

        RowOfText(
            stringResource(id = R.string.average_cycle_length),
            (Math.round(averageCycleLength * 10) / 10.0).toString() + " " + stringResource(id = R.string.days)
        )

        RowOfText(
            stringResource(id = R.string.average_period_length),
            (Math.round(averagePeriodLength * 10) / 10.0).toString() + " " + stringResource(id = R.string.days)
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
            follicleGrowthDays.toString()
        )

        RowOfText(
            stringResource(id = R.string.next_predicted_ovulation),
            //nextPredictedOvulation
            ovulationPredictionDate.toString()
        )

        RowOfText(
            stringResource(id = R.string.average_luteal_length),
            (Math.round(avgLutealLength * 10) / 10.0).toString() + " " + stringResource(id = R.string.days)
        )

    }
}

@Composable
fun RowOfText(stringOne: String, stringTwo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp, start = 5.dp, end = 5.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = stringOne,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringTwo,
            fontSize = 17.sp,
        )
    }
}
