package com.mensinator.app.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mensinator.app.R
import com.mensinator.app.ui.navigation.displayCutoutExcludingStatusBarsPadding
import com.mensinator.app.ui.theme.MensinatorTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = koinViewModel(),
    onViewHistory: () -> Unit = {},
) {
    val state = viewModel.viewState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    StatisticsScreenContent(modifier, state, onViewHistory)
}

@Composable
private fun StatisticsScreenContent(
    modifier: Modifier = Modifier,
    state: StatisticsViewModel.ViewState,
    onViewHistory: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .displayCutoutExcludingStatusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        RowOfText(
            stringResource(id = R.string.period_count),
            state.trackedPeriods
        )
        RowOfText(
            stringResource(id = R.string.average_cycle_length),
            state.averageCycleLength
        )
        RowOfText(
            stringResource(id = R.string.average_period_length),
            state.averagePeriodLength
        )
        RowOfText(
            stringResource(id = R.string.next_period_start_future),
            state.periodPredictionDate
        )
        RowOfText(
            stringResource(id = R.string.ovulation_count),
            state.ovulationCount
        )
        RowOfText(
            stringResource(id = R.string.average_ovulation_day),
            state.follicleGrowthDays
        )
        RowOfText(
            stringResource(id = R.string.next_predicted_ovulation),
            state.ovulationPredictionDate
        )
        RowOfText(
            stringResource(id = R.string.average_luteal_length),
            state.averageLutealLength
        )
        ViewFullHistoryButton(
            onClick = onViewHistory,
            modifier = Modifier.padding(top = 16.dp),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ViewFullHistoryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(percent = 50),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        Text(
            text = stringResource(id = R.string.period_history_view_full).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
fun RowOfText(stringOne: String, stringTwo: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.SpaceAround
    ) {
        Text(
            text = stringOne,
            modifier = Modifier
                .weight(0.7f)
                .padding(end = 8.dp),
        )
        stringTwo?.let {
            Text(
                text = it,
                modifier = Modifier.alignByBaseline().widthIn(max = 200.dp),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RowOfTextPreview() {
    RowOfText("firstString", "secondstring")
}

@Preview(showBackground = true)
@Composable
private fun RowOfTextLongPreview() {
    RowOfText("Very long first string, we could even use lorem ipsum here", "secondstring")
}

@Preview(showBackground = true)
@Composable
private fun RowOfTextLongSecondPreview() {
    RowOfText("Short Text", "first string, we could even use lorem ipsum here")
}

@Preview(showBackground = true)
@Composable
private fun StatisticsScreenPreview() {
    MensinatorTheme {
        StatisticsScreenContent(
            state = StatisticsViewModel.ViewState(
                trackedPeriods = "3",
                averageCycleLength = "28.5 days",
                averagePeriodLength = "5.0 days",
                periodPredictionDate = "28 Feb 2024",
                ovulationCount = "4",
                ovulationPredictionDate = "20 Mar 2024",
                follicleGrowthDays = "14.0",
                averageLutealLength = "15.0 days"
            )
        )
    }
}
