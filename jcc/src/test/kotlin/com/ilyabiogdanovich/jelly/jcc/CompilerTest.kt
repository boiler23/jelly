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

    @Test
    fun `compile print string - empty`() {
        // Prepare

        // Do
        val result = compiler.compile("print")

        // Check
        result shouldBe Compiler.Output(
            results = listOf(),
            errors = listOf("line 1:5 missing STRING at '<EOF>'"),
        )
    }

    @Test
    fun `compile print string - quoted`() {
        // Prepare

        // Do
        val result = compiler.compile("print \"text\"")

        // Check
        result shouldBe Compiler.Output(
            results = listOf("text"),
            errors = listOf(),
        )
    }

    @Test
    fun `compile print string - multiline`() {
        // Prepare

        // Do
        val result = compiler.compile("print \"line 1\nline 2\"")

        // Check
        result shouldBe Compiler.Output(
            results = listOf(),
            errors = listOf("line 1:6 mismatched input '\"line 1' expecting STRING"),
        )
    }

    @Test
    fun `compile print string - non-quoted`() {
        // Prepare

        // Do
        val result = compiler.compile("print text")

        // Check
        result shouldBe Compiler.Output(
            results = listOf(),
            errors = listOf("line 1:6 mismatched input 'text' expecting STRING"),
        )
    }

    @Test
    fun `compile print string - single quotes inside the string`() {
        // Prepare

        // Do
        val result = compiler.compile("print \"'text'\"")

        // Check
        result shouldBe Compiler.Output(
            results = listOf("'text'"),
            errors = listOf(),
        )
    }

    @Test
    fun `compile print string - non-closed quotes`() {
        // Prepare

        // Do
        val result = compiler.compile("print \"text")

        // Check
        result shouldBe Compiler.Output(
            results = listOf(),
            errors = listOf("line 1:6 mismatched input '\"text' expecting STRING"),
        )
    }

    @Test
    fun `compile print string - non-opened quotes`() {
        // Prepare

        // Do
        val result = compiler.compile("print text\"")

        // Check
        result shouldBe Compiler.Output(
            results = listOf(),
            errors = listOf("line 1:6 mismatched input 'text' expecting STRING"),
        )
    }

    @Test
    fun `compile print string - double quotes inside the string`() {
        // Prepare

        // Do
        val result = compiler.compile("print \"te\\\"xt\"")

        // Check
        result shouldBe Compiler.Output(
            results = listOf("te\"xt"),
            errors = listOf(),
        )
    }

    @Test
    fun `compile print string - slash inside the string`() {
        // Prepare

        // Do
        val result = compiler.compile("print \"te\\xt\"")

        // Check
        result shouldBe Compiler.Output(
            results = listOf("te\\xt"),
            errors = listOf(),
        )
    }
}
