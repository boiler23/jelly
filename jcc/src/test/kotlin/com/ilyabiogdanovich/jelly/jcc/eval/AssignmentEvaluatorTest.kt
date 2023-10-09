package com.ilyabiogdanovich.jelly.jcc.eval

import com.ilyabogdanovich.jelly.jcc.JccParser
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode
import org.junit.Test

/**
 * Test for [AssignmentEvaluator]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class AssignmentEvaluatorTest {
    private val expressionEvaluator = mockk<ExpressionEvaluator>()
    private val evalContext = mockk<EvalContext>()
    private val assignmentEvaluator = AssignmentEvaluator(expressionEvaluator)

    @Test
    fun `evaluate - no expression`() {
        // Prepare
        val startToken = mockk<Token> {
            every { line } returns 3
            every { charPositionInLine } returns 14
        }
        val parseContext = mockk<JccParser.AssignmentContext> {
            every { expression() } returns null
            every { text } returns "var n ="
            every { getStart() } returns startToken
        }

        // Do
        val result = assignmentEvaluator.evaluate(evalContext, parseContext)

        // Check
        result shouldBe EvalError(
            line = 3,
            positionInLine = 14,
            expression = "var n =",
            type = EvalError.Type.MissingVariableAssignment,
        ).asLeft()
    }

    @Test
    fun `evaluate - expression evaluated`() {
        // Prepare
        val name = mockk<TerminalNode> {
            every { text } returns "id"
        }
        val expression = mockk<JccParser.ExpressionContext>()
        val parseContext = mockk<JccParser.AssignmentContext> {
            every { expression() } returns expression
            every { NAME() } returns name
        }
        val variable = mockk<Var>()
        every { expressionEvaluator.evaluateExpression(evalContext, expression) } returns variable.asRight()

        // Do
        val result = assignmentEvaluator.evaluate(evalContext, parseContext)

        // Check
        result shouldBe ("id" to variable).asRight()
    }

    @Test
    fun `evaluate - expression not evaluated`() {
        // Prepare
        val expression = mockk<JccParser.ExpressionContext>()
        val parseContext = mockk<JccParser.AssignmentContext> {
            every { expression() } returns expression
        }
        val error = mockk<EvalError>()
        every { expressionEvaluator.evaluateExpression(evalContext, expression) } returns error.asLeft()

        // Do
        val result = assignmentEvaluator.evaluate(evalContext, parseContext)

        // Check
        result shouldBe error.asLeft()
    }
}
