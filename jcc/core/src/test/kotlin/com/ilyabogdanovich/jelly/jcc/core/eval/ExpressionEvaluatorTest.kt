package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.Error
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser.ExpressionContext
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser.IdentifierContext
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser.MapContext
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser.NumberContext
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser.ReduceContext
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser.SequenceContext
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.antlr.v4.runtime.Token
import org.junit.Test

/**
 * Test for [ExpressionEvaluator]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class ExpressionEvaluatorTest {
    private val evaluator = ExpressionEvaluator()

    @Test
    fun `evaluate integer`() = runTest {
        // Prepare
        val numberContext = mockk<NumberContext> {
            every { text } returns "123"
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns numberContext
        }

        // Do
        val result = evaluator.evaluateExpression(EvalContext(), parserContext)

        // Check
        result shouldBe Var.NumVar(Num.Integer(123)).asRight()
    }

    @Test
    fun `evaluate double`() = runTest {
        // Prepare
        val numberContext = mockk<NumberContext> {
            every { text } returns "123.456"
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns numberContext
        }

        // Do
        val result = evaluator.evaluateExpression(EvalContext(), parserContext)

        // Check
        result shouldBe Var.NumVar(Num.Real(123.456)).asRight()
    }

    @Test
    fun `evaluate wrong number`() = runTest {
        // Prepare
        val startToken = mockk<Token> {
            every { line } returns 3
            every { charPositionInLine } returns 14
        }
        val stopToken = mockk<Token> {
            every { line } returns 4
            every { charPositionInLine } returns 25
        }
        val numberContext = mockk<NumberContext> {
            every { text } returns "12i+56"
            every { getStart() } returns startToken
            every { getStop() } returns stopToken
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns numberContext
        }

        // Do
        val result = evaluator.evaluateExpression(EvalContext(), parserContext)

        // Check
        result shouldBe Error(
            start = Error.TokenPosition(line = 3, positionInLine = 14),
            stop = Error.TokenPosition(line = 4, positionInLine = 25),
            expression = "12i+56",
            type = Error.Type.InvalidNumber
        ).asLeft()
    }

    @Test
    fun `evaluate existing identifier`() = runTest {
        // Prepare
        val identifier = mockk<IdentifierContext> {
            every { text } returns "id"
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns null
            every { getRuleContext(IdentifierContext::class.java, 0) } returns identifier
        }
        val variable = mockk<Var>()

        // Do
        val result = evaluator.evaluateExpression(EvalContext(mapOf("id" to variable)), parserContext)

        // Check
        result shouldBe variable.asRight()
    }

    @Test
    fun `evaluate non-existing identifier`() = runTest {
        // Prepare
        val startToken = mockk<Token> {
            every { line } returns 3
            every { charPositionInLine } returns 14
        }
        val stopToken = mockk<Token> {
            every { line } returns 4
            every { charPositionInLine } returns 25
        }
        val identifier = mockk<IdentifierContext> {
            every { text } returns "id"
            every { getStart() } returns startToken
            every { getStop() } returns stopToken
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns null
            every { getRuleContext(IdentifierContext::class.java, 0) } returns identifier
        }

        // Do
        val result = evaluator.evaluateExpression(EvalContext(), parserContext)

        // Check
        result shouldBe Error(
            start = Error.TokenPosition(line = 3, positionInLine = 14),
            stop = Error.TokenPosition(line = 4, positionInLine = 25),
            expression = "id",
            type = Error.Type.UndeclaredVariable
        ).asLeft()
    }

    @Test
    fun `evaluate sequence`() = runTest {
        // Prepare
        val sequence = mockk<SequenceContext> {
            every { expression(0) } returns mockk {
                every { getRuleContext(NumberContext::class.java, 0) } returns mockk {
                    every { text } returns "1"
                }
            }
            every { expression(1) } returns mockk {
                every { getRuleContext(NumberContext::class.java, 0) } returns mockk {
                    every { text } returns "2"
                }
            }
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns null
            every { getRuleContext(IdentifierContext::class.java, 0) } returns null
            every { getRuleContext(SequenceContext::class.java, 0) } returns sequence
        }

        // Do
        val result = evaluator.evaluateExpression(EvalContext(), parserContext)

        // Check
        result shouldBe Var.SeqVar(Seq.fromBounds(1, 2)).asRight()
    }

    @Test
    fun `evaluate map`() = runTest {
        // Prepare
        val map = mockk<MapContext> {
            every { expression() } returns mockk {
                every { getRuleContext(NumberContext::class.java, 0) } returns null
                every { getRuleContext(IdentifierContext::class.java, 0) } returns null
                every { getRuleContext(SequenceContext::class.java, 0) } returns mockk {
                    every { expression(0) } returns mockk {
                        every { getRuleContext(NumberContext::class.java, 0) } returns mockk {
                            every { text } returns "1"
                        }
                    }
                    every { expression(1) } returns mockk {
                        every { getRuleContext(NumberContext::class.java, 0) } returns mockk {
                            every { text } returns "2"
                        }
                    }
                }
            }
            every { lambda1() } returns mockk {
                every { identifier() } returns mockk {
                    every { NAME() } returns mockk {
                        every { text } returns "id"
                    }
                }
                every { expression() } returns mockk {
                    every { getRuleContext(NumberContext::class.java, 0) } returns mockk {
                        every { text } returns "1"
                    }
                }
            }
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns null
            every { getRuleContext(IdentifierContext::class.java, 0) } returns null
            every { getRuleContext(SequenceContext::class.java, 0) } returns null
            every { getRuleContext(MapContext::class.java, 0) } returns map
        }

        // Do
        val result = evaluator.evaluateExpression(EvalContext(), parserContext)

        // Check
        result shouldBe Var.SeqVar(listOf(1.num, 1.num).toSeq()).asRight()
    }

    @Test
    fun `evaluate reduce`() = runTest {
        // Prepare
        val reduce = mockk<ReduceContext> {
            every { expression(0) } returns mockk {
                every { getRuleContext(NumberContext::class.java, 0) } returns null
                every { getRuleContext(IdentifierContext::class.java, 0) } returns null
                every { getRuleContext(SequenceContext::class.java, 0) } returns mockk {
                    every { expression(0) } returns mockk {
                        every { getRuleContext(NumberContext::class.java, 0) } returns mockk {
                            every { text } returns "1"
                        }
                    }
                    every { expression(1) } returns mockk {
                        every { getRuleContext(NumberContext::class.java, 0) } returns mockk {
                            every { text } returns "2"
                        }
                    }
                }
            }
            every { expression(1) } returns mockk {
                every { getRuleContext(NumberContext::class.java, 0) } returns mockk {
                    every { text } returns "0"
                }
            }
            every { lambda2() } returns mockk {
                every { identifier(0) } returns mockk {
                    every { NAME() } returns mockk {
                        every { text } returns "acc"
                    }
                }
                every { identifier(1) } returns mockk {
                    every { NAME() } returns mockk {
                        every { text } returns "n"
                    }
                }
                every { expression() } returns mockk {
                    every { getRuleContext(NumberContext::class.java, 0) } returns mockk {
                        every { text } returns "1"
                    }
                }
            }
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns null
            every { getRuleContext(IdentifierContext::class.java, 0) } returns null
            every { getRuleContext(SequenceContext::class.java, 0) } returns null
            every { getRuleContext(MapContext::class.java, 0) } returns null
            every { getRuleContext(ReduceContext::class.java, 0) } returns reduce
        }

        // Do
        val result = evaluator.evaluateExpression(EvalContext(), parserContext)

        // Check
        result shouldBe 1.toVar().asRight()
    }

    @Test
    fun `evaluate unsupported expression`() = runTest {
        // Prepare
        val startToken = mockk<Token> {
            every { line } returns 2
            every { charPositionInLine } returns 12
        }
        val stopToken = mockk<Token> {
            every { line } returns 3
            every { charPositionInLine } returns 14
        }
        val parserContext = mockk<ExpressionContext> {
            every { getRuleContext(NumberContext::class.java, 0) } returns null
            every { getRuleContext(IdentifierContext::class.java, 0) } returns null
            every { getRuleContext(SequenceContext::class.java, 0) } returns null
            every { getRuleContext(MapContext::class.java, 0) } returns null
            every { getRuleContext(ReduceContext::class.java, 0) } returns null
            every { text } returns "expr_text"
            every { getStart() } returns startToken
            every { getStop() } returns stopToken
        }

        // Do
        val result = evaluator.evaluateExpression(EvalContext(), parserContext)

        // Check
        result shouldBe Error(
            start = Error.TokenPosition(line = 2, positionInLine = 12),
            stop = Error.TokenPosition(line = 3, positionInLine = 14),
            expression = "expr_text",
            type = Error.Type.UnsupportedExpression
        ).asLeft()
    }
}
