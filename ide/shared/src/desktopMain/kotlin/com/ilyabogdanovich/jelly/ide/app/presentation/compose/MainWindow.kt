package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import com.ilyabogdanovich.jelly.ide.app.presentation.MainContentViewModel
import com.ilyabogdanovich.jelly.ide.app.presentation.MainWindowViewModel
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.AlertDialog
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.ConfirmDialog
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.FileDialog
import okio.Path.Companion.toOkioPath

/**
 * Composable for the main application window.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
@Composable
fun ApplicationScope.MainWindow(
    mainWindowViewModel: MainWindowViewModel,
    viewModel: MainContentViewModel,
) {
    Window(
        title = mainWindowViewModel.windowTitle,
        onCloseRequest = ::exitApplication
    ) {
        MainMenuBar(
            onNew = mainWindowViewModel::new,
            onOpen = mainWindowViewModel::open,
            onSave = mainWindowViewModel::save,
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
        if (mainWindowViewModel.isOpenFileDialogVisible) {
            FileDialog(
                "Open file",
                isLoad = true,
                onResult = { mainWindowViewModel.openResult(it?.toOkioPath()) }
            )
        }
        if (mainWindowViewModel.isSaveFileDialogVisible) {
            FileDialog(
                "Save file",
                isLoad = false,
                onResult = { mainWindowViewModel.saveResult(it?.toOkioPath()) }
            )
        }
        if (mainWindowViewModel.isCloseFileDialogVisible) {
            ConfirmDialog(
                "Do you want to save current file?",
                "If you choose no - all contents will be lost!",
                onResult = { mainWindowViewModel.closeResult(it) }
            )
        }
        if (mainWindowViewModel.isFailedOpenDialogVisible) {
            AlertDialog(
                "File not supported",
                "This file is not supported. You can only open files with extension 'jy'.",
                onResult = { mainWindowViewModel.failedOpenResult() }
            )
        }
    }
}
