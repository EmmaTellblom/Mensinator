package com.mensinator.app.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val historyDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@Composable
fun PeriodHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = koinViewModel(),
) {
    val state = viewModel.viewState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    PeriodHistoryContent(modifier, state)
}

@Composable
private fun PeriodHistoryContent(
    modifier: Modifier = Modifier,
    state: StatisticsViewModel.ViewState,
    initialTab: PeriodHistoryTab = PeriodHistoryTab.Past,
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
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

    LaunchedEffect(state.showPredictionTable) {
        if (!state.showPredictionTable) {
            selectedTab = PeriodHistoryTab.Past
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .displayCutoutExcludingStatusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        if (state.showPredictionTable) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
            ) {
                Tab(
                    selected = selectedTab == PeriodHistoryTab.Past,
                    onClick = { selectedTab = PeriodHistoryTab.Past },
                    text = { Text(stringResource(id = R.string.period_history_past)) },
                )
                Tab(
                    selected = selectedTab == PeriodHistoryTab.Prediction,
                    onClick = { selectedTab = PeriodHistoryTab.Prediction },
                    text = { Text(stringResource(id = R.string.period_history_prediction)) },
                )
            }

            when (selectedTab) {
                PeriodHistoryTab.Past -> PastPeriodsTable(
                    rows = state.completedPeriods,
                    page = safeHistoryPage,
                    onPageChange = { historyPage = it },
                )

                PeriodHistoryTab.Prediction -> PredictionPeriodsTable(
                    rows = state.predictedPeriods,
                )
            }
        } else {
            PastPeriodsTable(
                rows = state.completedPeriods,
                page = safeHistoryPage,
                onPageChange = { historyPage = it },
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

private enum class PeriodHistoryTab {
    Past,
    Prediction,
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

@Preview(showBackground = true)
@Composable
private fun PeriodHistoryPastPreview() {
    MensinatorTheme {
        PeriodHistoryContent(
            state = StatisticsViewModel.ViewState(
                showPredictionTable = true,
                completedPeriods = persistentListOf(
                    PeriodHistoryRow(LocalDate.of(2026, 9, 30), 30),
                    PeriodHistoryRow(LocalDate.of(2026, 8, 31), 30),
                    PeriodHistoryRow(LocalDate.of(2026, 7, 23), 39),
                    PeriodHistoryRow(LocalDate.of(2026, 6, 25), 28),
                    PeriodHistoryRow(LocalDate.of(2026, 5, 22), 34),
                    PeriodHistoryRow(LocalDate.of(2026, 4, 18), 30),
                    PeriodHistoryRow(LocalDate.of(2026, 3, 19), 30),
                ),
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PeriodHistoryWithoutPredictionPreview() {
    MensinatorTheme {
        PeriodHistoryContent(
            state = StatisticsViewModel.ViewState(
                showPredictionTable = false,
                completedPeriods = persistentListOf(
                    PeriodHistoryRow(LocalDate.of(2026, 9, 30), 30),
                    PeriodHistoryRow(LocalDate.of(2026, 8, 31), 30),
                ),
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PeriodHistoryPredictionPreview() {
    MensinatorTheme {
        PeriodHistoryContent(
            state = StatisticsViewModel.ViewState(
                showPredictionTable = true,
                predictedPeriods = persistentListOf(
                    LocalDate.of(2026, 11, 1),
                    LocalDate.of(2026, 12, 3),
                    LocalDate.of(2027, 1, 4),
                    LocalDate.of(2027, 2, 5),
                    LocalDate.of(2027, 3, 9),
                    LocalDate.of(2027, 4, 10),
                ),
            ),
            initialTab = PeriodHistoryTab.Prediction,
        )
    }
}
