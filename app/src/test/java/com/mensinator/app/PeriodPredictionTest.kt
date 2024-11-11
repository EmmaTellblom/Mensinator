package com.mensinator.app

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate


class PeriodPredictionTest {

    @MockK(relaxed = true)
    private lateinit var dbHelper: IPeriodDatabaseHelper

    @MockK
    private lateinit var calcHelper: ICalculationsHelper

    private lateinit var periodPrediction: IPeriodPrediction

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun getPredictedPeriodDate_onlyOnePeriodEntered_fallbackDate() {
        every { dbHelper.getPeriodCount() } returns 1

        periodPrediction = PeriodPrediction(dbHelper, calcHelper)

        assertEquals(LocalDate.parse("1900-01-01"), periodPrediction.getPredictedPeriodDate())
    }
}