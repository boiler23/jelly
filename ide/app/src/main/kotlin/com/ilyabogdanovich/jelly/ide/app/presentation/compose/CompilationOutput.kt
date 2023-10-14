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

/**
 * Composable for presenting the compilation output (results or errors).
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
@Composable
fun CompilationOutput(
    modifier: Modifier,
    title: String,
    content: String,
    textColor: Color = MaterialTheme.colors.onSurface,
) {
    Column(modifier = modifier) {
        TitleText(title)
        ReadOnlyEditTextField(
            content,
            textColor = textColor,
            modifier = Modifier.padding(10.dp)
        )
    }
}
