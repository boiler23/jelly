package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
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
    fun `evaluate - no expression`() = runTest {
        // Prepare
        val startToken = mockk<Token> {
            every { line } returns 3
            every { charPositionInLine } returns 14
        }
        val stopToken = mockk<Token> {
            every { line } returns 4
            every { charPositionInLine } returns 25
        }
        val parseContext = mockk<JccParser.AssignmentContext> {
            every { expression() } returns null
            every { text } returns "var n ="
            every { getStart() } returns startToken
            every { getStop() } returns stopToken
        }

        // Do
        val result = assignmentEvaluator.evaluate(evalContext, parseContext)

        // Check
        result shouldBe EvalError(
            start = EvalError.TokenPosition(line = 3, positionInLine = 14),
            stop = EvalError.TokenPosition(line = 4, positionInLine = 25),
            expression = "var n =",
            type = EvalError.Type.MissingVariableAssignment,
        ).asLeft()
    }

    @Test
    fun `evaluate - expression evaluated`() = runTest {
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
        coEvery { expressionEvaluator.evaluateExpression(evalContext, expression) } returns variable.asRight()

        // Do
        val result = assignmentEvaluator.evaluate(evalContext, parseContext)

        // Check
        result shouldBe ("id" to variable).asRight()
    }

    @Test
    fun `evaluate - expression not evaluated`() = runTest {
        // Prepare
        val expression = mockk<JccParser.ExpressionContext>()
        val parseContext = mockk<JccParser.AssignmentContext> {
            every { expression() } returns expression
        }
        val error = mockk<EvalError>()
        coEvery { expressionEvaluator.evaluateExpression(evalContext, expression) } returns error.asLeft()

        // Do
        val result = assignmentEvaluator.evaluate(evalContext, parseContext)

        // Check
        result shouldBe error.asLeft()
    }
}
