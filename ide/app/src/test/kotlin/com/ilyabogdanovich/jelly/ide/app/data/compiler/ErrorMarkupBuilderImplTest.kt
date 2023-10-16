package com.ilyabogdanovich.jelly.ide.app.data.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.compiler.ErrorMarkup
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.SourceMarkup
import com.ilyabogdanovich.jelly.jcc.core.eval.EvalError
import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [ErrorMarkupBuilderImpl]
 *
 * @author Ilya Bogdanovich on 15.10.2023
 */
class ErrorMarkupBuilderImplTest {
    private val builder = ErrorMarkupBuilderImpl()

    @Test
    fun `single-line error without stop token`() {
        // Prepare
        val sourceMarkup = SourceMarkup(
            lineStarts = listOf(0, 6, 12),
            lineLengths = listOf(5, 5, 5),
        )
        val evalErrors = listOf(
            EvalError(
                start = EvalError.TokenPosition(line = 2, positionInLine = 2),
                stop = null,
                expression = "",
                type = EvalError.Type.SyntaxError,
            )
        )

        // Do
        val result = builder.buildMarkup(sourceMarkup, evalErrors)

        // Check
        result shouldBe ErrorMarkup(errors = listOf(ErrorMarkup.Underline(line = 1, start = 8, stop = 11)))
    }

    @Test
    fun `single-line error without stop token on wrong line`() {
        // Prepare
        val sourceMarkup = SourceMarkup(
            lineStarts = listOf(0, 6, 12),
            lineLengths = listOf(5, 5, 5),
        )
        val evalErrors = listOf(
            EvalError(
                start = EvalError.TokenPosition(line = 5, positionInLine = 2),
                stop = null,
                expression = "",
                type = EvalError.Type.SyntaxError,
            )
        )

        // Do
        val result = builder.buildMarkup(sourceMarkup, evalErrors)

        // Check
        result shouldBe ErrorMarkup.empty()
    }

    @Test
    fun `single-line error with stop token`() {
        // Prepare
        val sourceMarkup = SourceMarkup(
            lineStarts = listOf(0, 6, 12),
            lineLengths = listOf(5, 5, 5),
        )
        val evalErrors = listOf(
            EvalError(
                start = EvalError.TokenPosition(line = 2, positionInLine = 2),
                stop = EvalError.TokenPosition(line = 2, positionInLine = 3),
                expression = "",
                type = EvalError.Type.SyntaxError,
            )
        )

        // Do
        val result = builder.buildMarkup(sourceMarkup, evalErrors)

        // Check
        result shouldBe ErrorMarkup(errors = listOf(ErrorMarkup.Underline(line = 1, start = 8, stop = 10)))
    }

    @Test
    fun `single-line error with stop token on wrong line`() {
        // Prepare
        val sourceMarkup = SourceMarkup(
            lineStarts = listOf(0, 6, 12),
            lineLengths = listOf(5, 5, 5),
        )
        val evalErrors = listOf(
            EvalError(
                start = EvalError.TokenPosition(line = 5, positionInLine = 2),
                stop = EvalError.TokenPosition(line = 5, positionInLine = 3),
                expression = "",
                type = EvalError.Type.SyntaxError,
            )
        )

        // Do
        val result = builder.buildMarkup(sourceMarkup, evalErrors)

        // Check
        result shouldBe ErrorMarkup.empty()
    }

    @Test
    fun `multi-line error - 2 lines`() {
        // Prepare
        val sourceMarkup = SourceMarkup(
            lineStarts = listOf(0, 6, 12),
            lineLengths = listOf(5, 5, 5),
        )
        val evalErrors = listOf(
            EvalError(
                start = EvalError.TokenPosition(line = 2, positionInLine = 2),
                stop = EvalError.TokenPosition(line = 3, positionInLine = 4),
                expression = "",
                type = EvalError.Type.SyntaxError,
            )
        )

        // Do
        val result = builder.buildMarkup(sourceMarkup, evalErrors)

        // Check
        result shouldBe ErrorMarkup(
            errors = listOf(
                ErrorMarkup.Underline(line = 1, start = 8, stop = 11),
                ErrorMarkup.Underline(line = 2, start = 12, stop = 17),
            )
        )
    }

