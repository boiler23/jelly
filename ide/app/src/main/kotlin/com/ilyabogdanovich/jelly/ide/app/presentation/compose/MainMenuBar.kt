package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar

/**
 * Composable for the main menu.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
@Composable
fun FrameWindowScope.MainMenuBar(
    onExit: () -> Unit,
) = MenuBar {
    Menu("File") {
        Item("Exit", onClick = onExit)
    }
}
