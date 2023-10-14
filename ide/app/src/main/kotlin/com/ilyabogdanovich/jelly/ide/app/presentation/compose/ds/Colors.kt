package com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * App theme colors definitions.
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
val colorPrimaryDark = Color(0xFFB0D7FF)
val colorBackgroundDark = Color(0xFF2D3142)
val colorBackgroundDarkAlternative = Color(0xFF191B24)

val colorPrimaryLight = Color(0xFF2D3142)
val colorBackgroundLight = Color(0xFFD8D5DB)
val colorBackgroundLightAlternative = Color(0xFFCCC8D0)

val darkThemeColors = darkColors(
    primary = colorPrimaryDark,
    background = colorBackgroundDark,
    surface = colorBackgroundDark,
)

val lightThemeColors = lightColors(
    primary = colorPrimaryLight,
    background = colorBackgroundLight,
    surface = colorBackgroundLight,
)

@Composable
fun ColorBackgroundAlternative(
    darkTheme: Boolean = isSystemInDarkTheme()
) = if (darkTheme) {
    colorBackgroundDarkAlternative
} else {
    colorBackgroundLightAlternative
}
