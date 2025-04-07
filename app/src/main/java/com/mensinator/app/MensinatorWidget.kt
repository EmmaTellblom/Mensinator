package com.mensinator.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.mensinator.app.business.IPeriodPrediction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.time.LocalDate

sealed interface NextPeriodFormat {
    data object OnlyDays : NextPeriodFormat
    data object MediumLengthText : NextPeriodFormat
}

// TODO: Check what widgets to implement
// TODO: Check how to enable the user to select what text format should be used
// TODO: Handle widget taps
// TODO: Provide widget preview to system https://developer.android.com/develop/ui/views/appwidgets/enhance#updated-preview-with-glance
// TODO: Update widget when data changes https://developer.android.com/develop/ui/compose/glance/glance-app-widget#update-glanceappwidget
class MensinatorWidget(
    private val periodPrediction: IPeriodPrediction,
) : GlanceAppWidget() {

    companion object {
        private val SMALL_BOX = DpSize(90.dp, 90.dp)
        private val BIG_BOX = DpSize(180.dp, 180.dp)
        private val ROW = DpSize(180.dp, 48.dp)
        private val LARGE_ROW = DpSize(300.dp, 48.dp)
        private val COLUMN = DpSize(48.dp, 180.dp)
        private val LARGE_COLUMN = DpSize(48.dp, 300.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_BOX, BIG_BOX, ROW, LARGE_ROW, COLUMN, LARGE_COLUMN)
    )


    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val daysUntilNextPeriod = formatDaysUntilPeriod(
                periodPrediction.getPredictedPeriodDate(),
                format = NextPeriodFormat.OnlyDays
            )
            WidgetUI(daysUntilNextPeriod)
        }
    }

    private fun formatDaysUntilPeriod(
        date: LocalDate?,
        format: NextPeriodFormat
    ): String {
        val daysUntilNextPeriod = LocalDate.now().until(date).days
        return when (format) {
            NextPeriodFormat.MediumLengthText -> {
                if (date == null) {
                    "Unknown"
                } else {
                    "Period in $daysUntilNextPeriod days"
                }
            }
            NextPeriodFormat.OnlyDays -> {
                if (date == null) {
                    "?"
                } else {
                    "$daysUntilNextPeriod"
                }
            }
        }
    }
}

@Composable
@GlanceComposable
fun WidgetUI(daysUntilNextPeriod: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(10.dp)
            .clickable(
                actionStartActivity<MainActivity>()
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = daysUntilNextPeriod,
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 18.sp
            ),
        )
    }
}

class MensinatorWidgetReceiver : GlanceAppWidgetReceiver() {
    private val koin = GlobalContext.get()
    override val glanceAppWidget: MensinatorWidget = koin.get()

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        context?.let {
            CoroutineScope(Dispatchers.IO).launch {
                glanceAppWidget.updateAll(it)
            }
        }
    }
}