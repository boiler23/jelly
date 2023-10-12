package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.eval.EvalError
import com.ilyabogdanovich.jelly.jcc.core.eval.Seq
import com.ilyabogdanovich.jelly.jcc.core.eval.Var
import com.ilyabogdanovich.jelly.jcc.core.eval.map
import com.ilyabogdanovich.jelly.jcc.core.eval.num
import com.ilyabogdanovich.jelly.jcc.core.eval.parallelMap
import com.ilyabogdanovich.jelly.jcc.core.eval.parallelReduce
import com.ilyabogdanovich.jelly.jcc.core.eval.reduce
import com.ilyabogdanovich.jelly.jcc.core.eval.toSeq
import com.ilyabogdanovich.jelly.jcc.core.eval.toVar
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Test for [Seq]
 *
 * @author Ilya Bogdanovich on 10.10.2023
 */
class SeqTest {
    @Test
    fun `list to seq`() {
        // Prepare
        val list = listOf(Var.NumVar(1.num), Var.NumVar(2.5.num))

        // Do
        val result = list.toSeq()

        // Check
        result shouldBe Seq.Array(list)
    }

    @Test
    fun `map array without errors`() = runTest {
        // Prepare

        // Do
        val result = listOf(1.toVar(), 2.toVar(), 3.toVar()).toSeq().map {
            it as Var.NumVar
            Var.NumVar(it.v + 1.num).asRight()
        }

        // Check
        result shouldBe listOf(2.toVar(), 3.toVar(), 4.toVar()).toSeq().asRight()
    }

    @Test
    fun `map array with errors`() = runTest {
        // Prepare
        val error = mockk<EvalError>()

        // Do
        val result = listOf(1.toVar(), 2.toVar(), 3.toVar()).toSeq().map { error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `map empty array`() = runTest {
        // Prepare

        // Do
        val result = listOf<Var>().toSeq().map {
            it as Var.NumVar
            Var.NumVar(it.v + 1.num).asRight()
        }

        // Check
        result shouldBe listOf<Var>().toSeq().asRight()
    }

    @Test
    fun `map bounds without errors`() = runTest {
        // Prepare

        // Do
        val result = Seq.Bounds(1, 3).map {
            it as Var.NumVar
            Var.NumVar(it.v + 1.num).asRight()
        }

        // Check
        result shouldBe listOf(2.toVar(), 3.toVar(), 4.toVar()).toSeq().asRight()
    }

    @Test
    fun `map bounds with errors`() = runTest {
        // Prepare
        val error = mockk<EvalError>()

        // Do
        val result = Seq.Bounds(1, 3).map { error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `parallel map array without errors`() = runTest {
        // Prepare

        // Do
        val result = listOf(1.toVar(), 2.toVar(), 3.toVar()).toSeq().parallelMap {
            it as Var.NumVar
            Var.NumVar(it.v + 1.num).asRight()
        }

        // Check
        result shouldBe listOf(2.toVar(), 3.toVar(), 4.toVar()).toSeq().asRight()
    }

    @Test
    fun `parallel map array with errors`() = runTest {
        // Prepare
        val error = mockk<EvalError>()

        // Do
        val result = listOf(1.toVar(), 2.toVar(), 3.toVar()).toSeq().parallelMap { error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `parallel map empty array`() = runTest {
        // Prepare

        // Do
        val result = listOf<Var>().toSeq().parallelMap {
            it as Var.NumVar
            Var.NumVar(it.v + 1.num).asRight()
        }

        // Check
        result shouldBe listOf<Var>().toSeq().asRight()
    }

    @Test
    fun `parallel map bounds without errors`() = runTest {
        // Prepare

        // Do
        val result = Seq.Bounds(1, 3).parallelMap {
            it as Var.NumVar
            Var.NumVar(it.v + 1.num).asRight()
        }

        // Check
        result shouldBe listOf(2.toVar(), 3.toVar(), 4.toVar()).toSeq().asRight()
    }

    @Test
    fun `parallel map bounds with errors`() = runTest {
        // Prepare
        val error = mockk<EvalError>()

        // Do
        val result = Seq.Bounds(1, 3).parallelMap { error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `reduce empty array`() = runTest {
        // Prepare

        // Do
        val result = listOf<Var>().toSeq().reduce(0.toVar()) { acc, n ->
            acc as Var.NumVar
            n as Var.NumVar
            Var.NumVar(acc.v + n.v).asRight()
        }

        // Check
        result shouldBe 0.toVar().asRight()
    }

    @Test
    fun `reduce array without errors`() = runTest {
        // Prepare

        // Do
        val result = listOf(1.toVar(), 2.toVar(), 3.toVar()).toSeq().reduce(0.toVar()) { acc, n ->
            acc as Var.NumVar
            n as Var.NumVar
            Var.NumVar(acc.v + n.v).asRight()
        }

        // Check
        result shouldBe 6.toVar().asRight()
    }

    @Test
    fun `reduce array with errors`() = runTest {
        // Prepare
        val error = mockk<EvalError>()

        // Do
        val result = listOf(1.toVar(), 2.toVar(), 3.toVar()).toSeq()
            .reduce(0.toVar()) { _, _ -> error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `reduce bounds without errors`() = runTest {
        // Prepare

        // Do
        val result = Seq.Bounds(1, 3).reduce(0.toVar()) { acc, n ->
            acc as Var.NumVar
            n as Var.NumVar
            Var.NumVar(acc.v + n.v).asRight()
        }

        // Check
        result shouldBe 6.toVar().asRight()
    }

    @Test
    fun `reduce bounds with errors`() = runTest {
        // Prepare
        val error = mockk<EvalError>()

        // Do
        val result = Seq.Bounds(1, 3).reduce(0.toVar()) { _, _ -> error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `parallel reduce empty array`() = runTest {
        // Prepare

        // Do
        val result = listOf<Var>().toSeq().parallelReduce(0.toVar()) { acc, n ->
            acc as Var.NumVar
            n as Var.NumVar
            Var.NumVar(acc.v + n.v).asRight()
        }

        // Check
        result shouldBe 0.toVar().asRight()
    }

    @Test
    fun `parallel reduce array without errors`() = runTest {
        // Prepare

        // Do
        val result = listOf(1.toVar(), 2.toVar(), 3.toVar()).toSeq().parallelReduce(0.toVar()) { acc, n ->
            acc as Var.NumVar
            n as Var.NumVar
            Var.NumVar(acc.v + n.v).asRight()
        }

        // Check
        result shouldBe 6.toVar().asRight()
    }

    @Test
    fun `parallel reduce array with errors`() = runTest {
        // Prepare
        val error = mockk<EvalError>()

        // Do
        val result = listOf(1.toVar(), 2.toVar(), 3.toVar()).toSeq()
            .parallelReduce(0.toVar()) { _, _ -> error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `parallel reduce bounds without errors`() = runTest {
        // Prepare

        // Do
        val result = Seq.Bounds(1, 3).parallelReduce(0.toVar()) { acc, n ->
            acc as Var.NumVar
            n as Var.NumVar
            Var.NumVar(acc.v + n.v).asRight()
        }

        // Check
        result shouldBe 6.toVar().asRight()
    }

    @Test
    fun `parallel reduce bounds with errors`() = runTest {
        // Prepare
        val error = mockk<EvalError>()

        // Do
        val result = Seq.Bounds(1, 3).parallelReduce(0.toVar()) { _, _ -> error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }
}