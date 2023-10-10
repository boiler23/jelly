package com.ilyabiogdanovich.jelly.jcc.eval

import com.ilyabogdanovich.jelly.jcc.JccParser.ExpressionContext
import com.ilyabogdanovich.jelly.jcc.JccParser.IdentifierContext
import com.ilyabogdanovich.jelly.jcc.JccParser.MapContext
import com.ilyabogdanovich.jelly.jcc.JccParser.NumberContext
import com.ilyabogdanovich.jelly.jcc.JccParser.ReduceContext
import com.ilyabogdanovich.jelly.jcc.JccParser.SequenceContext
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.antlr.v4.runtime.Token
import org.junit.Test

/**
 * Test for [ExpressionEvaluator]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class ExpressionEvaluatorTest {
    private val evalContext = EvalContext()
    private val evaluator = ExpressionEvaluator()

    @Test
    fun `evaluate integer`() {
        // Prepare
        val numberContext = mockk<NumberContext> {
            every { text } returns "123"
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns numberContext
            every { getRuleContext(IdentifierContext::class.java, 0) } returns null
        }

        // Do
        val result = evaluator.evaluateExpression(evalContext, parserContext)

        // Check
        result shouldBe Var.NumVar(Num.Integer(123)).asRight()
    }

    @Test
    fun `evaluate double`() {
        // Prepare
        val numberContext = mockk<NumberContext> {
            every { text } returns "123.456"
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns numberContext
            every { getRuleContext(IdentifierContext::class.java, 0) } returns null
        }

        // Do
        val result = evaluator.evaluateExpression(evalContext, parserContext)

        // Check
        result shouldBe Var.NumVar(Num.Real(123.456)).asRight()
    }

    @Test
    fun `evaluate wrong number`() {
        // Prepare
        val startToken = mockk<Token> {
            every { line } returns 5
            every { charPositionInLine } returns 11
        }
        val numberContext = mockk<NumberContext> {
            every { text } returns "12i+56"
            every { getStart() } returns startToken
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns numberContext
            every { getRuleContext(IdentifierContext::class.java, 0) } returns null
        }

        // Do
        val result = evaluator.evaluateExpression(evalContext, parserContext)

        // Check
        result shouldBe EvalError(
            line = 5,
            positionInLine = 11,
            expression = "12i+56",
            type = EvalError.Type.InvalidNumber
        ).asLeft()
    }

    @Test
    fun `evaluate existing identifier`() {
        // Prepare
        val identifier = mockk<IdentifierContext> {
            every { text } returns "id"
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns null
            every { getRuleContext(IdentifierContext::class.java, 0) } returns identifier
        }
        val variable = mockk<Var>()
        evalContext.push("id", variable)

        // Do
        val result = evaluator.evaluateExpression(evalContext, parserContext)

        // Check
        result shouldBe variable.asRight()
    }

    @Test
    fun `evaluate non-existing identifier`() {
        // Prepare
        val startToken = mockk<Token> {
            every { line } returns 3
            every { charPositionInLine } returns 14
        }
        val identifier = mockk<IdentifierContext> {
            every { text } returns "id"
            every { getStart() } returns startToken
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns null
            every { getRuleContext(IdentifierContext::class.java, 0) } returns identifier
        }

        // Do
        val result = evaluator.evaluateExpression(evalContext, parserContext)

        // Check
        result shouldBe EvalError(
            line = 3,
            positionInLine = 14,
            expression = "id",
            type = EvalError.Type.UndeclaredVariable
        ).asLeft()
    }

    @Test
    fun `evaluate unsupported expression`() {
        // Prepare
        val startToken = mockk<Token> {
            every { line } returns 2
            every { charPositionInLine } returns 12
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns null
            every { getRuleContext(IdentifierContext::class.java, 0) } returns null
            every { getRuleContext(SequenceContext::class.java, 0) } returns null
            every { getRuleContext(MapContext::class.java, 0) } returns null
            every { getRuleContext(ReduceContext::class.java, 0) } returns null
            every { text } returns "expr_text"
            every { getStart() } returns startToken
        }

        // Do
        val result = evaluator.evaluateExpression(evalContext, parserContext)

        // Check
        result shouldBe EvalError(
            line = 2,
            positionInLine = 12,
            expression = "expr_text",
            type = EvalError.Type.UnsupportedExpression
        ).asLeft()
    }
}
