package com.mensinator.app.calendar

import com.mensinator.app.business.PeriodId
import java.time.LocalDate

/**
 * Expands a single selected day to a full period of [periodLength] consecutive days.
 *
 * The expansion only happens when exactly one day is selected and that day is not already
 * marked as a period day. Selecting an already marked day therefore still removes it, and
 * selecting several days leaves the selection untouched.
 */
internal fun expandPeriodSelection(
    selectedDays: Set<LocalDate>,
    currentPeriodDays: Map<LocalDate, PeriodId>,
    periodLength: Int,
): Set<LocalDate> {
    if (selectedDays.size != 1 || periodLength <= 1) return selectedDays

    val firstDay = selectedDays.first()
    if (firstDay in currentPeriodDays) return selectedDays

    return (0 until periodLength)
        .mapTo(mutableSetOf()) { firstDay.plusDays(it.toLong()) }
}
