package com.mensinator.app.widgets

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders

@Composable
fun MensinatorGlanceTheme(content: @Composable () -> Unit) {
    val material3LightColors = lightColorScheme()
    val material3DarkColors = darkColorScheme().copy(
        inverseOnSurface = Color.White
    )

    GlanceTheme(
        colors = ColorProviders(
            light = material3LightColors,
            dark = material3DarkColors
        )
    ) {
        content()
    }
}
