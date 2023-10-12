package com.ilyabogdanovich.jelly.jcc.eval

import com.ilyabogdanovich.jelly.jcc.JccParser
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.antlr.v4.runtime.tree.TerminalNode
import org.junit.Test

/**
 * Test for [PrintEvaluator]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class PrintEvaluatorTest {
    private val evaluator = PrintEvaluator()

    @Test
    fun `evaluate - NO string`() {
        // Prepare
        val ctx = mockk<JccParser.PrintingContext> {
            every { STRING() } returns null
        }

        // Do
        val result = evaluator.evaluate(ctx)

        // Check
        result shouldBe null
    }

    @Test
    fun `evaluate - HAS string NO text`() {
        // Prepare
        val terminalNode = mockk<TerminalNode> {
            every { text } returns null
        }
        val ctx = mockk<JccParser.PrintingContext> {
            every { STRING() } returns terminalNode
        }

        // Do
        val result = evaluator.evaluate(ctx)

        // Check
        result shouldBe null
    }

    @Test
    fun `evaluate - HAS string HAS text NO index`() {
        // Prepare
        val terminalNode = mockk<TerminalNode> {
            every { text } returns "<EMPTY STRING>"
            every { symbol } returns mockk {
                every { tokenIndex } returns -1
            }
        }
        val ctx = mockk<JccParser.PrintingContext> {
            every { STRING() } returns terminalNode
        }

        // Do
        val result = evaluator.evaluate(ctx)

        // Check
        result shouldBe null
    }

    @Test
    fun `evaluate - HAS string HAS text HAS index NO quotes`() {
        // Prepare
        val terminalNode = mockk<TerminalNode> {
            every { text } returns "text"
            every { symbol } returns mockk {
                every { tokenIndex } returns 1
            }
        }
        val ctx = mockk<JccParser.PrintingContext> {
            every { STRING() } returns terminalNode
        }

        // Do
        val result = evaluator.evaluate(ctx)

        // Check
        result shouldBe "text"
    }

    @Test
    fun `evaluate - HAS string HAS text HAS index HAS quotes`() {
        // Prepare
        val terminalNode = mockk<TerminalNode> {
            every { text } returns "\"text\""
            every { symbol } returns mockk {
                every { tokenIndex } returns 1
            }
        }
        val ctx = mockk<JccParser.PrintingContext> {
            every { STRING() } returns terminalNode
        }

        // Do
        val result = evaluator.evaluate(ctx)

        // Check
        result shouldBe "text"
    }

    @Test
    fun `evaluate - HAS string HAS text HAS index HAS quotes HAS quotes inside`() {
        // Prepare
        val terminalNode = mockk<TerminalNode> {
            every { text } returns """"te\"xt""""
            every { symbol } returns mockk {
                every { tokenIndex } returns 1
            }
        }
        val ctx = mockk<JccParser.PrintingContext> {
            every { STRING() } returns terminalNode
        }

        // Do
        val result = evaluator.evaluate(ctx)

        // Check
        result shouldBe """te"xt"""
    }

    @Test
    fun `evaluate - HAS string HAS text HAS index HAS quotes HAS quotes inside at the end`() {
        // Prepare
        val terminalNode = mockk<TerminalNode> {
            every { text } returns "\"text\\\"\""
            every { symbol } returns mockk {
                every { tokenIndex } returns 1
            }
        }
        val ctx = mockk<JccParser.PrintingContext> {
            every { STRING() } returns terminalNode
        }

        // Do
        val result = evaluator.evaluate(ctx)

        // Check
        result shouldBe "text\""
    }

    @Test
    fun `evaluate - HAS string HAS text HAS index HAS quotes HAS slash at the end`() {
        // Prepare
        val terminalNode = mockk<TerminalNode> {
            every { text } returns "\"text\\\""
            every { symbol } returns mockk {
                every { tokenIndex } returns 1
            }
        }
        val ctx = mockk<JccParser.PrintingContext> {
            every { STRING() } returns terminalNode
        }

        // Do
        val result = evaluator.evaluate(ctx)

        // Check
        result shouldBe "text\\"
    }
}
