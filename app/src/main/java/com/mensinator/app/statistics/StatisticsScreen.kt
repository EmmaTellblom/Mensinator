package com.mensinator.app.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mensinator.app.R
import com.mensinator.app.ui.navigation.displayCutoutExcludingStatusBarsPadding
import com.mensinator.app.ui.theme.MensinatorTheme
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val historyDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = koinViewModel(),
) {
    val state = viewModel.viewState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    StatisticsScreenContent(modifier, state)
}

@Composable
private fun StatisticsScreenContent(
    modifier: Modifier = Modifier,
    state: StatisticsViewModel.ViewState
) {
    var periodsExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedHistoryTab by rememberSaveable { mutableStateOf(PeriodHistoryTab.Past) }
    var historyPage by rememberSaveable { mutableIntStateOf(0) }

    val totalPages = pageCount(state.completedPeriods.size)
    LaunchedEffect(totalPages) {
        historyPage = when {
            totalPages == 0 -> 0
            historyPage >= totalPages -> totalPages - 1
            else -> historyPage
        }
    }
    val safeHistoryPage = if (totalPages == 0) {
        0
    } else {
        historyPage.coerceIn(0, totalPages - 1)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .displayCutoutExcludingStatusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        PeriodsTrackedRow(
            count = state.trackedPeriods,
            expanded = periodsExpanded,
            onToggle = {
                periodsExpanded = !periodsExpanded
                if (!periodsExpanded) {
                    historyPage = 0
                }
            }
        )

        AnimatedVisibility(
            visible = periodsExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            PeriodHistorySection(
                completedPeriods = state.completedPeriods,
                predictedPeriods = state.predictedPeriods,
                selectedTab = selectedHistoryTab,
                onTabSelected = { selectedHistoryTab = it },
                page = safeHistoryPage,
                onPageChange = { historyPage = it },
            )
        }

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
    }
}

@Composable
private fun PeriodsTrackedRow(
    count: String?,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onToggle)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .rotate(if (expanded) 0f else -90f)
        )
        Text(
            text = stringResource(id = R.string.period_count),
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp, end = 8.dp)
        )
        count?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End
            )
        }
    }
}

private enum class PeriodHistoryTab {
    Past,
    Prediction,
}

@Composable
private fun PeriodHistorySection(
    completedPeriods: List<PeriodHistoryRow>,
    predictedPeriods: List<LocalDate>,
    selectedTab: PeriodHistoryTab,
    onTabSelected: (PeriodHistoryTab) -> Unit,
    page: Int,
    onPageChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
        ) {
            Tab(
                selected = selectedTab == PeriodHistoryTab.Past,
                onClick = { onTabSelected(PeriodHistoryTab.Past) },
                text = { Text(stringResource(id = R.string.period_history_past)) },
            )
            Tab(
                selected = selectedTab == PeriodHistoryTab.Prediction,
                onClick = { onTabSelected(PeriodHistoryTab.Prediction) },
                text = { Text(stringResource(id = R.string.period_history_prediction)) },
            )
        }

        when (selectedTab) {
            PeriodHistoryTab.Past -> PastPeriodsTable(
                rows = completedPeriods,
                page = page,
                onPageChange = onPageChange,
            )

            PeriodHistoryTab.Prediction -> PredictionPeriodsTable(
                rows = predictedPeriods,
            )
        }
    }
}

@Composable
private fun PastPeriodsTable(
    rows: List<PeriodHistoryRow>,
    page: Int,
    onPageChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HistoryTableHeader(showCycleLength = true)
        HorizontalDivider()

        if (rows.isEmpty()) {
            EmptyHistoryMessage(stringResource(id = R.string.period_history_no_completed_periods))
        } else {
            val pageRows = rows.pageFor(page)
            pageRows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = row.startDate.format(historyDateFormatter),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = row.cycleLengthDays?.toString() ?: "—",
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                if (index != pageRows.lastIndex) {
                    HorizontalDivider()
                }
            }

            val totalPages = pageCount(rows.size)
            if (totalPages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { onPageChange(page - 1) },
                        enabled = page > 0,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(id = R.string.period_history_older_periods),
                        )
                    }
                    Text(
                        text = stringResource(
                            id = R.string.period_history_page_indicator,
                            page + 1,
                            totalPages,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    IconButton(
                        onClick = { onPageChange(page + 1) },
                        enabled = page < totalPages - 1,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(id = R.string.period_history_newer_periods),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictionPeriodsTable(rows: List<LocalDate>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HistoryTableHeader(showCycleLength = false)
        HorizontalDivider()

        if (rows.isEmpty()) {
            EmptyHistoryMessage(stringResource(id = R.string.period_history_not_enough_data))
        } else {
            rows.forEachIndexed { index, date ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = date.format(historyDateFormatter),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                if (index != rows.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun HistoryTableHeader(showCycleLength: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.period_history_start_date),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
        )
        if (showCycleLength) {
            Text(
                text = stringResource(id = R.string.period_history_cycle_length_days),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun EmptyHistoryMessage(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
private fun PeriodHistoryPastPreview() {
    MensinatorTheme {
        PeriodHistorySection(
            completedPeriods = persistentListOf(
                PeriodHistoryRow(LocalDate.of(2026, 9, 30), 30),
                PeriodHistoryRow(LocalDate.of(2026, 8, 31), 30),
                PeriodHistoryRow(LocalDate.of(2026, 7, 23), 39),
                PeriodHistoryRow(LocalDate.of(2026, 6, 25), 28),
                PeriodHistoryRow(LocalDate.of(2026, 5, 22), 34),
                PeriodHistoryRow(LocalDate.of(2026, 4, 18), 30),
                PeriodHistoryRow(LocalDate.of(2026, 3, 19), 30),
            ),
            predictedPeriods = emptyList(),
            selectedTab = PeriodHistoryTab.Past,
            onTabSelected = {},
            page = 0,
            onPageChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PeriodHistoryPredictionPreview() {
    MensinatorTheme {
        PeriodHistorySection(
            completedPeriods = emptyList(),
            predictedPeriods = persistentListOf(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 12, 3),
                LocalDate.of(2027, 1, 4),
                LocalDate.of(2027, 2, 5),
                LocalDate.of(2027, 3, 9),
                LocalDate.of(2027, 4, 10),
            ),
            selectedTab = PeriodHistoryTab.Prediction,
            onTabSelected = {},
            page = 0,
            onPageChange = {},
        )
    }
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
                averageLutealLength = "15.0 days",
                completedPeriods = persistentListOf(
                    PeriodHistoryRow(LocalDate.of(2026, 9, 30), 30),
                    PeriodHistoryRow(LocalDate.of(2026, 8, 31), 30),
                    PeriodHistoryRow(LocalDate.of(2026, 7, 23), 39),
                ),
                predictedPeriods = persistentListOf(
                    LocalDate.of(2026, 11, 1),
                    LocalDate.of(2026, 12, 3),
                ),
            )
        )
    }
}
