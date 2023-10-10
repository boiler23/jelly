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
import io.mockk.verifySequence
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
    fun `evaluate sequence`() {
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
        val result = evaluator.evaluateExpression(evalContext, parserContext)

        // Check
        result shouldBe Var.SeqVar(Seq.Bounds(1, 2)).asRight()
    }

    @Test
    fun `evaluate map`() {
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
        val evalContext = mockk<EvalContext>()
        every { evalContext.push("id", 1.toVar()) } returns true
        every { evalContext.push("id", 2.toVar()) } returns true
        every { evalContext.pop("id") } returns Unit

        // Do
        val result = evaluator.evaluateExpression(evalContext, parserContext)

        // Check
        verifySequence {
            evalContext.push("id", 1.toVar())
            evalContext.pop("id")
            evalContext.push("id", 2.toVar())
            evalContext.pop("id")
        }
        result shouldBe listOf(1.toVar(), 1.toVar()).toVar().asRight()
    }

    @Test
    fun `evaluate reduce`() {
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
        every { evalContext.push(any(), any()) } returns true
        every { evalContext.pop(any()) } returns Unit

        // Do
        val result = evaluator.evaluateExpression(evalContext, parserContext)

        // Check
        verifySequence {
            evalContext.push("acc", 0.toVar())
            evalContext.push("n", 1.toVar())
            evalContext.pop("n")
            evalContext.pop("acc")
            evalContext.push("acc", 1.toVar())
            evalContext.push("n", 2.toVar())
            evalContext.pop("n")
            evalContext.pop("acc")
        }
        result shouldBe 1.toVar().asRight()
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
