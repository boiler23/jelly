package com.ilyabogdanovich.jelly.ide.app.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.EditTextField
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.TitleText

/**
 * Composable for the code editor.
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
@Composable
fun CodeEditor(
    modifier: Modifier = Modifier,
    sourceInput: TextFieldValue,
    onSourceInputChanged: (TextFieldValue) -> Unit,
) {
    Column(modifier = modifier) {
        TitleText("Input")
        EditTextField(
            modifier = Modifier
                .height(0.dp)
                .weight(1f)
                .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 2.dp),
            value = sourceInput,
            onValueChange = { onSourceInputChanged(it) },
        )
    }
}
