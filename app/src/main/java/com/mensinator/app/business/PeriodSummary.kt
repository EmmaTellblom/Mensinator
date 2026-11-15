package com.mensinator.app.business

import java.time.LocalDate

/**
 * A single tracked period, derived from all dates that share the same period id.
 */
data class PeriodSummary(
    val periodId: PeriodId,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val nextStartDate: LocalDate?,
)
