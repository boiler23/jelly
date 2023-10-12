package com.ilyabogdanovich.jelly.ide.app

import androidx.compose.runtime.mutableStateOf
import com.ilyabogdanovich.jelly.jcc.core.Compiler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.measureTimedValue

/**
 * Main view model for the IDE application.
 *
 * @author Ilya Bogdanovich on 11.10.2023
 */
class AppViewModel(private val compiler: Compiler) {
    private val textInput = MutableStateFlow("")
    val resultOutput = mutableStateOf("")
    val errorOutput = mutableStateOf("")
    val compilationTimeOutput = mutableStateOf("")
    val compilationStatus = mutableStateOf(false)

    fun notifyNewTextInput(text: String) {
        textInput.value = text
    }

    suspend fun subscribeForTextInput() = textInput.collectLatest {
        compilationStatus.value = true
        val (results, duration) = measureTimedValue { compiler.compile(it) }
        compilationStatus.value = false
        resultOutput.value = results.output.joinToString("")
        errorOutput.value = results.errors.joinToString("\n")
        compilationTimeOutput.value = (duration.inWholeMilliseconds / 1000.0).toString()
    }
}
