package com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * App theme definition.
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = MaterialTheme(
    colors = if (darkTheme) darkThemeColors else lightThemeColors,
    content = content,
)
