package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.eval.EvalContext
import io.kotest.matchers.shouldBe
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
