package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.Error
import com.ilyabogdanovich.jelly.jcc.core.ExecutionResult
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser
import com.ilyabogdanovich.jelly.jcc.core.print.VarPrinter
import com.ilyabogdanovich.jelly.jcc.core.toError
import com.ilyabogdanovich.jelly.utils.Either

/**
 * Helper to evaluate program statements.
 * This is the main entry point for the evaluation logic.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
internal class ProgramEvaluator(
    private val expressionEvaluator: ExpressionEvaluator,
    private val printEvaluator: PrintEvaluator,
    private val assignmentEvaluator: AssignmentEvaluator,
    private val varPrinter: VarPrinter
) {
    private suspend fun StringBuilder.out(ctx: JccParser.OutputContext, evalContext: EvalContext) =
        when (val evaluated = expressionEvaluator.evaluateExpression(evalContext, ctx.expression())) {
            is Either.Left -> listOf(evaluated.value)
            is Either.Right -> listOf<Error>().also { append(varPrinter.print(evaluated.value)) }
        }

    private fun StringBuilder.printing(ctx: JccParser.PrintingContext) =
        printEvaluator.evaluate(ctx, this)

    private suspend fun assignment(ctx: JccParser.AssignmentContext, evalContext: EvalContext) =
        when (val newEvalContextOrError = assignmentEvaluator.evaluate(evalContext, ctx)) {
            is Either.Left -> evalContext to listOf(newEvalContextOrError.value)
            is Either.Right -> newEvalContextOrError.value to listOf()
        }

    private fun topLevelExpression(ctx: JccParser.ExpressionContext) =
        listOf(ctx.toError(Error.Type.TopLevelExpressionsUnsupported))

    suspend fun evaluate(ctx: JccParser.ProgramContext): ExecutionResult {
        val output = StringBuilder()
        val errors = mutableListOf<Error>()
        var evalContext = EvalContext()

        ctx.statement().forEach { statement ->
            statement.output()?.let {
                val outErrors = output.out(it, evalContext)
                errors.addAll(outErrors)
            }

            statement.printing()?.let {
                output.printing(it)
            }

            statement.assignment()?.let {
                val (newEvalContext, assignmentErrors) = assignment(it, evalContext)
                evalContext = newEvalContext
                errors.addAll(assignmentErrors)
            }

            statement.expression()?.let {
                val topLevelExpressionErrors = topLevelExpression(it)
                errors.addAll(topLevelExpressionErrors)
            }
        }

        return ExecutionResult(output = output.toString(), errors = errors)
    }
}
