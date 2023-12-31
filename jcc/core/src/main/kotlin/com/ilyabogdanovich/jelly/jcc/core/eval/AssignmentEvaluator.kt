package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.Error
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser
import com.ilyabogdanovich.jelly.jcc.core.toError
import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import com.ilyabogdanovich.jelly.utils.mapRight

/**
 * Evaluator for the assignment statments.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
internal class AssignmentEvaluator(private val expressionEvaluator: ExpressionEvaluator) {
    /**
     * Evaluates the assignment context into either a name-value pair or error.
     * @param evalContext evaluation context with the current variables.
     * @param assignmentContext assignment context of the parser.
     * @return either error or evaluated pair.
     */
    suspend fun evaluate(
        evalContext: EvalContext,
        assignmentContext: JccParser.AssignmentContext
    ): Either<Error, EvalContext> {
        val expression = assignmentContext.expression()
            ?: return assignmentContext.toError(Error.Type.MissingVariableAssignment).asLeft()
        val evaluated = expressionEvaluator.evaluateExpression(evalContext, expression)
            .mapRight { variable ->
                val id = assignmentContext.NAME().text
                id to variable
            }

        return when (evaluated) {
            is Either.Left -> {
                evaluated.value.asLeft()
            }
            is Either.Right -> {
                val (id, variable) = evaluated.value
                when (val result = evalContext + mapOf(id to variable)) {
                    is Either.Left ->
                        assignmentContext.toError(
                            Error.Type.VariableRedeclaration,
                            expression = assignmentContext.NAME()?.text
                        ).asLeft()
                    is Either.Right ->
                        result.value.asRight()
                }
            }
        }
    }
}
