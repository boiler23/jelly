package com.ilyabiogdanovich.jelly.jcc.eval

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.Test

/**
 * Test for [EvalContext]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class EvalContextTest {
    private val evalContext = EvalContext()

    @Test
    fun `get non-existing`() {
        // Prepare

        // Do
        val result = evalContext["id"]

        // Check
        result shouldBe null
    }
}
