package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextFieldValue
import com.ilyabogdanovich.jelly.ide.app.domain.DeepLink
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.ErrorMarkup
import com.ilyabogdanovich.jelly.ide.app.presentation.compiler.CompilationStatus

/**
 * Collection of previews to debug layouts.
 *
 * @author Ilya Bogdanovich on 25.10.2023
 */
@Preview
@Composable
fun SplashScreen_Preview() {
    MaterialTheme {
        SplashScreen()
    }
}

@Composable
@Preview
fun MainView_Preview() {
    MainView(
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
        compilationStatus = CompilationStatus.InProgress,
        onSourceInputChanged = {},
        onDeepLinkClicked = {},
    )
}
