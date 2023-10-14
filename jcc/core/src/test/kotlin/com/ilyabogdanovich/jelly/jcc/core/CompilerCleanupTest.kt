package com.ilyabogdanovich.jelly.jcc.core

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Test for [Compiler] dedicated on variables cleanup.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class CompilerCleanupTest {
    private val compiler = Compiler()

    @Test
    fun cleanup() = runTest {
        // Prepare

        // Do
        compiler.compile(
            """
                var n = 3
            """.trimIndent()
        )
        val result = compiler.compile(
            """
                out n
            """.trimIndent()
        )

        // Check
        result.output shouldBe ""
        result.errors.map { it.formattedMessage } shouldBe listOf("line 1:4: Variable undeclared: 'n'.")
    }
}
