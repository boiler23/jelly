package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [EvalContext]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class EvalContextTest {
    @Test
    fun `get non-existing`() {
        // Prepare
        val evalContext = EvalContext(mapOf("n" to 1.toVar()))

        // Do
        val result = evalContext["id"]

        // Check
        result shouldBe null
    }

    @Test
    fun `add non-existing key`() {
        // Prepare

        // Do
        val result = EvalContext(mapOf("n" to 1.toVar())) + mapOf("id" to 2.toVar())

        // Check
        result shouldBe EvalContext(mapOf("n" to 1.toVar(), "id" to 2.toVar())).asRight()
    }

    @Test
    fun `add existing key`() {
        // Prepare

        // Do
        val result = EvalContext(mapOf("id" to 1.toVar())) + mapOf("id" to 1.toVar())

        // Check
        result shouldBe setOf("id").asLeft()
    }
}
