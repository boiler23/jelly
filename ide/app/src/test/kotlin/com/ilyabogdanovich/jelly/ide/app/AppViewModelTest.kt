@file:OptIn(ExperimentalCoroutinesApi::class)

package com.ilyabogdanovich.jelly.ide.app

import com.ilyabiogdanovich.jelly.jcc.Compiler
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerifySequence
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
class AppViewModelTest {
    private val compiler = mockk<Compiler>()
    private val dispatcher = UnconfinedTestDispatcher()
    private val scope = TestScope(dispatcher)

    private val viewModel = AppViewModel(compiler)

    @Test
    fun `compile on subscribe`() = runTest(dispatcher) {
        // Prepare
        coEvery { compiler.compile("") } returns Compiler.Result(output = listOf(), errors = listOf())
        coEvery { compiler.compile("text") } returns Compiler.Result(
            output = listOf("out", "put"),
            errors = listOf("err 1", "err 2")
        )

        // Do
        scope.launch { viewModel.subscribeForTextInput() }
        viewModel.notifyNewTextInput("text")

        // Check
        coVerifySequence {
            compiler.compile("")
            compiler.compile("text")
        }
        viewModel.resultOutput.value shouldBe "output"
        viewModel.errorOutput.value shouldBe "err 1\nerr 2"
    }
}
