@file:OptIn(ExperimentalCoroutinesApi::class)

package com.ilyabogdanovich.jelly.ide.app

import androidx.compose.ui.text.input.TextFieldValue
import com.ilyabogdanovich.jelly.ide.app.documents.Document
import com.ilyabogdanovich.jelly.ide.app.documents.DocumentRepository
import com.ilyabogdanovich.jelly.jcc.core.Compiler
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

/**
 * Test for [AppViewModel]
 *
 * @author Ilya Bogdanovich on 11.10.2023
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    private val compiler = mockk<Compiler>()
    private val documentRepository = mockk<DocumentRepository>()
    private val dispatcher = UnconfinedTestDispatcher()
    private val scope = TestScope(dispatcher)

    private val viewModel = AppViewModel(
        compiler,
        documentRepository,
        EmptyLoggerFactory,
    )

    @Test
    fun `compile on new source text`() = runTest(dispatcher) {
        // Prepare
        coEvery { compiler.compile("text") } returns Compiler.Result(
            output = listOf("out", "put"),
            errors = listOf("err 1", "err 2")
        )

        // Do
        scope.launch { viewModel.processCompilationRequests() }
        viewModel.notifySourceInputChanged(TextFieldValue("text"))

        // Check
        coVerifySequence {
            compiler.compile("text")
        }
        viewModel.resultOutput shouldBe "output"
        viewModel.errorOutput shouldBe "err 1\nerr 2"
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
        coEvery { compiler.compile("text") } returns Compiler.Result(output = listOf("out"), errors = listOf("err"))
        viewModel.splashScreenVisible shouldBe true

        // Do
        scope.launch { viewModel.processCompilationRequests() }
        scope.launch { viewModel.processDocumentUpdates() }
        scope.launch { viewModel.startApp() }

        // Check
        coVerifySequence {
            documentRepository.read()
            compiler.compile("text")
        }
        viewModel.resultOutput shouldBe "out"
        viewModel.errorOutput shouldBe "err"
        viewModel.splashScreenVisible shouldBe false
    }
}
