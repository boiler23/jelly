@file:Suppress("FunctionName")

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
    val resultOutput = mutableStateListOf<String>()
    val errorOutput = mutableStateListOf<String>()

    fun handleInput(newInput: TextFieldValue) {
        input.value = newInput
        val compilation = Compiler().compile(newInput.text)
        resultOutput.clear()
        resultOutput.addAll(compilation.results)
        errorOutput.clear()
        errorOutput.addAll(compilation.errors)
    }

    handleInput(input.value)

    MaterialTheme {
        Column(modifier = Modifier.fillMaxHeight().padding(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Input", fontWeight = FontWeight.Bold)
                    BasicTextField(
                        value = input.value,
                        onValueChange = ::handleInput,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Output", fontWeight = FontWeight.Bold)
                        LazyColumn {
                            items(resultOutput) {
                                Text(it)
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Errors", fontWeight = FontWeight.Bold)
                        LazyColumn {
                            items(errorOutput) {
                                Text(it, color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
