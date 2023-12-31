package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
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
    navigationEffect: Any,
    errorMarkup: ErrorMarkup,
    onSourceInputChanged: (TextFieldValue) -> Unit,
) {
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    val showSyntaxHelp = remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Box {
            TitleText("Input")
            IconButton(
                onClick = { showSyntaxHelp.value = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(6.dp)
                    .size(24.dp)
            ) {
                Icon(
                    Icons.Filled.QuestionMark,
                    contentDescription = "",
                )
            }
        }
        CodeEditTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .onSizeChanged { viewportSize = it },
            value = sourceInput,
            navigationEffect = navigationEffect,
            errorMarkup = errorMarkup,
            viewportSize = viewportSize,
            onValueChange = { onSourceInputChanged(it) },
        )
    }

    SyntaxHelpDialog(showSyntaxHelp)
}

@Composable
private fun CodeEditTextField(
    value: TextFieldValue,
    navigationEffect: Any,
    errorMarkup: ErrorMarkup,
    modifier: Modifier = Modifier,
    errorColor: Color = MaterialTheme.colors.error,
    highlightColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
    viewportSize: IntSize,
    onValueChange: (TextFieldValue) -> Unit = {},
) {
    val hScrollState = rememberScrollState()
    val vScrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var lineTops by remember { mutableStateOf<List<Float>>(listOf()) }
    val decorationOffset = remember { mutableStateOf(0f) }
    var highlightedLine by remember { mutableStateOf<Int?>(null) }

    fun updateHighlight(offset: Int) {
        layout?.let { l -> highlightedLine = l.getLineForOffset(offset) }
    }

    BasicTextField(
        modifier = modifier
            .verticalScroll(vScrollState)
            .horizontalScroll(hScrollState)
            .focusRequester(focusRequester)
            .padding(bottom = 2.dp)
            .drawBehind {
                layout?.let {
                    drawErrorMarkup(value.text, decorationOffset.value, it, errorMarkup, errorColor)
                    drawHighlight(it, size, highlightedLine, highlightColor)
                }
            },
        value = value,
        textStyle = EditTextStyle(),
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        onValueChange = {
            onValueChange(it)
            updateHighlight(it.selection.start)
        },
        onTextLayout = { l ->
            layout = l
            lineTops = List(l.lineCount) { l.getLineTop(it) }
            updateHighlight(value.selection.start)
        },
        decorationBox = { innerTextField -> DecorationBox(decorationOffset, innerTextField, lineTops, highlightedLine) }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(vScrollState.maxValue to value.selection) {
        layout?.let { l ->
            l.scrollHorizontally(value, hScrollState, viewportSize, decorationOffset.value)
            l.scrollVertically(value, vScrollState, viewportSize)
        }
    }

    LaunchedEffect(navigationEffect) {
        focusRequester.requestFocus()
        updateHighlight(value.selection.start)
    }
}

private suspend fun TextLayoutResult.scrollHorizontally(
    value: TextFieldValue,
    hScrollState: ScrollState,
    viewportSize: IntSize,
    decorationOffset: Float
) {
    val start = getHorizontalPosition(value.selection.start, usePrimaryDirection = true)
    val end = getHorizontalPosition(value.selection.end, usePrimaryDirection = true)
    if (end > hScrollState.value + viewportSize.width - 2 * decorationOffset) {
        hScrollState.scrollBy(end - hScrollState.value - viewportSize.width + 2 * decorationOffset)
    } else if (start < hScrollState.value) {
        hScrollState.scrollBy(start - hScrollState.value)
    }
}

private suspend fun TextLayoutResult.scrollVertically(
    value: TextFieldValue,
    vScrollState: ScrollState,
    viewportSize: IntSize,
) {
    val line = getLineForOffset(value.selection.end)
    val top = getLineTop(line)
    val bottom = getLineBottom(line)
    val lineHeight = bottom - top
    val viewportBottom = vScrollState.value + viewportSize.height
    if (bottom > viewportBottom - lineHeight) {
        vScrollState.animateScrollBy(lineHeight)
    } else if (top < vScrollState.value + lineHeight) {
        vScrollState.animateScrollBy(-lineHeight)
    }
}

private fun DrawScope.drawHighlight(layout: TextLayoutResult, size: Size, line: Int?, color: Color) {
    if (line != null) {
        val top = layout.getLineTop(line)
        val bottom = layout.getLineBottom(line)
        drawRect(color, Offset(0f, top), Size(size.width, bottom - top))
    }
}

private fun DrawScope.drawErrorMarkup(
    text: String,
    decorationOffset: Float,
    layout: TextLayoutResult,
    errorMarkup: ErrorMarkup,
    errorColor: Color
) {
    val dash = ERROR_MARKUP_DASH_INTERVAL.dp.toPx()
    val path = Path()
    errorMarkup.errors.forEach { markup ->
        if (markup.line < layout.lineCount && markup.start <= text.length && markup.stop <= text.length) {
            val left = decorationOffset + layout.getHorizontalPosition(markup.start, true)
            val right = decorationOffset + layout.getHorizontalPosition(markup.stop, true)
            val bottom = layout.getLineBottom(markup.line) + ERROR_MARKUP_OFFSET_Y.dp.toPx()
            path.moveTo(left, bottom)
            path.lineTo(if (left == right) left + ERROR_MARKUP_MIN_WIDTH.dp.toPx() else right, bottom)
        }
    }

    drawPath(
        path = path,
        errorColor,
        style = Stroke(
            width = ERROR_MARKUP_THICKNESS.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash), 0f)
        )
    )
}

@Composable
private fun DecorationBox(
    decorationOffset: MutableState<Float>,
    innerTextField: @Composable () -> Unit,
    lineTops: List<Float>,
    highlighted: Int?,
) {
    Row {
        if (lineTops.isNotEmpty()) {
            LineNumbers(decorationOffset, lineTops, highlighted)
        }
        innerTextField()
    }
}

@Composable
private fun LineNumbers(
    decorationOffset: MutableState<Float>,
    lineTops: List<Float>,
    highlighted: Int?,
) {
    val localDensity = LocalDensity.current
    Box(
        modifier = Modifier
            .onSizeChanged { decorationOffset.value = localDensity.run { it.width.toFloat() } }
            .padding(horizontal = 8.dp)
    ) {
        for (lineIndex in lineTops.indices) {
            val line = lineIndex + 1
            val top = lineTops[lineIndex]
            Text(
                modifier = Modifier
                    .offset(y = with(LocalDensity.current) { top.toDp() })
                    .align(Alignment.CenterEnd),
                text = line.toString(),
                style = EditTextStyle(
                    textColor = if (lineIndex == highlighted) {
                        MaterialTheme.colors.onSurface
                    } else {
                        MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    }
                ),
            )
        }
    }
}

// Error markup line thickness, in dp
private const val ERROR_MARKUP_THICKNESS = 2f
// Error markup line dash interval, in dp
private const val ERROR_MARKUP_DASH_INTERVAL = 3f
// Error markup vertical offset, in dp
private const val ERROR_MARKUP_OFFSET_Y = 1f
// Error highlight minimal length, in dp
private const val ERROR_MARKUP_MIN_WIDTH = 10f
