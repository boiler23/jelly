package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import com.ilyabogdanovich.jelly.ide.app.presentation.MainViewModel

/**
 * Composable for the main application window.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
@Composable
fun ApplicationScope.MainWindow(viewModel: MainViewModel) {
    Window(onCloseRequest = ::exitApplication) {
        MainMenuBar(
            onExit = ::exitApplication
        )
        MainView(
            splashScreenVisible = viewModel.splashScreenVisible,
            sourceInput = viewModel.sourceInput,
            errorMarkup = viewModel.errorMarkup,
            resultOutput = viewModel.resultOutput,
            navigationEffect = viewModel.navigationEffect,
            errorMessages = viewModel.errorMessages,
            compilationStatus = viewModel.compilationStatus,
            onSourceInputChanged = viewModel::notifySourceInputChanged,
            onDeepLinkClicked = viewModel::notifyDeepLinkClicked,
        )
    }
}
