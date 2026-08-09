package com.mensinator.app

import com.mensinator.app.business.ICalculationsHelper
import com.mensinator.app.business.IPeriodDatabaseHelper
import com.mensinator.app.business.IPeriodPrediction
import com.mensinator.app.business.PeriodPrediction
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


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
    fun getPredictedPeriodDate_onlyOnePeriodEntered_null() = runTest {
        coEvery { dbHelper.getPeriodCount() } returns 1
        periodPrediction = PeriodPrediction(dbHelper, calcHelper)
        assertEquals(null, periodPrediction.getPredictedPeriodDate())
    }
}