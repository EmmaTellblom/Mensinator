package com.mensinator.app.utils

import android.content.Context
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.layout.Box
import com.mensinator.app.business.CalculationsHelper
import com.mensinator.app.business.PeriodDatabaseHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.time.LocalDate
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.background
import androidx.glance.layout.Alignment
import com.mensinator.app.R

object MensinatorWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override val stateDefinition: GlanceStateDefinition<LocalDate>
        get() = object : GlanceStateDefinition<LocalDate> {
            override suspend fun getDataStore(
                context: Context,
                fileKey: String
            ): DataStore<LocalDate> {
                return PeriodDataStore(context)
            }

            override fun getLocation(context: Context, fileKey: String): File {
                throw NotImplementedError("Not implemented")
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

    @Composable
    private fun MyContent(value: LocalDate) {
        Box(GlanceModifier.background(ImageProvider(R.drawable.circle_widget)), contentAlignment = Alignment.Center) {
            Text(text = value.toString())
        }
    }
}

class PeriodDataStore(private val context: Context) : DataStore<LocalDate> {
    override val data: Flow<LocalDate>
        get() {
            val dbHelper = PeriodDatabaseHelper(context, DefaultDispatcherProvider())
            val calculationsHelper = CalculationsHelper(dbHelper = dbHelper)
            return flow { calculationsHelper.calculateNextPeriod()?.let { emit(it) } }
        }

    override suspend fun updateData(transform: suspend (LocalDate) -> LocalDate): LocalDate {
        throw NotImplementedError("Not implemented")
    }
}
