package com.ilyabogdanovich.jelly.ide.app.data.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.DeepLink
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.SourceMarkup
import com.ilyabogdanovich.jelly.jcc.core.Error
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
        val sourceMarkup = SourceMarkup(
            lineStarts = listOf(0, 5, 15),
            lineLengths = listOf(4, 9, 17),
        )
        val errors = listOf(
            mockk<Error> {
                every { formattedMessage } returns "error 1"
                every { start } returns Error.TokenPosition(line = 2, positionInLine = 3)
            },
            mockk<Error> {
                every { formattedMessage } returns "error 2"
                every { start } returns Error.TokenPosition(line = 3, positionInLine = 5)
            },
        )

        // Do
        val result = builder.build(sourceMarkup, errors)

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
