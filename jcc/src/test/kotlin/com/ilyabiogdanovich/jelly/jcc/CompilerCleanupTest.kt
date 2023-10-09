package com.ilyabiogdanovich.jelly.jcc

import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

/**
 * Test for [Compiler] dedicated on variables cleanup.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class CompilerCleanupTest {
    private val compiler = Compiler()

    @Test
    fun cleanup() {
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
        result shouldBe Compiler.Result(
            output = listOf(),
            errors = listOf("1:4: Variable undeclared: `n`."),
        )
    }
}
