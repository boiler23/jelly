@file:OptIn(ExperimentalCoroutinesApi::class)

package com.ilyabogdanovich.jelly.ide.app.presentation

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.ilyabogdanovich.jelly.ide.app.domain.DeepLink
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationServiceClient
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.ErrorMarkup
import com.ilyabogdanovich.jelly.ide.app.domain.documents.Document
import com.ilyabogdanovich.jelly.ide.app.domain.documents.DocumentContentTracker
import com.ilyabogdanovich.jelly.ide.app.presentation.compiler.CompilationStatus
import com.ilyabogdanovich.jelly.logging.EmptyLoggerFactory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * Test for [MainContentViewModel]
 *
 * @author Ilya Bogdanovich on 11.10.2023
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainContentViewModelTest {
    private val compilationServiceClient = mockk<CompilationServiceClient>()
    private val documentContentTracker = mockk<DocumentContentTracker>()
    private val dispatcher = UnconfinedTestDispatcher()
    private val scope = TestScope(dispatcher)

    private val viewModel = MainContentViewModel(
        compilationServiceClient,
        documentContentTracker,
        EmptyLoggerFactory,
    )

    @Test
    fun `compile on new source text - success`() = runTest(dispatcher) {
        // Prepare
        val errorMarkup = ErrorMarkup(listOf(ErrorMarkup.Underline(1, 1, 1)))
        val errorMessages = listOf(
            CompilationResults.ErrorMessage("error 1", DeepLink.Cursor(position = 1)),
            CompilationResults.ErrorMessage("error 2", DeepLink.Cursor(position = 2)),
        )
        coEvery { compilationServiceClient.compile("text") } returns CompilationResults(
            out = "output",
            errors = errorMessages,
            errorMarkup = errorMarkup,
            duration = 1500.milliseconds,
        )

        // Do
        scope.launch { viewModel.processCompilationRequests() }
        viewModel.notifySourceInputChanged(TextFieldValue("text"))

        // Check
        coVerifySequence {
            compilationServiceClient.compile("text")
        }
        viewModel.errorMarkup shouldBe errorMarkup
        viewModel.errorMessages shouldBe errorMessages
        viewModel.resultOutput shouldBe "output"
        viewModel.compilationStatus shouldBe CompilationStatus.Done(1500.milliseconds)
    }

    @Test
    fun `compile on new source text - exception`() = runTest(dispatcher) {
        // Prepare
        val exception = OutOfMemoryError("Failed to allocate")
        coEvery { compilationServiceClient.compile("text") } throws exception

        // Do
        scope.launch { viewModel.processCompilationRequests() }
        viewModel.notifySourceInputChanged(TextFieldValue("text"))

        // Check
        coVerifySequence {
            compilationServiceClient.compile("text")
        }
        viewModel.errorMarkup shouldBe ErrorMarkup.empty()
        viewModel.errorMessages shouldBe listOf()
        viewModel.resultOutput shouldBe ""
        viewModel.compilationStatus shouldBe CompilationStatus.Exception(exception)
    }

    @Test
    fun `save document on new source text`() {
        // Prepare
        coEvery { documentContentTracker.handleContentChanges(Document("text")) } returns Unit

        // Do
        scope.launch { viewModel.processDocumentUpdates() }
        viewModel.notifySourceInputChanged(TextFieldValue("text"))

        // Check
        coVerifySequence {
            documentContentTracker.handleContentChanges(Document("text"))
        }
        viewModel.resultOutput shouldBe ""
        viewModel.errorMessages shouldBe listOf()
    }

    @Test
    fun `clear error markup on new source text - text changed`() {
        // Prepare
        viewModel.errorMarkup = ErrorMarkup(listOf(ErrorMarkup.Underline(line = 1, start = 2, stop = 3)))
        viewModel.sourceInput = TextFieldValue("old text")

        // Do
        viewModel.notifySourceInputChanged(TextFieldValue("new text"))

        // Check
        viewModel.errorMarkup shouldBe ErrorMarkup.empty()
        viewModel.sourceInput shouldBe TextFieldValue("new text")
        viewModel.compilationStatus shouldBe CompilationStatus.InProgress
    }

    @Test
    fun `clear error markup on new source text - text not changed`() {
        // Prepare
        val markup = ErrorMarkup(listOf(ErrorMarkup.Underline(line = 1, start = 2, stop = 3)))
        viewModel.errorMarkup = markup
        viewModel.sourceInput = TextFieldValue("text")

        // Do
        viewModel.notifySourceInputChanged(TextFieldValue("text"))

        // Check
        viewModel.errorMarkup shouldBe markup
        viewModel.sourceInput shouldBe TextFieldValue("text")
        viewModel.compilationStatus shouldBe CompilationStatus.Empty
    }

    @Test
    fun `compile on app start`() = runTest(dispatcher) {
        // Prepare
        val errorMarkup = ErrorMarkup(listOf(ErrorMarkup.Underline(1, 1, 1)))
        val errorMessages = listOf(
            CompilationResults.ErrorMessage("error 1", DeepLink.Cursor(position = 1)),
            CompilationResults.ErrorMessage("error 2", DeepLink.Cursor(position = 2)),
        )
        every { documentContentTracker.internalContentChanges } returns flowOf(Document(text = "text"))
        coEvery { compilationServiceClient.compile("text") } returns CompilationResults(
            out = "out",
            errors = errorMessages,
            errorMarkup = errorMarkup,
            duration = 1000.milliseconds,
        )
        viewModel.splashScreenVisible shouldBe true

        // Do
        scope.launch { viewModel.processCompilationRequests() }
        scope.launch { viewModel.processDocumentUpdates() }
        scope.launch { viewModel.processContentChanges() }

        // Check
        coVerifySequence {
            compilationServiceClient.compile("text")
        }
        viewModel.errorMarkup shouldBe errorMarkup
        viewModel.errorMessages shouldBe errorMessages
        viewModel.resultOutput shouldBe "out"
        viewModel.compilationStatus shouldBe CompilationStatus.Done(1000.milliseconds)
        viewModel.splashScreenVisible shouldBe false
    }

    @Test
    fun `handle cursor deep link`() {
        // Prepare
        viewModel.sourceInput = TextFieldValue("test")
        val initialNavigationEffect = viewModel.navigationEffect

        // Do
        viewModel.notifyDeepLinkClicked(DeepLink.Cursor(position = 10))

        // Check
        viewModel.sourceInput shouldBe TextFieldValue("test", TextRange(10))
        viewModel.navigationEffect shouldNotBe initialNavigationEffect
    }
}
