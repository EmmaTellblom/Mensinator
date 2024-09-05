package com.mensinator.app.data

import androidx.compose.ui.graphics.Color

class DataSource(isDarkTheme: Boolean) {

    private val lightColorMap = mapOf(
        "Red" to Color(0xFFFB7979),
        "Green" to Color(0xFFACDF92),
        "Blue" to Color(0xFF8FA7E4),
        "Yellow" to Color(0xFFFFF29F),
        "Cyan" to Color(0xFF8ECCE9),
        "Magenta" to Color(0xFFCFB6E0),
        "Black" to Color(0xFF212121),
        "White" to Color(0xFFF5F5F5),
        "DarkGray" to Color(0xFFABABAB),
        "LightGray" to Color(0xFFDFDDDD),
        "Light Gray" to Color(0xFFDFDDDD)
    )

    private val darkColorMap = mapOf(
        "Red" to Color(0xFF995759), // Softer red
        "Green" to Color(0xFF6B8D5D), // Softer green
        "Blue" to Color(0xFF5F7097), // Softer blue
        "Yellow" to Color(0xFFAC965B), // Softer yellow
        "Cyan" to Color(0xFF6D96A8), // Softer cyan
        "Magenta" to Color(0xFF6F5F79), // Softer magenta
        "Black" to Color(0xFF212121), // Softer black (dark gray)
        "White" to Color(0xFFF5F5F5), // Softer white (light gray)
        "DarkGray" to Color(0xFF585858), // Softer dark gray
        "LightGray" to Color(0xFF8F8F8F) // Softer light gray
    )

    val colorMap: Map<String, Color> = if (isDarkTheme) darkColorMap else lightColorMap
}
