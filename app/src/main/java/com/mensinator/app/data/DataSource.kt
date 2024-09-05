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
        "Red" to Color(0x99FB7979),
        "Green" to Color(0x99ACDF92),
        "Blue" to Color(0x998FA7E4),
        "Yellow" to Color(0x99FFF29F),
        "Cyan" to Color(0x998ECCE9),
        "Magenta" to Color(0x99CFB6E0),
        "Black" to Color(0x99212121),
        "White" to Color(0x99F5F5F5),
        "DarkGray" to Color(0x99ABABAB),
        "LightGray" to Color(0x99DFDDDD),
        "Light Gray" to Color(0x99DFDDDD)
    )

    val colorMap: Map<String, Color> = if (isDarkTheme) darkColorMap else lightColorMap
}
