package com.mensinator.app.data

import androidx.compose.ui.graphics.Color

class DataSource(isDarkTheme: Boolean) {

    private val lightColorMap = mapOf(
        "LightRed" to Color(0xFFF9D3D3),
        "LightGreen" to Color(0xFFE0F9D3),
        "LightBlue" to Color(0xFFA6BBF2),
        "LightYellow" to Color(0xFFFAF7D1),
        "LightCyan" to Color(0xFFD2EDF9),
        "LightMagenta" to Color(0xFFE8D6F5),
        "LightOrange" to Color(0xFFF9E5D3),

        "Red" to Color(0xFFF2A6A6),
        "Green" to Color(0xFFC0F2A6),
        "Blue" to Color(0xFFA6BBF2),
        "Yellow" to Color(0xFFF5EFA3),
        "Cyan" to Color(0xFFA6DAF2),
        "Magenta" to Color(0xFFD1ACEA),
        "Orange" to Color(0xFFF2CBA6),

        "Black" to Color(0xFF212121),
        "White" to Color(0xFFF5F5F5),
        "DarkGray" to Color(0xFFABABAB),
        "LightGray" to Color(0xFFDFDDDD),

        "DarkRed" to Color(0xFFEC7C7B),
        "DarkGreen" to Color(0xFFA2E87D),
        "DarkBlue" to Color(0xFF7999EC),
        "DarkYellow" to Color(0xFFF0E775),
        "DarkCyan" to Color(0xFF79C8EC),
        "DarkMagenta" to Color(0xFFBA8CD9),
        "DarkOrange" to Color(0xFFF0B175)
    )

    private val darkColorMap = mapOf(
        "LightRed" to Color(0xFFA97070),
        "LightGreen" to Color(0xFF78946B),
        "LightBlue" to Color(0xFF7582A3),
        "LightYellow" to Color(0xFFAF9C6A),
        "LightCyan" to Color(0xFF79929E),
        "LightMagenta" to Color(0xFF9175A3),
        "LightOrange" to Color(0xFFAF8B6A),

        "Red" to Color(0xFFA15E5E),
        "Green" to Color(0xFF668E53),
        "Blue" to Color(0xFF5E71A1),
        "Yellow" to Color(0xFFB3974D),
        "Magenta" to Color(0xFF75568A),
        "Orange" to Color(0xFFB37E4D),

        "DarkRed" to Color(0xFF793B3B),
        "DarkGreen" to Color(0xFF446336),
        "DarkBlue" to Color(0xFF364263),
        "DarkYellow" to Color(0xFF6B5A2E),
        "DarkCyan" to Color(0xFF365563),
        "DarkMagenta" to Color(0xFF513663),
        "DarkOrange" to Color(0xFF6B4B2E),

        "Black" to Color(0xFF212121),
        "White" to Color(0xFFF5F5F5),
        "DarkGray" to Color(0xFF585858),
        "LightGray" to Color(0xFF8F8F8F)
    )

    val colorMap: Map<String, Color> = if (isDarkTheme) darkColorMap else lightColorMap
}