    @Test
    fun `multi-line error - 3 lines`() {
        // Prepare
        val sourceMarkup = SourceMarkup(
            lineStarts = listOf(0, 6, 12, 24),
            lineLengths = listOf(5, 5, 11, 5),
        )
        val evalErrors = listOf(
            EvalError(
                start = EvalError.TokenPosition(line = 2, positionInLine = 3),
                stop = EvalError.TokenPosition(line = 4, positionInLine = 1),
                expression = "",
                type = EvalError.Type.SyntaxError,
            )
        )

        // Do
        val result = builder.buildMarkup(sourceMarkup, evalErrors)

        // Check
        result shouldBe ErrorMarkup(
            errors = listOf(
                ErrorMarkup.Underline(line = 1, start = 9, stop = 11),
                ErrorMarkup.Underline(line = 2, start = 12, stop = 23),
                ErrorMarkup.Underline(line = 3, start = 24, stop = 26),
            )
        )
    }

    @Test
    fun `multi-line error - 4 lines`() {
        // Prepare
        val sourceMarkup = SourceMarkup(
            lineStarts = listOf(0, 6, 12, 24, 41),
            lineLengths = listOf(5, 5, 11, 16, 5),
        )
        val evalErrors = listOf(
            EvalError(
                start = EvalError.TokenPosition(line = 2, positionInLine = 3),
                stop = EvalError.TokenPosition(line = 5, positionInLine = 1),
                expression = "",
                type = EvalError.Type.SyntaxError,
            )
        )

        // Do
        val result = builder.buildMarkup(sourceMarkup, evalErrors)

        // Check
        result shouldBe ErrorMarkup(
            errors = listOf(
                ErrorMarkup.Underline(line = 1, start = 9, stop = 11),
                ErrorMarkup.Underline(line = 2, start = 12, stop = 23),
                ErrorMarkup.Underline(line = 3, start = 24, stop = 40),
                ErrorMarkup.Underline(line = 4, start = 41, stop = 43),
            )
        )
    }

    @Test
    fun `multi-line error out of source`() {
        // Prepare
        val sourceMarkup = SourceMarkup(
            lineStarts = listOf(0, 6),
            lineLengths = listOf(5, 5),
        )
        val evalErrors = listOf(
            EvalError(
                start = EvalError.TokenPosition(line = 2, positionInLine = 3),
                stop = EvalError.TokenPosition(line = 5, positionInLine = 1),
                expression = "",
                type = EvalError.Type.SyntaxError,
            )
        )

        // Do
        val result = builder.buildMarkup(sourceMarkup, evalErrors)

        // Check
        result shouldBe ErrorMarkup(
            errors = listOf(
                ErrorMarkup.Underline(line = 1, start = 9, stop = 11),
            )
        )
    }

    @Test
    fun `multiple errors on a single line - no overlaps`() {
        // Prepare
        val sourceMarkup = SourceMarkup(
            lineStarts = listOf(0),
            lineLengths = listOf(16),
        )
        val evalErrors = listOf(
            EvalError(
                start = EvalError.TokenPosition(line = 1, positionInLine = 3),
                stop = EvalError.TokenPosition(line = 1, positionInLine = 3),
                expression = "",
                type = EvalError.Type.SyntaxError,
            ),
            EvalError(
                start = EvalError.TokenPosition(line = 1, positionInLine = 7),
                stop = EvalError.TokenPosition(line = 1, positionInLine = 8),
                expression = "",
                type = EvalError.Type.SyntaxError,
            )
        )

        // Do
        val result = builder.buildMarkup(sourceMarkup, evalErrors)

        // Check
        result shouldBe ErrorMarkup(
            errors = listOf(
                ErrorMarkup.Underline(line = 0, start = 3, stop = 4),
                ErrorMarkup.Underline(line = 0, start = 7, stop = 9),
            )
        )
    }

    @Test
    fun `multiple errors on a single line - with overlaps`() {
        // Prepare
        val sourceMarkup = SourceMarkup(
            lineStarts = listOf(0),
            lineLengths = listOf(16),
        )
        val evalErrors = listOf(
            EvalError(
                start = EvalError.TokenPosition(line = 1, positionInLine = 5),
                stop = EvalError.TokenPosition(line = 1, positionInLine = 5),
                expression = "",
                type = EvalError.Type.SyntaxError,
            ),
            EvalError(
                start = EvalError.TokenPosition(line = 1, positionInLine = 3),
                stop = EvalError.TokenPosition(line = 1, positionInLine = 8),
                expression = "",
                type = EvalError.Type.SyntaxError,
            )
        )

        // Do
        val result = builder.buildMarkup(sourceMarkup, evalErrors)

        // Check
        result shouldBe ErrorMarkup(
            errors = listOf(
                ErrorMarkup.Underline(line = 0, start = 3, stop = 9),
            )
        )
    }
}
