package com.mensinator.app.widgets

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.mensinator.app.MainActivity
import com.mensinator.app.R

@GlanceComposable
@Composable
fun WidgetContentWithLabel(text: String, subText: String, showBackground: Boolean) {
    val cornerRadiusModifier = if (Build.VERSION.SDK_INT >= 31) {
        GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        GlanceModifier
    }

    val bgModifier = if (showBackground) {
        GlanceModifier.background(ImageProvider(R.drawable.widget_background))
    } else {
        GlanceModifier.background(GlanceTheme.colors.widgetBackground)
    }
    val textColor = if (showBackground) {
        GlanceTheme.colors.inverseOnSurface
    } else {
        GlanceTheme.colors.onSurface
    }

    // Scrollable LazyColumn in case the translations are long
    LazyColumn(
        modifier = cornerRadiusModifier
            .then(bgModifier)
            .appWidgetBackground()
            .clickable(actionStartActivity<MainActivity>())
            .fillMaxSize()
            .padding(8.dp),
    ) {
        item {
            Text(
                text = text,
                modifier = GlanceModifier.fillMaxWidth(),
                style = TextStyle(
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                ),
            )
        }
        item {
            Text(
                text = subText,
                style = TextStyle(
                    color = textColor,
                    //fontSize = 18.sp,
                    textAlign = TextAlign.Center
                ),
            )
        }

    }
}