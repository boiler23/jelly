package com.ilyabogdanovich.jelly.ide.app.presentation

import androidx.annotation.WorkerThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationServiceClient
import com.ilyabogdanovich.jelly.ide.app.domain.documents.Document
import com.ilyabogdanovich.jelly.ide.app.domain.documents.DocumentRepository
import com.ilyabogdanovich.jelly.logging.LoggerFactory
import com.ilyabogdanovich.jelly.logging.get
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Main view model for the IDE application.
 *
 * @author Ilya Bogdanovich on 11.10.2023
 */
class AppViewModel(
    private val compilationServiceClient: CompilationServiceClient,
    private val documentRepository: DocumentRepository,
    loggerFactory: LoggerFactory
) {
    private val logger = loggerFactory.get<AppViewModel>()
    var splashScreenVisible by mutableStateOf(true)
    var sourceInput by mutableStateOf(TextFieldValue(""))
    var resultOutput by mutableStateOf("")
    var errorOutput by mutableStateOf("")
    var compilationTimeOutput by mutableStateOf("")
    var compilationInProgress by mutableStateOf(false)

    private val documentUpdates = MutableSharedFlow<Document>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val compilationRequests = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    fun notifySourceInputChanged(input: TextFieldValue) {
        notifySourceInputChangedInternal(newInput = input.text, oldInput = sourceInput.text)
        sourceInput = input
    }

    private fun notifySourceInputChangedInternal(newInput: String, oldInput: String) {
        if (newInput != oldInput) {
            logger.d { "source input changed" }
            compilationRequests.tryEmit(newInput)
            documentUpdates.tryEmit(Document(newInput))
        }
    }

    @WorkerThread
    fun startApp() {
        logger.d { "preparing app" }
        val text = documentRepository.read().text
        sourceInput = TextFieldValue(text)
        compilationRequests.tryEmit(text)
        splashScreenVisible = false
    }

    @WorkerThread
    suspend fun processDocumentUpdates() = documentUpdates.collectLatest {
        logger.d { "saving document" }
        documentRepository.write(it)
    }

    @WorkerThread
    suspend fun processCompilationRequests() = compilationRequests.collectLatest {
        logger.d { "compiling input" }
        compilationInProgress = true
        val compilationResults = compilationServiceClient.compile(it)
        compilationInProgress = false
        resultOutput = compilationResults.out
        errorOutput = compilationResults.err
        compilationTimeOutput = (compilationResults.duration.inWholeMilliseconds / 1000.0).toString()
    }
}
