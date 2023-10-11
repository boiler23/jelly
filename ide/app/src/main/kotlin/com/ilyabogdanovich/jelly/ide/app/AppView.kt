@file:Suppress("FunctionName")

package com.ilyabogdanovich.jelly.ide.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * Composable for the main application UI.
 *
 * @author Ilya Bogdanovich on 11.10.2023
 */
@Composable
@Preview
fun App(
    resultOutput: MutableState<String>,
    errorOutput: MutableState<String>,
    onInputTextChanged: (String) -> Unit
) {
    val input = remember {
        mutableStateOf(
            TextFieldValue(
                text = """
                    var n = 500
                    var seq = map({0, n}, i -> (-1)^i / (2 * i + 1))
                    var pi = 4 * reduce(seq, 0, x y -> x + y)
                    print "pi = "
                    out pi
                """.trimIndent()
            )
        )
    }

    onInputTextChanged(input.value.text)

    MaterialTheme {
        Column(modifier = Modifier.fillMaxHeight().padding(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Input", fontWeight = FontWeight.Bold)
                    BasicTextField(
                        value = input.value,
                        onValueChange = {
                            input.value = it
                            onInputTextChanged(it.text)
                        },
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Output", fontWeight = FontWeight.Bold)
                        BasicTextField(
                            resultOutput.value,
                            onValueChange = {},
                            readOnly = true,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Errors", fontWeight = FontWeight.Bold)
                        BasicTextField(
                            errorOutput.value,
                            onValueChange = {},
                            textStyle = TextStyle.Default.copy(color = Color.Red),
                            readOnly = true,
                        )
                    }
                }
            }
        }
    }
}
