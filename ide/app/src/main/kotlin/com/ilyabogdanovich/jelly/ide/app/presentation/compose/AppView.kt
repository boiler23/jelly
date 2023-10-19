@file:Suppress("FunctionName")

package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ilyabogdanovich.jelly.ide.app.domain.DeepLink
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.ErrorMarkup
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.AppTheme

/**
 * Composables for the main application UI.
 *
 * @author Ilya Bogdanovich on 11.10.2023
 */
@Composable
@Suppress("LongParameterList")
fun App(
    splashScreenVisible: Boolean,
    sourceInput: TextFieldValue,
    errorMarkup: ErrorMarkup,
    resultOutput: String,
    navigationEffect: Any,
    errorMessages: List<CompilationResults.ErrorMessage>,
    compilationTimeOutput: String,
    compilationInProgress: Boolean,
    onSourceInputChanged: (TextFieldValue) -> Unit,
    onDeepLinkClicked: (DeepLink) -> Unit,
) {
    AppTheme {
        Surface {
            AnimatedVisibility(splashScreenVisible, enter = fadeIn(), exit = fadeOut()) {
                SplashScreen()
            }
            AnimatedVisibility(!splashScreenVisible, enter = fadeIn(), exit = fadeOut()) {
                AppView(
                    sourceInput = sourceInput,
                    errorMarkup = errorMarkup,
                    resultOutput = resultOutput,
                    navigationEffect = navigationEffect,
                    errorMessages = errorMessages,
                    compilationTimeOutput = compilationTimeOutput,
                    compilationInProgress = compilationInProgress,
                    onSourceInputChanged = onSourceInputChanged,
                    onDeepLinkClicked = onDeepLinkClicked,
                )
            }
        }
    }
}

@Composable
@Suppress("LongParameterList")
fun AppView(
    sourceInput: TextFieldValue,
    errorMarkup: ErrorMarkup,
    resultOutput: String,
    navigationEffect: Any,
    errorMessages: List<CompilationResults.ErrorMessage>,
    compilationTimeOutput: String,
    compilationInProgress: Boolean,
    onSourceInputChanged: (TextFieldValue) -> Unit,
    onDeepLinkClicked: (DeepLink) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CodeEditor(
            modifier = Modifier.weight(2f),
            sourceInput = sourceInput,
            navigationEffect = navigationEffect,
            errorMarkup = errorMarkup,
            onSourceInputChanged = onSourceInputChanged,
        )
        Row(modifier = Modifier.weight(1f)) {
            CompilationOutput(modifier = Modifier.weight(1f), content = resultOutput)
            Divider(modifier = Modifier.fillMaxHeight().width(2.dp))
            CompilationErrorsOutput(
                modifier = Modifier.weight(1f),
                errorMessages = errorMessages,
                onDeepLinkClicked = onDeepLinkClicked,
            )
        }
        CompilationStatus(compilationTimeOutput, compilationInProgress)
    }
}

@Composable
@Preview
fun App_Preview() {
    App(
        splashScreenVisible = false,
        sourceInput = TextFieldValue(
            """
                print "Hello, world!"
                out err
                print "sin^2(x)+cos^2(x)=1"
            """.trimIndent()
        ),
        errorMarkup = ErrorMarkup(
            listOf(
                ErrorMarkup.Underline(line = 1, start = 4, stop = 7),
                ErrorMarkup.Underline(line = 2, start = 1, stop = 10),
            )
        ),
        resultOutput = "Hello, world!",
        navigationEffect = Any(),
        errorMessages = listOf(
            CompilationResults.ErrorMessage(
                "line:2:5 Undefined variable 'err'.",
                DeepLink.Cursor(position = 5)
            )
        ),
        compilationTimeOutput = "Compiling...",
        compilationInProgress = true,
        onSourceInputChanged = {},
        onDeepLinkClicked = {},
    )
}
