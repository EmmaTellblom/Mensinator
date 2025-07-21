package com.mensinator.app.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mensinator.app.MainActivity
import com.mensinator.app.R

@GlanceComposable
@Composable
fun WidgetContent(text: String) {
    val context = LocalContext.current

    val cornerRadiusModifier = if (android.os.Build.VERSION.SDK_INT >= 31) {
        GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        GlanceModifier
    }
/*
    Box(
        cornerRadiusModifier
            .background(GlanceTheme.colors.widgetBackground)
            .appWidgetBackground()
            .clickable(actionStartActivity<MainActivity>())
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = GlanceModifier
                .padding(10.dp)
                .fillMaxWidth(),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            ),
        )

        Text(
            text = "P",
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 12.sp,
                textAlign = TextAlign.End
            ),
        )
    }
*/


    Box(
        modifier = GlanceModifier
            .background(ImageProvider(R.drawable.circle_widget))
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            )
        )
    }
}