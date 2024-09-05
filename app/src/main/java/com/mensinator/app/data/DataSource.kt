package com.mensinator.app.data

import androidx.compose.ui.graphics.Color

class DataSource {

    val colorMap = mapOf(
        "Red" to Color(0xFFFB7979),        // Softer red
        "Green" to Color(0xFFACDF92),      // Softer green
        "Blue" to Color(0xFF8FA7E4),       // Softer blue
        "Yellow" to Color(0xFFFFF29F),     // Softer yellow
        "Cyan" to Color(0xFF8ECCE9),       // Softer cyan
        "Magenta" to Color(0xFFCFB6E0),    // Softer magenta
        "Black" to Color(0xFF212121),      // Softer black (dark gray)
        "White" to Color(0xFFF5F5F5),      // Softer white (light gray)
        "DarkGray" to Color(0xFFABABAB),   // Softer dark gray
        "LightGray" to Color(0xFFDFDDDD),  // Softer light gray
        "Light Gray" to Color(0xFFDFDDDD)  // Softer light gray
    )

    val darkColorMap = mapOf(
        "Red" to Color(0x99FB7979),        // Softer red with 60% opacity
        "Green" to Color(0x99ACDF92),      // Softer green with 60% opacity
        "Blue" to Color(0x998FA7E4),       // Softer blue with 60% opacity
        "Yellow" to Color(0x99FFF29F),     // Softer yellow with 60% opacity
        "Cyan" to Color(0x998ECCE9),       // Softer cyan with 60% opacity
        "Magenta" to Color(0x99CFB6E0),    // Softer magenta with 60% opacity
        "Black" to Color(0x99212121),      // Softer black (dark gray) with 60% opacity
        "White" to Color(0x99F5F5F5),      // Softer white (light gray) with 60% opacity
        "DarkGray" to Color(0x99ABABAB),   // Softer dark gray with 60% opacity
        "LightGray" to Color(0x99DFDDDD),   // Softer light gray with 60% opacity
        "Light Gray" to Color(0x99DFDDDD)   // Softer light gray with 60% opacity
    )

}