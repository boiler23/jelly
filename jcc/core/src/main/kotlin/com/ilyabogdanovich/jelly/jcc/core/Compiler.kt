package com.ilyabogdanovich.jelly.jcc.core

import com.ilyabogdanovich.jelly.jcc.core.antlr.JccLexer
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser
import com.ilyabogdanovich.jelly.jcc.core.eval.AssignmentEvaluator
import com.ilyabogdanovich.jelly.jcc.core.eval.EvalContext
import com.ilyabogdanovich.jelly.jcc.core.eval.EvalError
import com.ilyabogdanovich.jelly.jcc.core.eval.ExpressionEvaluator
import com.ilyabogdanovich.jelly.jcc.core.eval.PrintEvaluator
import com.ilyabogdanovich.jelly.jcc.core.eval.toError
import com.ilyabogdanovich.jelly.jcc.core.print.VarPrinter
import com.ilyabogdanovich.jelly.utils.Either
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

/**
 * Compiler for our language.
 *
 * @author Ilya Bogdanovich on 08.10.2023
 */
class Compiler {
    private class ResultListener {
        val errors = mutableListOf<EvalError>()
        val output = StringBuilder()
        private val expressionEvaluator = ExpressionEvaluator()
        private val assignmentEvaluator = AssignmentEvaluator(expressionEvaluator)
        private val varPrinter = VarPrinter()
        private val printEvaluator = PrintEvaluator()

        suspend fun assignment(evalContext: EvalContext, ctx: JccParser.AssignmentContext): EvalContext {
            return when (val newEvalContextOrError = assignmentEvaluator.evaluate(evalContext, ctx)) {
                is Either.Left -> evalContext.also { errors.add(newEvalContextOrError.value) }
                is Either.Right -> newEvalContextOrError.value
            }
        }

        suspend fun out(evalContext: EvalContext, ctx: JccParser.OutputContext) {
            when (val evaluated = expressionEvaluator.evaluateExpression(evalContext, ctx.expression())) {
                is Either.Left -> errors.add(evaluated.value)
                is Either.Right -> output.append(varPrinter.print(evaluated.value))
            }
        }

        fun print(ctx: JccParser.PrintingContext) =
            printEvaluator.evaluate(ctx, output)

        fun expression(ctx: JccParser.ExpressionContext) =
            errors.add(ctx.toError(EvalError.Type.TopLevelExpressionsUnsupported))
    }

    data class Result(
        val output: String,
        val errors: List<EvalError>,
    )

    suspend fun compile(src: String): Result {
        val resultListener = ResultListener()
        val syntaxErrorListener = SyntaxErrorListener()

        val lexer = JccLexer(CharStreams.fromString(src))
        lexer.addErrorListener(syntaxErrorListener)
        val parser = JccParser(CommonTokenStream(lexer))
        parser.addErrorListener(syntaxErrorListener)
        val tree = parser.program()

        var evalContext = EvalContext()
        tree.statement().forEach { statement ->
            statement.output()?.let { ctx ->
                resultListener.out(evalContext, ctx)
            }
            statement.printing()?.let { ctx ->
                resultListener.print(ctx)
            }
            statement.assignment()?.let { ctx ->
                evalContext = resultListener.assignment(evalContext, ctx)
            }
            statement.expression()?.let { ctx ->
                resultListener.expression(ctx)
            }
        }

        return Result(
            output = resultListener.output.toString(),
            errors = (syntaxErrorListener.errors + resultListener.errors)
                .sortedWith { o1, o2 ->
                    val compareLines = o1.start.line.compareTo(o2.start.line)
                    if (compareLines == 0) {
                        o1.start.positionInLine.compareTo(o2.start.positionInLine)
                    } else {
                        compareLines
                    }
                },
        )
    }
}
