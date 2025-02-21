package com.mensinator.app.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Pick the text color with the best contrast against this background color.
fun Color.pickBestContrastTextColorForThisBackground(
    isDarkMode: Boolean,
    textColor1: Color,
    textColor2: Color
): Color {
    fun calculateContrastRatio(color1: Color, color2: Color): Double {
        val lum1 = color1.luminance()
        val lum2 = color2.luminance()

        val lighter = maxOf(lum1, lum2)
        val darker = minOf(lum1, lum2)

        return (lighter + 0.05) / (darker + 0.05) // +0.05 to avoid null division
    }

    val safeBackground = when {
        Color.Transparent == this && isDarkMode -> Color.Black
        Color.Transparent == this -> Color.White
        else -> this
    }

    val contrast1 = calculateContrastRatio(safeBackground, textColor1)
    val contrast2 = calculateContrastRatio(safeBackground, textColor2)

    return if (contrast1 >= contrast2) textColor1 else textColor2
}
