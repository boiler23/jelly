package com.ilyabogdanovich.jelly.ide.app.presentation

import androidx.annotation.WorkerThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.ilyabogdanovich.jelly.ide.app.domain.DeepLink
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationServiceClient
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.ErrorMarkup
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
    var errorMarkup by mutableStateOf(ErrorMarkup.empty())
    var resultOutput by mutableStateOf("")
    var navigationEffect by mutableStateOf(Any())
    var errorMessages by mutableStateOf(listOf<CompilationResults.ErrorMessage>())
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

    fun notifyDeepLinkClicked(deepLink: DeepLink) {
        when (deepLink) {
            is DeepLink.Cursor -> {
                sourceInput = sourceInput.copy(selection = TextRange(deepLink.position))
                // to make sure we don't lose the editor focus on consequent clicks on the error message,
                // we assign a new value to this effect on every deep link.
                // This will make sure to run the recomposition, and hence execute the DisposableEffect block.
                navigationEffect = Any()
            }
        }
    }

    private fun notifySourceInputChangedInternal(newInput: String, oldInput: String) {
        if (newInput != oldInput) {
            logger.d { "source input changed" }
            errorMarkup = ErrorMarkup.empty()
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
        errorMarkup = compilationResults.errorMarkup
        resultOutput = compilationResults.out
        errorMessages = compilationResults.errors
        compilationTimeOutput = (compilationResults.duration.inWholeMilliseconds / 1000.0).toString()
    }
}
