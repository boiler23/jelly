package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.ReadOnlyEditTextField
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.TitleText
import kotlin.math.min

/**
 * Composable for presenting the compilation output.
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
@Composable
fun CompilationOutput(
    modifier: Modifier,
    content: String,
    textColor: Color = MaterialTheme.colors.onSurface,
) {
    Column(modifier = modifier) {
        TitleText("Output")
        ReadOnlyEditTextField(
            content.substring(0 until min(content.length, MAX_OUTPUT_LENGTH)),
            textColor = textColor,
            modifier = Modifier.padding(10.dp)
        )
    }
}

// we don't show more than 5000 characters to not impact the app performance.
// this should become configurable in the future.
private const val MAX_OUTPUT_LENGTH = 5000
