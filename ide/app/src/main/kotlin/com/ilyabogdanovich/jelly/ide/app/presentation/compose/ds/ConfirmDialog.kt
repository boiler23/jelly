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
 * This composable provides support for platform dialog with three options: Yes, No, or Cancel.
 *
 * @author Ilya Bogdanovich on 22.10.2023
 */
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun WindowScope.ConfirmDialog(
    title: String,
    message: String,
    onResult: (result: ConfirmDialogResult) -> Unit
) {
    DisposableEffect(Unit) {
        val job = GlobalScope.launch(Dispatchers.Swing) {
            val resultInt = JOptionPane.showConfirmDialog(
                window, message, title, JOptionPane.YES_NO_CANCEL_OPTION
            )
            val result = when (resultInt) {
                JOptionPane.YES_OPTION -> ConfirmDialogResult.Yes
                JOptionPane.NO_OPTION -> ConfirmDialogResult.No
                else -> ConfirmDialogResult.Cancel
            }
            onResult(result)
        }

        onDispose {
            job.cancel()
        }
    }
}
