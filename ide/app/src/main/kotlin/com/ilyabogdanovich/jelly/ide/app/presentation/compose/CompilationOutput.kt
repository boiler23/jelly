package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.rememberTextFieldScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.EditTextStyle
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.TitleText

/**
 * Composable for presenting the compilation output.
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
@Composable
fun CompilationOutput(
    content: String,
    modifier: Modifier,
    textColor: Color = MaterialTheme.colors.onSurface,
) {
    Column(modifier = modifier) {
        TitleText("Output")
        CompilationOutputTextField(
            content,
            textColor = textColor,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompilationOutputTextField(
    value: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colors.onSurface,
) {
    val vScrollState = rememberTextFieldScrollState(Orientation.Vertical)
    BasicTextField(
        modifier = modifier,
        value = value,
        textStyle = EditTextStyle(textColor),
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        onValueChange = {},
        readOnly = true,
        scrollState = vScrollState,
    )

    LaunchedEffect(value) {
        vScrollState.animateScrollBy(vScrollState.maxOffset)
    }
}
