package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.Error
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
        val list = listOf(1.num, 2.5.num)

        // Do
        val result = list.toSeq()

        // Check
        result shouldBe Seq(sequenceOf(1.num, 2.5.num), 2)
    }

    @Test
    fun `map array without errors`() = runTest {
        // Prepare

        // Do
        val result = listOf(1.num, 2.num, 3.num).toSeq().map { (it + 1.num).asRight() }

        // Check
        result shouldBe listOf(2.num, 3.num, 4.num).toSeq().asRight()
    }

    @Test
    fun `map array with errors`() = runTest {
        // Prepare
        val error = mockk<Error>()

        // Do
        val result = listOf(1.num, 2.num, 3.num).toSeq().map { error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `map empty array`() = runTest {
        // Prepare

        // Do
        val result = listOf<Num>().toSeq().map { (it + 1.num).asRight() }

        // Check
        result shouldBe listOf<Num>().toSeq().asRight()
    }

    @Test
    fun `map bounds without errors`() = runTest {
        // Prepare

        // Do
        val result = Seq.fromBounds(1, 3).map { (it + 1.num).asRight() }

        // Check
        result shouldBe listOf(2.num, 3.num, 4.num).toSeq().asRight()
    }

    @Test
    fun `map bounds with errors`() = runTest {
        // Prepare
        val error = mockk<Error>()

        // Do
        val result = Seq.fromBounds(1, 3).map { error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `parallel map array without errors`() = runTest {
        // Prepare

        // Do
        val result = listOf(1.num, 2.num, 3.num).toSeq().parallelMap { (it + 1.num).asRight() }

        // Check
        result shouldBe listOf(2.num, 3.num, 4.num).toSeq().asRight()
    }

    @Test
    fun `parallel map array with errors`() = runTest {
        // Prepare
        val error = mockk<Error>()

        // Do
        val result = listOf(1.num, 2.num, 3.num).toSeq().parallelMap { error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `parallel map empty array`() = runTest {
        // Prepare

        // Do
        val result = listOf<Num>().toSeq().parallelMap { (it + 1.num).asRight() }

        // Check
        result shouldBe listOf<Num>().toSeq().asRight()
    }

    @Test
    fun `parallel map bounds without errors`() = runTest {
        // Prepare

        // Do
        val result = Seq.fromBounds(1, 3).parallelMap { (it + 1.num).asRight() }

        // Check
        result shouldBe listOf(2.num, 3.num, 4.num).toSeq().asRight()
    }

    @Test
    fun `parallel map bounds with errors`() = runTest {
        // Prepare
        val error = mockk<Error>()

        // Do
        val result = Seq.fromBounds(1, 3).parallelMap { error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `reduce empty array`() = runTest {
        // Prepare

        // Do
        val result = listOf<Num>().toSeq().reduce(0.num) { acc, n -> (acc + n).asRight() }

        // Check
        result shouldBe 0.num.asRight()
    }

    @Test
    fun `reduce array without errors`() = runTest {
        // Prepare

        // Do
        val result = listOf(1.num, 2.num, 3.num).toSeq().reduce(0.num) { acc, n -> (acc + n).asRight() }

        // Check
        result shouldBe 6.num.asRight()
    }

    @Test
    fun `reduce array with errors`() = runTest {
        // Prepare
        val error = mockk<Error>()

        // Do
        val result = listOf(1.num, 2.num, 3.num).toSeq().reduce(0.num) { _, _ -> error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `reduce bounds without errors`() = runTest {
        // Prepare

        // Do
        val result = Seq.fromBounds(1, 3).reduce(0.num) { acc, n -> (acc + n).asRight() }

        // Check
        result shouldBe 6.num.asRight()
    }

    @Test
    fun `reduce bounds with errors`() = runTest {
        // Prepare
        val error = mockk<Error>()

        // Do
        val result = Seq.fromBounds(1, 3).reduce(0.num) { _, _ -> error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `parallel reduce empty array`() = runTest {
        // Prepare

        // Do
        val result = listOf<Num>().toSeq().parallelReduce(0.num) { acc, n -> (acc + n).asRight() }

        // Check
        result shouldBe 0.num.asRight()
    }

    @Test
    fun `parallel reduce array without errors`() = runTest {
        // Prepare

        // Do
        val result = listOf(1.num, 2.num, 3.num).toSeq().parallelReduce(0.num) { acc, n -> (acc + n).asRight() }

        // Check
        result shouldBe 6.num.asRight()
    }

    @Test
    fun `parallel reduce array with errors`() = runTest {
        // Prepare
        val error = mockk<Error>()

        // Do
        val result = listOf(1.num, 2.num, 3.num).toSeq().parallelReduce(0.num) { _, _ -> error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }

    @Test
    fun `parallel reduce bounds without errors`() = runTest {
        // Prepare

        // Do
        val result = Seq.fromBounds(1, 3).parallelReduce(0.num) { acc, n -> (acc + n).asRight() }

        // Check
        result shouldBe 6.num.asRight()
    }

    @Test
    fun `parallel reduce bounds with errors`() = runTest {
        // Prepare
        val error = mockk<Error>()

        // Do
        val result = Seq.fromBounds(1, 3).parallelReduce(0.num) { _, _ -> error.asLeft() }

        // Check
        result shouldBe error.asLeft()
    }
}
