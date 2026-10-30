package com.mensinator.app.calendar

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class PeriodSelectionTest {

    private val firstDay = LocalDate.of(2026, 2, 22)
    private val expectedPeriod = (0L until 5L)
        .map { firstDay.plusDays(it) }
        .toSet()

    @Test
    fun `single unmarked day is expanded to the configured period length`() {
        val result = expandPeriodSelection(
            selectedDays = setOf(firstDay),
            currentPeriodDays = emptyMap(),
            periodLength = 5,
        )

        assertEquals(expectedPeriod, result)
    }

    @Test
    fun `single day already marked as period is not expanded`() {
        val result = expandPeriodSelection(
            selectedDays = setOf(firstDay),
            currentPeriodDays = mapOf(firstDay to 1),
            periodLength = 5,
        )

        assertEquals(setOf(firstDay), result)
    }

    @Test
    fun `multiple selected days are not expanded`() {
        val selection = setOf(firstDay, firstDay.plusDays(1))

        val result = expandPeriodSelection(
            selectedDays = selection,
            currentPeriodDays = emptyMap(),
            periodLength = 5,
        )

        assertEquals(selection, result)
    }

    @Test
    fun `period length of one or less does not expand the selection`() {
        val resultForZero = expandPeriodSelection(
            selectedDays = setOf(firstDay),
            currentPeriodDays = emptyMap(),
            periodLength = 0,
        )
        val resultForOne = expandPeriodSelection(
            selectedDays = setOf(firstDay),
            currentPeriodDays = emptyMap(),
            periodLength = 1,
        )

        assertEquals(setOf(firstDay), resultForZero)
        assertEquals(setOf(firstDay), resultForOne)
    }

    @Test
    fun `already marked following days still produce the full period range`() {
        val currentPeriodDays = mapOf(
            firstDay.plusDays(2) to 2,
            firstDay.plusDays(3) to 2,
        )

        val result = expandPeriodSelection(
            selectedDays = setOf(firstDay),
            currentPeriodDays = currentPeriodDays,
            periodLength = 5,
        )

        assertEquals(expectedPeriod, result)
    }
}
