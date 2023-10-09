package com.ilyabiogdanovich.jelly.jcc

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [Compiler]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class CompilerTest {
    private val compiler = Compiler()

    @Test
    fun `compile empty`() {
        // Prepare

        // Do
        val result = compiler.compile("")

        // Check
        result shouldBe Compiler.Output(
            results = listOf(),
            errors = listOf(),
        )
    }
    
    @Test
    fun `compile out number`() {
        // Prepare

        // Do
        val result = compiler.compile("out 500")

        // Check
        result shouldBe Compiler.Output(
            results = listOf("500"),
            errors = listOf(),
        )
    }

    @Test
    fun `compile out number - with error`() {
        // Prepare

        // Do
        val result = compiler.compile("out 500;")

        // Check
        result shouldBe Compiler.Output(
            results = listOf("500"),
            errors = listOf("line 1:7 token recognition error at: ';'"),
        )
    }
}
