@file:Suppress("FunctionName")

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ilyabiogdanovich.jelly.jcc.Compiler

@Composable
@Preview
fun App() {
    val input = remember { mutableStateOf(TextFieldValue(text = "Hello Jelly;")) }
    var resultOutput by remember { mutableStateOf("") }
    var errorOutput by remember { mutableStateOf("") }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxHeight().padding(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth().height(0.dp).weight(1f)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Input", fontWeight = FontWeight.Bold)
                    BasicTextField(
                        value = input.value,
                        onValueChange = { newInput ->
                            input.value = newInput
                            val compilation = Compiler().compile(newInput.text)
                            resultOutput = compilation.result
                            errorOutput = compilation.errors
                        },
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Output", fontWeight = FontWeight.Bold)
                    Text(resultOutput)
                    Text("Errors", fontWeight = FontWeight.Bold)
                    Text(errorOutput, color = Color.Red)
                }
            }
            Button(
                modifier = Modifier.wrapContentHeight(),
                onClick = {
                    val compilation = Compiler().compile(input.value.text)
                    resultOutput = compilation.result
                    errorOutput = compilation.errors
                }
            ) {
                Text("Compile")
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
