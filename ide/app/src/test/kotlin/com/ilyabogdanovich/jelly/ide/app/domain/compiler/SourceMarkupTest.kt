package com.ilyabogdanovich.jelly.ide.app.domain.compiler

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [SourceMarkup]
 *
 * @author Ilya Bogdanovich on 17.10.2023
 */
class SourceMarkupTest {
    @Test
    fun `from empty source`() {
        // Prepare

        // Do
        val result = SourceMarkup.from("")

        // Check
        result shouldBe SourceMarkup(lineLengths = listOf(0), lineStarts = listOf(0))
    }

    @Test
    fun `from single line`() {
        // Prepare

        // Do
        val result = SourceMarkup.from("test")

        // Check
        result shouldBe SourceMarkup(lineLengths = listOf(4), lineStarts = listOf(0))
    }

    @Test
    fun `from single line with newline in the end`() {
        // Prepare

        // Do
        val result = SourceMarkup.from("everything\n")

        // Check
        result shouldBe SourceMarkup(lineLengths = listOf(10, 0), lineStarts = listOf(0, 11))
    }

    @Test
    fun `from single line with newline only`() {
        // Prepare

        // Do
        val result = SourceMarkup.from("\n")

        // Check
        result shouldBe SourceMarkup(lineLengths = listOf(0, 0), lineStarts = listOf(0, 1))
    }

    @Test
    fun `from multiline text`() {
        // Prepare

        // Do
        val result = SourceMarkup.from(
            """
                everything
                has
                limit
            """.trimIndent()
        )

        // Check
        result shouldBe SourceMarkup(lineLengths = listOf(10, 3, 5), lineStarts = listOf(0, 11, 15))
    }

    @Test
    fun `from multiline with newline in the end`() {
        // Prepare

        // Do
        val result = SourceMarkup.from("everything\nhas\nlimit\n")

        // Check
        result shouldBe SourceMarkup(lineLengths = listOf(10, 3, 5, 0), lineStarts = listOf(0, 11, 15, 21))
    }

    @Test
    fun `from multiline with newlines only`() {
        // Prepare

        // Do
        val result = SourceMarkup.from("\n\n\n")

        // Check
        result shouldBe SourceMarkup(lineLengths = listOf(0, 0, 0, 0), lineStarts = listOf(0, 1, 2, 3))
    }
}
