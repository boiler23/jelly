package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.ColorBackgroundAlternative
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.RotatingIcon

/**
 * Composable for presenting the compilation status.
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
@Composable
fun CompilationStatus(
    compilationTimeOutput: String,
    compilationInProgress: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorBackgroundAlternative())
            .padding(10.dp)
    ) {
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
