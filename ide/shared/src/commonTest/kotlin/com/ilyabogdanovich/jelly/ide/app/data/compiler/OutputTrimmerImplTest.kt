package com.ilyabogdanovich.jelly.ide.app.data.compiler

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [OutputTrimmerImpl]
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
class OutputTrimmerImplTest {
    private val trimmer = OutputTrimmerImpl(maxLength = 10)

    @Test
    fun `trim short string`() {
        // Prepare

        // Do
        val result = trimmer.trim("abcde")

        // Check
        result shouldBe "abcde"
    }

    @Test
    fun `trim long string`() {
        // Prepare

        // Do
        val result = trimmer.trim("abcdefghijklmnopqrstuvwxyz")

        // Check
        result shouldBe "abcdefghij\nOutput is too large to display."
    }
}
