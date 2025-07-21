package com.mensinator.app.widgets

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.glance.state.GlanceStateDefinition
import com.mensinator.app.business.ICalculationsHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.time.LocalDate


class WidgetDataStore() : DataStore<WidgetData>, KoinComponent {

    companion object {
        val stateDefinition = object : GlanceStateDefinition<WidgetData> {
            override suspend fun getDataStore(
                context: Context,
                fileKey: String
            ): DataStore<WidgetData> {
                return WidgetDataStore()
            }

            override fun getLocation(context: Context, fileKey: String): File {
                throw NotImplementedError("Not implemented yet")
            }
        }
    }

    val calculationsHelper: ICalculationsHelper = get()

    override val data: Flow<WidgetData> = flowOf(
        WidgetData(
            daysUntilPeriodWithoutText = formatDaysUntilPeriod(
                calculationsHelper.calculateNextPeriod(),
                NextPeriodFormat.OnlyDays
            ),
            daysUntilPeriodWithText = formatDaysUntilPeriod(
                calculationsHelper.calculateNextPeriod(),
                NextPeriodFormat.MediumLengthText
            ),
            nextPeriod = calculationsHelper.calculateNextPeriod()
        )
    )

    override suspend fun updateData(transform: suspend (WidgetData) -> WidgetData): WidgetData {
        throw NotImplementedError("Not implemented yet")
    }

    sealed interface NextPeriodFormat {
        data object OnlyDays : NextPeriodFormat
        data object MediumLengthText : NextPeriodFormat
    }

    private fun formatDaysUntilPeriod(
        date: LocalDate?,
        format: NextPeriodFormat
    ): String {
        val daysUntilNextPeriod = LocalDate.now().until(date).days
        return when (format) {
            NextPeriodFormat.OnlyDays -> {
                if (date == null) {
                    "?"
                } else {
                    "$daysUntilNextPeriod"
                }
            }
            NextPeriodFormat.MediumLengthText -> {
                if (date == null) {
                    "Unknown"
                } else {
                    "Period in $daysUntilNextPeriod days"
                }
            }
        }
    }
}