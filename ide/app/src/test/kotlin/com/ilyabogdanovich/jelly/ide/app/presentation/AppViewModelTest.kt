@file:OptIn(ExperimentalCoroutinesApi::class)

package com.ilyabogdanovich.jelly.ide.app.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationServiceClient
import com.ilyabogdanovich.jelly.ide.app.domain.documents.Document
import com.ilyabogdanovich.jelly.ide.app.domain.documents.DocumentRepository
import com.ilyabogdanovich.jelly.logging.EmptyLoggerFactory
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * Test for [AppViewModel]
 *
 * @author Ilya Bogdanovich on 11.10.2023
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    private val compilationServiceClient = mockk<CompilationServiceClient>()
    private val documentRepository = mockk<DocumentRepository>()
    private val dispatcher = UnconfinedTestDispatcher()
    private val scope = TestScope(dispatcher)

    private val viewModel = AppViewModel(
        compilationServiceClient,
        documentRepository,
        EmptyLoggerFactory,
    )

    @Test
    fun `compile on new source text`() = runTest(dispatcher) {
        // Prepare
        coEvery { compilationServiceClient.compile("text") } returns CompilationResults(
            out = "output",
            err = "err 1\nerr 2",
            duration = 1500.milliseconds,
        )

        // Do
        scope.launch { viewModel.processCompilationRequests() }
        viewModel.notifySourceInputChanged(TextFieldValue("text"))

        // Check
        coVerifySequence {
            compilationServiceClient.compile("text")
        }
        viewModel.resultOutput shouldBe "output"
        viewModel.errorOutput shouldBe "err 1\nerr 2"
        viewModel.compilationTimeOutput shouldBe "1.5"
        viewModel.compilationInProgress shouldBe false
    }

    @Test
    fun `save document on new source text`() {
        // Prepare
        coEvery { documentRepository.write(Document("text")) } returns Unit

        // Do
        scope.launch { viewModel.processDocumentUpdates() }
        viewModel.notifySourceInputChanged(TextFieldValue("text"))

        // Check
        coVerifySequence {
            documentRepository.write(Document("text"))
        }
        viewModel.resultOutput shouldBe ""
        viewModel.errorOutput shouldBe ""
    }

    @Test
    fun `compile on app start`() = runTest(dispatcher) {
        // Prepare
        every { documentRepository.read() } returns Document(text = "text")
        coEvery { compilationServiceClient.compile("text") } returns CompilationResults(
            out = "out",
            err = "err",
            duration = 1000.milliseconds,
        )
        viewModel.splashScreenVisible shouldBe true

        // Do
        scope.launch { viewModel.processCompilationRequests() }
        scope.launch { viewModel.processDocumentUpdates() }
        scope.launch { viewModel.startApp() }

        // Check
        coVerifySequence {
            documentRepository.read()
            compilationServiceClient.compile("text")
        }
        viewModel.resultOutput shouldBe "out"
        viewModel.errorOutput shouldBe "err"
        viewModel.compilationTimeOutput shouldBe "1.0"
        viewModel.compilationInProgress shouldBe false
        viewModel.splashScreenVisible shouldBe false
    }
}
