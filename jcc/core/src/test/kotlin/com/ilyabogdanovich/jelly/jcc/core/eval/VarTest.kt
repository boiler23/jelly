package com.ilyabogdanovich.jelly.jcc.core.eval

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [Var]
 *
 * @author Ilya Bogdanovich on 10.10.2023
 */
class VarTest {
    @Test
    fun `int to var`() {
        // Prepare

        // Do
        val result = 123.toVar()

        // Check
        result shouldBe Var.NumVar(Num.Integer(123))
    }

    @Test
    fun `double to var`() {
        // Prepare

        // Do
        val result = 123.456.toVar()

        // Check
        result shouldBe Var.NumVar(Num.Real(123.456))
    }

    @Test
    fun `num to var`() {
        // Prepare

        // Do
        val result = 123.456.num.toVar()

        // Check
        result shouldBe Var.NumVar(Num.Real(123.456))
    }
}
