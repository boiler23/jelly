package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

/**
 * Dialog that shows a hint about app's syntax.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
@Composable
fun SyntaxHelpDialog(visible: MutableState<Boolean>) {
    if (visible.value) {
        AlertDialog(
            onDismissRequest = { visible.value = false },
            title = { Text(text = "Jelly Syntax") },
            text = {
                Text(
                    """
                        expr    : number | id | (expr) | expr op expr | {expr, expr} | map(expr, id -> expr) | reduce(expr, expr, id id -> expr)
                        op      : +|-|*|/|^
                        stmt    : var id = expr | out expr | print "string"
                        program : stmt*
                    """.trimIndent(),
                    style = MaterialTheme.typography.body2.copy(
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                )
            },
            confirmButton = {},
            dismissButton = { Button(onClick = { visible.value = false }) { Text("Close") } }
        )
    }
}
