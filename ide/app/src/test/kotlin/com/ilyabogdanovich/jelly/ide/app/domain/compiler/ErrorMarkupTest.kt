package com.ilyabogdanovich.jelly.ide.app.domain.compiler

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [ErrorMarkup]
 *
 * @author Ilya Bogdanovich on 15.10.2023
 */
class ErrorMarkupTest {
    @Test
    fun empty() {
        // Prepare

        // Do
        val result = ErrorMarkup.empty()

        // Check
        result shouldBe ErrorMarkup(listOf())
    }
}
