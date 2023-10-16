package com.ilyabogdanovich.jelly.ide.app.data.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.DeepLink
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.jcc.core.eval.EvalError
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

/**
 * Test for [ErrorListBuilderImpl]
 *
 * @author Ilya Bogdanovich on 16.10.2023
 */
class ErrorListBuilderImplTest {
    private val builder = ErrorListBuilderImpl()

    @Test
    fun build() {
        // Prepare
        val source = listOf(
            "test",
            "code line",
            "another code line",
        )
        val errors = listOf(
            mockk<EvalError> {
                every { formattedMessage } returns "error 1"
                every { start } returns EvalError.TokenPosition(line = 2, positionInLine = 3)
            },
            mockk<EvalError> {
                every { formattedMessage } returns "error 2"
                every { start } returns EvalError.TokenPosition(line = 3, positionInLine = 5)
            },
        )

        // Do
        val result = builder.build(source, errors)

        // Check
        result shouldBe listOf(
            CompilationResults.ErrorMessage(
                formattedMessage = "error 1",
                deepLink = DeepLink.Cursor(position = 8),
            ),
            CompilationResults.ErrorMessage(
                formattedMessage = "error 2",
                deepLink = DeepLink.Cursor(position = 20),
            ),
        )
    }
}
