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

    @Test
    fun `get existing`() {
        // Prepare
        val variable = mockk<Var>()
        evalContext.push("id", variable)

        // Do
        val result = evalContext["id"]

        // Check
        result shouldBe variable
    }

    @Test
    fun `push non-existing`() {
        // Prepare
        val variable = mockk<Var>()

        // Do
        val result = evalContext.push("id", variable)

        // Check
        result shouldBe true
    }

    @Test
    fun `push existing`() {
        // Prepare
        val variable = mockk<Var>()
        evalContext.push("id", variable)

        // Do
        val result = evalContext.push("id", variable)

        // Check
        result shouldBe false
    }

    @Test
    fun pop() {
        // Prepare
        val variable = mockk<Var>()
        evalContext.push("id", variable)

        // Do
        evalContext.pop("id")

        // Check
        evalContext["id"] shouldBe null
    }

    @Test
    fun clear() {
        // Prepare
        evalContext.push("id1", mockk())
        evalContext.push("id2", mockk())

        // Do
        evalContext.clear()

        // Check
        evalContext["id1"] shouldBe null
        evalContext["id2"] shouldBe null
    }
}
