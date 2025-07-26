package com.mensinator.app.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.mensinator.app.MainActivity
import com.mensinator.app.R

@GlanceComposable
@Composable
fun WidgetContentWithoutLabel(text: String, label: String, showBackground: Boolean) {
    val cornerRadiusModifier = if (android.os.Build.VERSION.SDK_INT >= 31) {
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

    Box(
        modifier = cornerRadiusModifier
            .then(bgModifier)
            .appWidgetBackground()
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                modifier = GlanceModifier.padding(8.dp),
                style = TextStyle(
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    //fontSize = 18.sp,
                    textAlign = TextAlign.Center
                ),
            )
        }
        Box(
            modifier = GlanceModifier.padding(end = 8.dp, bottom = 8.dp).fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = label,
                style = TextStyle(
                    color = textColor,
                    fontSize = 12.sp,
                ),
            )
        }
    }
}