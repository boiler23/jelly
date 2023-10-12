package com.ilyabogdanovich.jelly.jcc.eval

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [EvalError]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class EvalErrorTest {
    @Test
    fun `formatted message - invalid number`() {
        // Prepare
        val error = EvalError(
            line = 1,
            positionInLine = 2,
            expression = "3i+4",
            type = EvalError.Type.InvalidNumber,
        )

        // Do
        val result = error.formattedMessage

        // Check
        result shouldBe "1:2: Invalid number encountered in `3i+4`. It is neither integer or double."
    }

    @Test
    fun `formatted message - unsupported expression`() {
        // Prepare
        val error = EvalError(
            line = 2,
            positionInLine = 3,
            expression = "UNSUPPORTED",
            type = EvalError.Type.UnsupportedExpression,
        )

        // Do
        val result = error.formattedMessage

        // Check
        result shouldBe "2:3: Unsupported expression encountered: `UNSUPPORTED`."
    }
}
