package com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.WindowScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import javax.swing.JOptionPane

/**
 * This composable provides support for platform dialog with only one button to close it.
 *
 * @author Ilya Bogdanovich on 22.10.2023
 */
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun WindowScope.AlertDialog(
    title: String,
    message: String,
    onResult: () -> Unit
) {
    DisposableEffect(Unit) {
        val job = GlobalScope.launch(Dispatchers.Swing) {
            JOptionPane.showConfirmDialog(window, message, title, JOptionPane.CLOSED_OPTION)
            onResult()
        }

        onDispose {
            job.cancel()
        }
    }
}
