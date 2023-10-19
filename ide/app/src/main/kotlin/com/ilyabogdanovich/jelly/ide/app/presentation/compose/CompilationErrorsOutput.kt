package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ilyabogdanovich.jelly.ide.app.domain.DeepLink
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.EditTextStyle
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.TitleText
import kotlin.math.abs

/**
 * Composable for presenting the compilation error messages.
 *
 * @author Ilya Bogdanovich on 16.10.2023
 */
@Composable
fun CompilationErrorsOutput(
    modifier: Modifier,
    errorMessages: List<CompilationResults.ErrorMessage>,
    onDeepLinkClicked: (DeepLink) -> Unit,
) {
    Column(modifier = modifier) {
        TitleText("Errors")
        CompilationErrorMessageList(errorMessages, onDeepLinkClicked)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CompilationErrorMessageList(
    errorMessages: List<CompilationResults.ErrorMessage>,
    onDeepLinkClicked: (DeepLink) -> Unit,
) {
    var pressPosition by remember { mutableStateOf(Offset.Zero) }
    SelectionContainer(Modifier.padding(10.dp).fillMaxWidth()) {
        LazyColumn {
            items(errorMessages) { errorMessage ->
                var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
                val annotatedString = buildAnnotatedString {
                    pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                    pushStringAnnotation(ANNOTATION_TAG, errorMessage.deepLink.buildString())
                    append(errorMessage.formattedMessage)
                    pop()
                    pop()
                }
                BasicText(
                    text = annotatedString,
                    // workaround for https://github.com/JetBrains/compose-multiplatform/issues/1450
                    // todo: when fixed, change to ClickableText and move to onClick parameter
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPointerEvent(PointerEventType.Press) {
                            val change = it.changes.first()
                            pressPosition = change.position
                        }
                        .onPointerEvent(PointerEventType.Release) {
                            val change = it.changes.first()
                            val delta = change.position - pressPosition
                            if (abs(delta.x) < MIN_CURSOR_MOVEMENT && abs(delta.y) < MIN_CURSOR_MOVEMENT) {
                                val offset = layout?.getOffsetForPosition(change.position) ?: 0
                                val deepLink = annotatedString
                                    .getStringAnnotations(ANNOTATION_TAG, offset, offset).firstOrNull()?.item
                                    ?.let { annotation -> DeepLink.parseString(annotation) }
                                if (deepLink != null) {
                                    onDeepLinkClicked(deepLink)
                                }
                            }
                        },
                    style = EditTextStyle(MaterialTheme.colors.error),
                    onTextLayout = { layout = it }
                )
            }
        }
    }
}

private const val ANNOTATION_TAG = "DeepLink"
private const val MIN_CURSOR_MOVEMENT = 5
