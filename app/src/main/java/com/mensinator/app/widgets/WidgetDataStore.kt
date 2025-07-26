package com.mensinator.app.widgets

import android.content.Context
import android.graphics.*
import androidx.core.graphics.createBitmap
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
            daysUntilPeriodBitmap = getBitmap(calculationsHelper.calculateNextPeriod()),
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

    private fun getBitmap(calculateNextPeriod: LocalDate?): Bitmap {
        val bitmap = createBitmap(150, 300)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        val arcSize = 130f
        canvas.drawArc(10f, 40f, 10f+arcSize, 40+arcSize, 135f, 270f, false, paint)
        canvas.drawText("7 days", 32f, 115f, Paint().apply {
            color = Color.BLACK
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        })

        canvas.drawText("until next period", 10f, 200f, Paint().apply {
            color = Color.BLACK
            textSize = 18f
        })

        return bitmap
    }

}