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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * Composables for the main application UI.
 *
 * @author Ilya Bogdanovich on 11.10.2023
 */
@Composable
fun App(
    splashScreenVisible: Boolean,
    sourceInput: TextFieldValue,
    resultOutput: String,
    errorOutput: String,
    compilationTimeOutput: String,
    compilationInProgress: Boolean,
    onSourceInputChanged: (TextFieldValue) -> Unit,
) {
    MaterialTheme {
        AnimatedVisibility(splashScreenVisible, enter = fadeIn(), exit = fadeOut()) {
            SplashScreen()
        }
        AnimatedVisibility(!splashScreenVisible, enter = fadeIn(), exit = fadeOut()) {
            AppView(
                sourceInput = sourceInput,
                resultOutput = resultOutput,
                errorOutput = errorOutput,
                compilationTimeOutput = compilationTimeOutput,
                compilationInProgress = compilationInProgress,
                onSourceInputChanged = onSourceInputChanged,
            )
        }
    }
}

@Composable
fun AppView(
    sourceInput: TextFieldValue,
    resultOutput: String,
    errorOutput: String,
    compilationTimeOutput: String,
    compilationInProgress: Boolean,
    onSourceInputChanged: (TextFieldValue) -> Unit,
) {
    Column(modifier = Modifier.fillMaxHeight().padding(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Input", fontWeight = FontWeight.Bold)
                BasicTextField(
                    modifier = Modifier.height(0.dp).weight(1f),
                    value = sourceInput,
                    onValueChange = { onSourceInputChanged(it) },
                )
                Row {
                    AnimatedVisibility(compilationInProgress) {
                        RotatingIcon(
                            Icons.Filled.Sync,
                            modifier = Modifier.padding(end = 6.dp).size(12.dp)
                        )
                    }
                    Text(
                        if (compilationInProgress) {
                            "Compiling..."
                        } else {
                            "Last compile time: $compilationTimeOutput"
                        },
                        style = MaterialTheme.typography.overline
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Output", fontWeight = FontWeight.Bold)
                    BasicTextField(
                        resultOutput,
                        onValueChange = {},
                        readOnly = true,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Errors", fontWeight = FontWeight.Bold)
                    BasicTextField(
                        errorOutput,
                        onValueChange = {},
                        textStyle = TextStyle.Default.copy(color = Color.Red),
                        readOnly = true,
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun App_Preview() {
    App(
        splashScreenVisible = false,
        sourceInput = TextFieldValue("""print "Hello, world!" """),
        resultOutput = "Hello, world!",
        errorOutput = "line:1:1 Hello world!",
        compilationTimeOutput = "Compiling...",
        compilationInProgress = true,
        onSourceInputChanged = {},
    )
}
