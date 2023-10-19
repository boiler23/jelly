package com.ilyabogdanovich.jelly.jcc.core.print

import com.ilyabogdanovich.jelly.jcc.core.eval.Seq
import com.ilyabogdanovich.jelly.jcc.core.eval.Var
import com.ilyabogdanovich.jelly.jcc.core.eval.num
import com.ilyabogdanovich.jelly.jcc.core.eval.toSeq
import com.ilyabogdanovich.jelly.jcc.core.eval.toVar
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * Test for [VarPrinter]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class VarPrinterTest {
    private val printer = VarPrinter()

    private fun Seq.toVar() = Var.SeqVar(this)

    @Before
    fun setUp() {
        Locale.setDefault(Locale.ROOT)
    }

    @Test
    fun `print integer`() {
        // Prepare
        val variable = 123.toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "123"
    }

    @Test
    fun `print double`() {
        // Prepare
        val variable = 123.456.toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "123.456"
    }

    @Test
    fun `print double with long fraction`() {
        // Prepare
        val variable = 3.1415926.toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "3.1415926"
    }

    @Test
    fun `print positive infinity`() {
        // Prepare
        val variable = Double.POSITIVE_INFINITY.toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "Infinity"
    }

    @Test
    fun `print negative infinity`() {
        // Prepare
        val variable = Double.NEGATIVE_INFINITY.toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "-Infinity"
    }

    @Test
    fun `print NaN`() {
        // Prepare
        val variable = Double.NaN.toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "NaN"
    }

    @Test
    fun `print bounds sequence - many elements`() {
        // Prepare
        val variable = Var.SeqVar(Seq.fromBounds(1, 5))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ 1, 2, 3, 4, 5 }"
    }

    @Test
    fun `print bounds sequence - 1 element`() {
        // Prepare
        val variable = Var.SeqVar(Seq.fromBounds(1, 1))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ 1 }"
    }

    @Test
    fun `print array sequence - many elements`() {
        // Prepare
        val variable = listOf(1.num, 2.0.num, 3.num, 4.0.num, 5.0.num).toSeq().toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ 1, 2, 3, 4, 5 }"
    }

    @Test
    fun `print array sequence - 1 element`() {
        // Prepare
        val variable = listOf(3.1415926.num).toSeq().toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ 3.1415926 }"
    }

    @Test
    fun `print array sequence - empty`() {
        // Prepare
        val variable = Var.SeqVar(Seq(sequenceOf(), 0))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{  }"
    }
}
