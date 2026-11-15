package com.mensinator.app.statistics

import com.mensinator.app.business.PeriodSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class PeriodHistoryTest {

    private val today = LocalDate.of(2026, 10, 31)

    @Test
    fun `ongoing period is excluded from past rows`() {
        val periods = listOf(
            PeriodSummary(
                periodId = 2,
                startDate = LocalDate.of(2026, 10, 30),
                endDate = LocalDate.of(2026, 11, 3),
                nextStartDate = null,
            ),
        )

        assertEquals(emptyList<PeriodHistoryRow>(), periods.toCompletedPeriodRows(today))
    }

    @Test
    fun `future period is excluded from past rows`() {
        val periods = listOf(
            PeriodSummary(
                periodId = 3,
                startDate = LocalDate.of(2026, 11, 20),
                endDate = LocalDate.of(2026, 11, 24),
                nextStartDate = null,
            ),
        )

        assertEquals(emptyList<PeriodHistoryRow>(), periods.toCompletedPeriodRows(today))
    }

    @Test
    fun `completed periods are sorted newest first and use the next start for cycle length`() {
        val periods = listOf(
            PeriodSummary(
                periodId = 1,
                startDate = LocalDate.of(2026, 9, 30),
                endDate = LocalDate.of(2026, 10, 4),
                nextStartDate = LocalDate.of(2026, 10, 30),
            ),
            PeriodSummary(
                periodId = 2,
                startDate = LocalDate.of(2026, 10, 30),
                endDate = LocalDate.of(2026, 11, 3),
                nextStartDate = null,
            ),
            PeriodSummary(
                periodId = 0,
                startDate = LocalDate.of(2026, 8, 31),
                endDate = LocalDate.of(2026, 9, 4),
                nextStartDate = LocalDate.of(2026, 9, 30),
            ),
        )

        val rows = periods.toCompletedPeriodRows(today)

        assertEquals(
            listOf(
                PeriodHistoryRow(LocalDate.of(2026, 9, 30), 30),
                PeriodHistoryRow(LocalDate.of(2026, 8, 31), 30),
            ),
            rows,
        )
    }

    @Test
    fun `most recent completed period without a next start has no cycle length`() {
        val periods = listOf(
            PeriodSummary(
                periodId = 1,
                startDate = LocalDate.of(2026, 9, 30),
                endDate = LocalDate.of(2026, 10, 4),
                nextStartDate = null,
            ),
        )

        val rows = periods.toCompletedPeriodRows(today)

        assertEquals(1, rows.size)
        assertNull(rows.first().cycleLengthDays)
    }

    @Test
    fun `prediction starts with the engine date and uses the rounded average`() {
        val firstPrediction = LocalDate.of(2026, 10, 1)

        val predictions = predictedPeriodStarts(
            firstPredictedDate = firstPrediction,
            averageCycleLengthDays = 31.6,
        )

        assertEquals(PREDICTION_COUNT, predictions.size)
        assertEquals(firstPrediction, predictions.first())
        assertEquals(firstPrediction.plusDays(32), predictions[1])
        assertEquals(firstPrediction.plusDays(32 * 5), predictions.last())
    }

    @Test
    fun `prediction keeps an overdue first date`() {
        val overduePrediction = LocalDate.of(2026, 10, 1)

        val predictions = predictedPeriodStarts(
            firstPredictedDate = overduePrediction,
            averageCycleLengthDays = 30.0,
        )

        assertEquals(overduePrediction, predictions.first())
    }

    @Test
    fun `prediction is empty when the engine has no date or no usable average`() {
        assertEquals(
            emptyList<LocalDate>(),
            predictedPeriodStarts(null, 30.0),
        )
        assertEquals(
            emptyList<LocalDate>(),
            predictedPeriodStarts(LocalDate.of(2026, 10, 1), 0.0),
        )
        assertEquals(
            emptyList<LocalDate>(),
            predictedPeriodStarts(LocalDate.of(2026, 10, 1), 0.4),
        )
    }

    @Test
    fun `pagination uses six rows per page`() {
        val rows = (1..13).toList()

        assertEquals(3, pageCount(rows.size))
        assertEquals(listOf(1, 2, 3, 4, 5, 6), rows.pageFor(0))
        assertEquals(listOf(7, 8, 9, 10, 11, 12), rows.pageFor(1))
        assertEquals(listOf(13), rows.pageFor(2))
        assertEquals(emptyList<Int>(), rows.pageFor(3))
        assertEquals(0, pageCount(0))
    }
}
