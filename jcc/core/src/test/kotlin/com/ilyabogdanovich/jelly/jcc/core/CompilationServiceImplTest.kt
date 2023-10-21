package com.ilyabogdanovich.jelly.jcc.core

import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser
import com.ilyabogdanovich.jelly.jcc.core.eval.ProgramEvaluator
import com.ilyabogdanovich.jelly.jcc.core.parse.ParseResult
import com.ilyabogdanovich.jelly.jcc.core.parse.ParseTreeBuilder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Test for [CompilationServiceImpl]
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
class CompilationServiceImplTest {
    private val parseTreeBuilder = mockk<ParseTreeBuilder>()
    private val programEvaluator = mockk<ProgramEvaluator>()

    private val service = CompilationServiceImpl(parseTreeBuilder, programEvaluator)

    @Test
    fun `merge and sort errors`() = runTest {
        // Prepare
        val program = mockk<JccParser.ProgramContext>()
        coEvery { parseTreeBuilder.build("source") } returns ParseResult(
            tree = program,
            syntaxErrors = listOf(
                Error(
                    start = Error.TokenPosition(line = 5, positionInLine = 20),
                    stop = null,
                    expression = "expr 1",
                    type = Error.Type.InvalidArithmeticOperand,
                ),
                Error(
                    start = Error.TokenPosition(line = 5, positionInLine = 6),
                    stop = null,
                    expression = "expr 1",
                    type = Error.Type.InvalidArithmeticOperand,
                ),
            ),
            ruleNames = arrayOf(),
        )
        coEvery { programEvaluator.evaluate(program) } returns ExecutionResult(
            output = "output",
            errors = listOf(
                Error(
                    start = Error.TokenPosition(line = 1, positionInLine = 20),
                    stop = null,
                    expression = "expr 1",
                    type = Error.Type.InvalidArithmeticOperand,
                ),
                Error(
                    start = Error.TokenPosition(line = 8, positionInLine = 4),
                    stop = null,
                    expression = "expr 1",
                    type = Error.Type.InvalidArithmeticOperand,
                ),
            ),
        )

        // Do
        val result = service.compile("source")

        // Check
        result shouldBe ExecutionResult(
            output = "output",
            errors = listOf(
                Error(
                    start = Error.TokenPosition(line = 1, positionInLine = 20),
                    stop = null,
                    expression = "expr 1",
                    type = Error.Type.InvalidArithmeticOperand,
                ),
                Error(
                    start = Error.TokenPosition(line = 5, positionInLine = 6),
                    stop = null,
                    expression = "expr 1",
                    type = Error.Type.InvalidArithmeticOperand,
                ),
                Error(
                    start = Error.TokenPosition(line = 5, positionInLine = 20),
                    stop = null,
                    expression = "expr 1",
                    type = Error.Type.InvalidArithmeticOperand,
                ),
                Error(
                    start = Error.TokenPosition(line = 8, positionInLine = 4),
                    stop = null,
                    expression = "expr 1",
                    type = Error.Type.InvalidArithmeticOperand,
                ),
            ),
        )
    }
}
