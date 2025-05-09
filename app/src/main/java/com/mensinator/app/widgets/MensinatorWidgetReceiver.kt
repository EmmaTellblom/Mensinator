package com.mensinator.app.widgets

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

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