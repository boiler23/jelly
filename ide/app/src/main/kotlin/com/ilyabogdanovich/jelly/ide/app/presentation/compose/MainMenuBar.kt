package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import kotlinx.coroutines.launch

/**
 * Composable for the main menu.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
@Composable
fun FrameWindowScope.MainMenuBar(
    onNew: suspend () -> Unit,
    onOpen: suspend () -> Unit,
    onSave: suspend () -> Unit,
    onExit: () -> Unit,
) = MenuBar {
    val scope = rememberCoroutineScope()
    Menu("File") {
        Item("New", shortcut = KeyShortcut(Key.N, meta = true), onClick = { scope.launch { onNew() } })
        Item("Open...", shortcut = KeyShortcut(Key.O, meta = true), onClick = { scope.launch { onOpen() } })
        Item("Save", shortcut = KeyShortcut(Key.S, meta = true), onClick = { scope.launch { onSave() } })
        Separator()
        Item("Exit", shortcut = KeyShortcut(Key.Q, meta = true), onClick = onExit)
    }
}
