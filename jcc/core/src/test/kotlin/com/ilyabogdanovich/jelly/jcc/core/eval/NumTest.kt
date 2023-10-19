package com.ilyabogdanovich.jelly.jcc.core.eval

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [Num]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class NumTest {
    //
    // operations with Int and Int
    //

    @Test
    fun `int + int`() {
        // Prepare

        // Do
        val result = Num.Integer(2) + Num.Integer(3)

        // Check
        result shouldBe Num.Integer(5)
    }

    @Test
    fun `int - int`() {
        // Prepare

        // Do
        val result = Num.Integer(5) - Num.Integer(3)

        // Check
        result shouldBe Num.Integer(2)
    }

    @Test
    fun `int times int`() {
        // Prepare

        // Do
        val result = Num.Integer(2) * Num.Integer(3)

        // Check
        result shouldBe Num.Integer(6)
    }

    @Test
    fun `int div int - int`() {
        // Prepare

        // Do
        val result = Num.Integer(6) / Num.Integer(3)

        // Check
        result shouldBe Num.Integer(2)
    }

    @Test
    fun `int div int - real`() {
        // Prepare

        // Do
        val result = Num.Integer(5) / Num.Integer(2)

        // Check
        result shouldBe Num.Real(2.5)
    }

    @Test
    fun `int pow int`() {
        // Prepare

        // Do
        val result = Num.Integer(2).pow(Num.Integer(5))

        // Check
        result shouldBe Num.Integer(32)
    }

    //
    // operations with Int and Real
    //

    @Test
    fun `int + real`() {
        // Prepare

        // Do
        val result = Num.Integer(2) + Num.Real(3.0)

        // Check
        result shouldBe Num.Real(5.0)
    }

    @Test
    fun `int - real`() {
        // Prepare

        // Do
        val result = Num.Integer(5) - Num.Real(3.0)

        // Check
        result shouldBe Num.Real(2.0)
    }

    @Test
    fun `int times real`() {
        // Prepare

        // Do
        val result = Num.Integer(2) * Num.Real(3.0)

        // Check
        result shouldBe Num.Real(6.0)
    }

    @Test
    fun `int div real`() {
        // Prepare

        // Do
        val result = Num.Integer(6) / Num.Real(3.0)

        // Check
        result shouldBe Num.Real(2.0)
    }

    @Test
    fun `int pow real`() {
        // Prepare

        // Do
        val result = Num.Integer(2).pow(Num.Real(5.0))

        // Check
        result shouldBe Num.Real(32.0)
    }

    //
    // operations with Real and Int
    //

    @Test
    fun `real + int`() {
        // Prepare

        // Do
        val result = Num.Real(2.0) + Num.Integer(3)

        // Check
        result shouldBe Num.Real(5.0)
    }

    @Test
    fun `real - int`() {
        // Prepare

        // Do
        val result = Num.Real(5.0) - Num.Integer(3)

        // Check
        result shouldBe Num.Real(2.0)
    }

    @Test
    fun `real times int`() {
        // Prepare

        // Do
        val result = Num.Real(2.0) * Num.Integer(3)

        // Check
        result shouldBe Num.Real(6.0)
    }

    @Test
    fun `real div int`() {
        // Prepare

        // Do
        val result = Num.Real(6.0) / Num.Integer(3)

        // Check
        result shouldBe Num.Real(2.0)
    }

    @Test
    fun `real pow int`() {
        // Prepare

        // Do
        val result = Num.Real(2.0).pow(Num.Integer(5))

        // Check
        result shouldBe Num.Real(32.0)
    }

    //
    // operations with Real and Real
    //

    @Test
    fun `real + real`() {
        // Prepare

        // Do
        val result = Num.Real(2.0) + Num.Real(3.0)

        // Check
        result shouldBe Num.Real(5.0)
    }

    @Test
    fun `real - real`() {
        // Prepare

        // Do
        val result = Num.Real(5.0) - Num.Real(3.0)

        // Check
        result shouldBe Num.Real(2.0)
    }

    @Test
    fun `real times real`() {
        // Prepare

        // Do
        val result = Num.Real(2.0) * Num.Real(3.0)

        // Check
        result shouldBe Num.Real(6.0)
    }

    @Test
    fun `real div real`() {
        // Prepare

        // Do
        val result = Num.Real(6.0) / Num.Real(3.0)

        // Check
        result shouldBe Num.Real(2.0)
    }

    @Test
    fun `real pow real`() {
        // Prepare

        // Do
        val result = Num.Real(2.0).pow(Num.Real(5.0))

        // Check
        result shouldBe Num.Real(32.0)
    }

    //
    // Unary
    //
    @Test
    fun `negate int`() {
        // Prepare

        // Do
        val result = -Num.Integer(123)

        // Check
        result shouldBe Num.Integer(-123)
    }

    @Test
    fun `negate real`() {
        // Prepare

        // Do
        val result = -Num.Real(123.456)

        // Check
        result shouldBe Num.Real(-123.456)
    }

    //
    // Helpers
    //
    @Test
    fun `Int as Num`() {
        // Prepare

        // Do
        val result = 123.num

        // Check
        result shouldBe Num.Integer(123)
    }

    @Test
    fun `Double as Num`() {
        // Prepare

        // Do
        val result = 123.456.num

        // Check
        result shouldBe Num.Real(123.456)
    }
}
