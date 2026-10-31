package com.mensinator.app.statistics

import com.mensinator.app.business.PeriodSummary
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

const val PERIOD_HISTORY_PAGE_SIZE = 6
const val PREDICTION_COUNT = 6

data class PeriodHistoryRow(
    val startDate: LocalDate,
    val cycleLengthDays: Int?,
)

/**
 * Keeps only periods that have already ended and converts each one to the values shown in
 * the Past table. The current (ongoing) period is intentionally excluded.
 */
fun List<PeriodSummary>.toCompletedPeriodRows(today: LocalDate): List<PeriodHistoryRow> =
    asSequence()
        .filter { it.endDate.isBefore(today) }
        .sortedByDescending { it.startDate }
        .map { period ->
            PeriodHistoryRow(
                startDate = period.startDate,
                cycleLengthDays = period.nextStartDate?.let { nextStart ->
                    ChronoUnit.DAYS.between(period.startDate, nextStart).toInt()
                },
            )
        }
        .toList()

/**
 * Builds the list of predicted period start dates. The first date comes from the existing
 * prediction logic; the following dates are spaced by the rounded average cycle length.
 */
fun predictedPeriodStarts(
    firstPredictedDate: LocalDate?,
    averageCycleLengthDays: Double,
    count: Int = PREDICTION_COUNT,
): List<LocalDate> {
    if (firstPredictedDate == null || count <= 0) return emptyList()
    if (!averageCycleLengthDays.isFinite() || averageCycleLengthDays <= 0.0) return emptyList()

    val cycleLengthDays = averageCycleLengthDays.roundToInt()
    if (cycleLengthDays < 1) return emptyList()

    return List(count) { index ->
        firstPredictedDate.plusDays(cycleLengthDays.toLong() * index)
    }
}

fun <T> List<T>.pageFor(page: Int, pageSize: Int = PERIOD_HISTORY_PAGE_SIZE): List<T> {
    if (page < 0 || pageSize <= 0) return emptyList()

    val startIndex = page * pageSize
    if (startIndex >= size) return emptyList()

    return subList(startIndex, minOf(startIndex + pageSize, size))
}

fun pageCount(total: Int, pageSize: Int = PERIOD_HISTORY_PAGE_SIZE): Int {
    if (total <= 0 || pageSize <= 0) return 0
    return (total + pageSize - 1) / pageSize
}
