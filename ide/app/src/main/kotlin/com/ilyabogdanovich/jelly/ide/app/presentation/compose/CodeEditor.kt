package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.ErrorMarkup
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.EditTextStyle
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.TitleText

/**
 * Composable for the code editor.
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
@Composable
fun CodeEditor(
    modifier: Modifier = Modifier,
    sourceInput: TextFieldValue,
    errorMarkup: ErrorMarkup,
    onSourceInputChanged: (TextFieldValue) -> Unit,
) {
    Column(modifier = modifier) {
        TitleText("Input")
        CodeEditTextField(
            modifier = Modifier
                .height(0.dp)
                .weight(1f)
                .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 2.dp),
            value = sourceInput,
            errorMarkup = errorMarkup,
            onValueChange = { onSourceInputChanged(it) },
        )
    }
}

@Composable
private fun CodeEditTextField(
    value: TextFieldValue,
    errorMarkup: ErrorMarkup,
    modifier: Modifier = Modifier,
    errorColor: Color = MaterialTheme.colors.error,
    onValueChange: (TextFieldValue) -> Unit = {},
) {
    val hScrollState = rememberScrollState()
    val vScrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    BasicTextField(
        modifier = modifier
            .verticalScroll(vScrollState)
            .horizontalScroll(hScrollState)
            .focusRequester(focusRequester)
            .drawBehind {
                layout?.let {
                    errorMarkup.errors.forEach { markup ->
                        val dash = ERROR_MARKUP_DASH_INTERVAL.dp.toPx()
                        drawPath(
                            path = Path().apply {
                                val left = it.getHorizontalPosition(markup.start, true)
                                val right = it.getHorizontalPosition(markup.stop, true)
                                moveTo(left, it.getLineBottom(markup.line) + ERROR_MARKUP_OFFSET_Y.dp.toPx())
                                lineTo(right, it.getLineBottom(markup.line) + ERROR_MARKUP_OFFSET_Y.dp.toPx())
                            },
                            errorColor,
                            style = Stroke(
                                width = ERROR_MARKUP_THICKNESS,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash), 0f)
                            )
                        )
                    }
                }
            },
        value = value,
        textStyle = EditTextStyle(),
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        onValueChange = onValueChange,
        onTextLayout = { layout = it }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

// Error markup line thickness, in dp
private const val ERROR_MARKUP_THICKNESS = 3f
// Error markup line dash interval, in dp
private const val ERROR_MARKUP_DASH_INTERVAL = 3f
// Error markup vertical offset, in dp
private const val ERROR_MARKUP_OFFSET_Y = 1f
