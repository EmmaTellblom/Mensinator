package com.mensinator.app.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mensinator.app.R
import com.mensinator.app.business.CalculationsHelper
import com.mensinator.app.business.PeriodDatabaseHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.time.LocalDate

object MensinatorWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override val stateDefinition: GlanceStateDefinition<String>
        get() = object : GlanceStateDefinition<String> {
            override suspend fun getDataStore(
                context: Context,
                fileKey: String
            ): DataStore<String> {
                return PeriodDataStore(context)
            }

            override fun getLocation(context: Context, fileKey: String): File {
                throw NotImplementedError("Not implemented yet")
            }
        }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long-running
        // operations.
        provideContent {
            MyContent(currentState())
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun MyContent(value: String) {
        Box(
            modifier = GlanceModifier
                .background(ImageProvider(R.drawable.circle_widget)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp
                )
            )
        }
    }
}

class PeriodDataStore(private val context: Context) : DataStore<String> {
    val dbHelper by lazy { PeriodDatabaseHelper(context, DefaultDispatcherProvider()) }

    override val data: Flow<String>
        get() {
            val calculationsHelper = CalculationsHelper(dbHelper = dbHelper)
            val remainingDays = getDaysUntil(nextDay = calculationsHelper.calculateNextPeriod())
            return flow { calculationsHelper.calculateNextPeriod()?.let { emit(remainingDays) } }
        }

    override suspend fun updateData(transform: suspend (String) -> String): String {
        throw NotImplementedError("Not implemented yet")
    }

    private fun getDaysUntil(nextDay: LocalDate?): String {
        return LocalDate.now().until(nextDay).days.toString()
    }
}
