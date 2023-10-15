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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.ErrorMarkup
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.AppTheme

/**
 * Composables for the main application UI.
 *
 * @author Ilya Bogdanovich on 11.10.2023
 */
@Composable
fun App(
    splashScreenVisible: Boolean,
    sourceInput: TextFieldValue,
    errorMarkup: ErrorMarkup,
    resultOutput: String,
    errorOutput: String,
    compilationTimeOutput: String,
    compilationInProgress: Boolean,
    onSourceInputChanged: (TextFieldValue) -> Unit,
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
                    errorOutput = errorOutput,
                    compilationTimeOutput = compilationTimeOutput,
                    compilationInProgress = compilationInProgress,
                    onSourceInputChanged = onSourceInputChanged,
                )
            }
        }
    }
}

@Composable
fun AppView(
    sourceInput: TextFieldValue,
    errorMarkup: ErrorMarkup,
    resultOutput: String,
    errorOutput: String,
    compilationTimeOutput: String,
    compilationInProgress: Boolean,
    onSourceInputChanged: (TextFieldValue) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CodeEditor(
            modifier = Modifier.weight(2f),
            sourceInput = sourceInput,
            errorMarkup = errorMarkup,
            onSourceInputChanged = onSourceInputChanged,
        )
        Row(modifier = Modifier.weight(1f)) {
            CompilationOutput(
                modifier = Modifier.weight(1f),
                title = "Output",
                content = resultOutput,
            )
            Divider(modifier = Modifier.fillMaxHeight().width(2.dp))
            CompilationOutput(
                modifier = Modifier.weight(1f),
                title = "Errors",
                content = errorOutput,
                textColor = MaterialTheme.colors.error,
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
        errorOutput = "line:2:5 Undefined variable 'err'.",
        compilationTimeOutput = "Compiling...",
        compilationInProgress = true,
        onSourceInputChanged = {},
    )
}
