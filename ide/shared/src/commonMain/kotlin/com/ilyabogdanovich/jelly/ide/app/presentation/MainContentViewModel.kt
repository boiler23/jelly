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
import com.ilyabogdanovich.jelly.ide.app.domain.documents.DocumentContentTracker
import com.ilyabogdanovich.jelly.ide.app.presentation.compiler.CompilationStatus
import com.ilyabogdanovich.jelly.logging.LoggerFactory
import com.ilyabogdanovich.jelly.logging.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * View model for the main content of the IDE application: code editor & its output panels.
 *
 * @author Ilya Bogdanovich on 11.10.2023
 */
class MainContentViewModel(
    private val compilationServiceClient: CompilationServiceClient,
    private val documentContentTracker: DocumentContentTracker,
    loggerFactory: LoggerFactory
) : PlatformViewModel() {
    private val logger = loggerFactory.get<MainContentViewModel>()
    var splashScreenVisible by mutableStateOf(true)
    var sourceInput by mutableStateOf(TextFieldValue(""))
    var errorMarkup by mutableStateOf(ErrorMarkup.empty())
    var resultOutput by mutableStateOf("")
    var navigationEffect by mutableStateOf(Any())
    var errorMessages by mutableStateOf(listOf<CompilationResults.ErrorMessage>())
    var compilationStatus by mutableStateOf<CompilationStatus>(CompilationStatus.Empty)

    private val documentUpdates = MutableSharedFlow<Document>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val compilationRequests = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * Processes the changes to the source code text input.
     */
    fun notifySourceInputChanged(input: TextFieldValue) {
        notifySourceInputChangedInternal(newInput = input.text, oldInput = sourceInput.text)
        sourceInput = input
    }

    /**
     * Processed the deep link clicks.
     */
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
            compilationStatus = CompilationStatus.InProgress
            compilationRequests.tryEmit(newInput)
            documentUpdates.tryEmit(Document(newInput))
        }
    }

    /**
     * Processes changes to the internal document contents,
     * not caused by the edit field changes (e.g. a new file opened).
     */
    suspend fun processContentChanges() = documentContentTracker.internalContentChanges.collectLatest {
        logger.d { "internal contents changed" }
        val text = it.text
        sourceInput = TextFieldValue(text)
        compilationRequests.tryEmit(text)
        splashScreenVisible = false
    }

    @WorkerThread
    suspend fun processDocumentUpdates() = documentUpdates.collectLatest {
        logger.d { "saving document" }
        documentContentTracker.handleContentChanges(it)
    }

    @WorkerThread
    @Suppress("TooGenericExceptionCaught")
    suspend fun processCompilationRequests() = compilationRequests.collectLatest {
        logger.d { "compiling input" }
        try {
            val compilationResults = compilationServiceClient.compile(it)
            compilationStatus = CompilationStatus.Done(compilationResults.duration)
            errorMarkup = compilationResults.errorMarkup
            resultOutput = compilationResults.out
            errorMessages = compilationResults.errors
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            compilationStatus = CompilationStatus.Exception(e)
        }
    }
}
