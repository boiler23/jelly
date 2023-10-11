package com.ilyabogdanovich.jelly.ide.app

import androidx.compose.runtime.mutableStateOf
import com.ilyabiogdanovich.jelly.jcc.Compiler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Main view model for the IDE application.
 *
 * @author Ilya Bogdanovich on 11.10.2023
 */
class AppViewModel(private val compiler: Compiler) {
    private val textInput = MutableStateFlow("")
    val resultOutput = mutableStateOf("")
    val errorOutput = mutableStateOf("")

    fun notifyNewTextInput(text: String) {
        textInput.value = text
    }

    suspend fun subscribeForTextInput() = textInput.collectLatest {
        val results = compiler.compile(it)
        resultOutput.value = results.output.joinToString("")
        errorOutput.value = results.errors.joinToString("\n")
    }
}
