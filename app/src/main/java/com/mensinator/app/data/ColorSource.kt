package com.mensinator.app.data

import androidx.compose.ui.graphics.Color

object ColorSource {

    fun getColorMap(isDarkTheme: Boolean): Map<String, Color> {
        return if (isDarkTheme) darkColorMap else lightColorMap
    }

    fun getColor(isDarkTheme: Boolean, colorName: String): Color {
        return when (isDarkTheme) {
            true -> darkColorMap[colorName] ?: Color.Red
            false -> lightColorMap[colorName] ?: Color.Red
        }
    }

    val colorsGroupedByHue = listOf(
        listOf("LightRed", "Red", "DarkRed"),       // Red shades
        listOf("LightOrange", "Orange", "DarkOrange"), // Orange shades
        listOf("LightYellow", "Yellow", "DarkYellow"), // Yellow shades
        listOf("LightGreen", "Green", "DarkGreen"), // Green shades
        listOf("LightCyan", "Cyan", "DarkCyan"),    // Cyan shades
        listOf("LightBlue", "Blue", "DarkBlue"),    // Blue shades
        listOf("LightMagenta", "Magenta", "DarkMagenta"), // Magenta shades
        listOf("White", "LightGray", "DarkGray")    // Gray and Black shades
    )

    private val lightColorMap = mapOf(
        "LightRed" to Color(0xFFF9D3D3),
        "Red" to Color(0xFFF2A6A6),
        "DarkRed" to Color(0xFFEC7C7B),

        "LightGreen" to Color(0xFFE0F9D3),
        "Green" to Color(0xFFC0F2A6),
        "DarkGreen" to Color(0xFFA2E87D),

        "LightBlue" to Color(0xFFA6BBF2),
        "Blue" to Color(0xFFA6BBF2),
        "DarkBlue" to Color(0xFF7999EC),

        "LightYellow" to Color(0xFFFAF7D1),
        "Yellow" to Color(0xFFF5EFA3),
        "DarkYellow" to Color(0xFFF0E775),

        "LightCyan" to Color(0xFFD2EDF9),
        "Cyan" to Color(0xFFA6DAF2),
        "DarkCyan" to Color(0xFF79C8EC),

        "LightMagenta" to Color(0xFFE8D6F5),
        "Magenta" to Color(0xFFD1ACEA),
        "DarkMagenta" to Color(0xFFBA8CD9),

        "LightOrange" to Color(0xFFF9E5D3),
        "Orange" to Color(0xFFF2CBA6),
        "DarkOrange" to Color(0xFFF0B175),

        "Black" to Color(0xFF212121),
        "DarkGray" to Color(0xFFABABAB),
        "LightGray" to Color(0xFFDFDDDD),
    )

    private val darkColorMap = mapOf(
        "LightRed" to Color(0xFFA97070),
        "Red" to Color(0xFFA15E5E),
        "DarkRed" to Color(0xFF793B3B),

        "LightGreen" to Color(0xFF78946B),
        "Green" to Color(0xFF668E53),
        "DarkGreen" to Color(0xFF446336),

        "LightBlue" to Color(0xFF7582A3),
        "Blue" to Color(0xFF5E71A1),
        "DarkBlue" to Color(0xFF364263),

        "LightYellow" to Color(0xFFAF9C6A),
        "Yellow" to Color(0xFFB3974D),
        "DarkYellow" to Color(0xFF6B5A2E),

        "LightCyan" to Color(0xFF79929E),
        "Cyan" to Color(0xFF5E8CA1),
        "DarkCyan" to Color(0xFF365563),

        "LightMagenta" to Color(0xFF9175A3),
        "Magenta" to Color(0xFF75568A),
        "DarkMagenta" to Color(0xFF513663),

        "LightOrange" to Color(0xFFAF8B6A),
        "Orange" to Color(0xFFB37E4D),
        "DarkOrange" to Color(0xFF6B4B2E),

        "White" to Color(0xFFF5F5F5),
        "DarkGray" to Color(0xFF585858),
        "LightGray" to Color(0xFF8F8F8F)
    )
}
