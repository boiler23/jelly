package com.ilyabiogdanovich.jelly.jcc.print

import com.ilyabiogdanovich.jelly.jcc.eval.Num
import com.ilyabiogdanovich.jelly.jcc.eval.Seq
import com.ilyabiogdanovich.jelly.jcc.eval.Var
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.util.Locale

/**
 * Test for [VarPrinter]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class VarPrinterTest {
    private val printer = VarPrinter()

    @Test
    fun `print integer`() {
        // Prepare
        val variable = Var.NumVar(Num.Integer(123))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "123"
    }

    @Test
    fun `print double`() {
        // Prepare
        val variable = Var.NumVar(Num.Real(123.456))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "123.456"
    }

    @Test
    fun `print sequence - many elements`() {
        // Prepare
        val variable = Var.SeqVar(Seq(1, 5))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ 1, 2, 3, 4, 5 }"
    }

    @Test
    fun `print sequence - 1 element`() {
        // Prepare
        val variable = Var.SeqVar(Seq(1, 1))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ 1 }"
    }

    @Test
    fun `print sequence - wrong`() {
        // Prepare
        val variable = Var.SeqVar(Seq(5, 1))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{  }"
    }
}
