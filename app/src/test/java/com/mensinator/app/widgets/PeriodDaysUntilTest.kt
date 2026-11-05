package com.mensinator.app.widgets

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class PeriodDaysUntilTest {

    @Test
    fun `returns total days across a year boundary`() {
        assertEquals(
            89,
            daysUntilPeriod(LocalDate.of(2026, 11, 5), LocalDate.of(2027, 2, 2))
        )
    }

    @Test
    fun `returns total days for dates in a leap year`() {
        assertEquals(
            2,
            daysUntilPeriod(LocalDate.of(2028, 2, 28), LocalDate.of(2028, 3, 1))
        )
    }

    @Test
    fun `returns zero when the period is today`() {
        assertEquals(
            0,
            daysUntilPeriod(LocalDate.of(2027, 6, 15), LocalDate.of(2027, 6, 15))
        )
    }
}
