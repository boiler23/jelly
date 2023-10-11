package com.ilyabiogdanovich.jelly.jcc.print

import com.ilyabiogdanovich.jelly.jcc.eval.Seq
import com.ilyabiogdanovich.jelly.jcc.eval.Var
import com.ilyabiogdanovich.jelly.jcc.eval.toVar
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
    fun `print bounds sequence - many elements`() {
        // Prepare
        val variable = Var.SeqVar(Seq.Bounds(1, 5))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ 1, 2, 3, 4, 5 }"
    }

    @Test
    fun `print bounds sequence - 1 element`() {
        // Prepare
        val variable = Var.SeqVar(Seq.Bounds(1, 1))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ 1 }"
    }

    @Test
    fun `print bounds sequence - wrong`() {
        // Prepare
        val variable = Var.SeqVar(Seq.Bounds(5, 1))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{  }"
    }

    @Test
    fun `print array sequence - many elements`() {
        // Prepare
        val variable = listOf(1.toVar(), 2.0.toVar(), 3.toVar(), 4.0.toVar(), 5.0.toVar()).toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ 1, 2, 3, 4, 5 }"
    }

    @Test
    fun `print array sequence - 1 element`() {
        // Prepare
        val variable = listOf(3.1415926.toVar()).toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ 3.1415926 }"
    }

    @Test
    fun `print array sequence - empty`() {
        // Prepare
        val variable = Var.SeqVar(Seq.Array(listOf()))

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{  }"
    }

    @Test
    fun `print multi-array sequence - many elements`() {
        // Prepare
        val variable = listOf(
            1.toVar(), listOf(2.0.toVar(), listOf(3.toVar()).toVar()).toVar(), 4.0.toVar(), 5.0.toVar()
        ).toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ 1, { 2, { 3 } }, 4, 5 }"
    }

    @Test
    fun `print multi-array sequence - 1 element`() {
        // Prepare
        val variable = listOf(listOf(3.1415926.toVar()).toVar()).toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ { 3.1415926 } }"
    }

    @Test
    fun `print multi-array sequence - empty`() {
        // Prepare
        val variable = listOf(listOf<Var>().toVar()).toVar()

        // Do
        val result = printer.print(variable)

        // Check
        result shouldBe "{ {  } }"
    }
}
