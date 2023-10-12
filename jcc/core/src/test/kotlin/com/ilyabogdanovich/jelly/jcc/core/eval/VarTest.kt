package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.eval.Num
import com.ilyabogdanovich.jelly.jcc.core.eval.Seq
import com.ilyabogdanovich.jelly.jcc.core.eval.Var
import com.ilyabogdanovich.jelly.jcc.core.eval.toVar
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
    fun `var list to sequence var`() {
        // Prepare
        val list = listOf(1.toVar(), 2.toVar())

        // Do
        val result = list.toVar()

        // Check
        result shouldBe Var.SeqVar(Seq.Array(list))
    }
}
