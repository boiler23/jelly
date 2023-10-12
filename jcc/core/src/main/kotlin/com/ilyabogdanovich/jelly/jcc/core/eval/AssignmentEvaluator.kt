package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser
import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.mapRight

/**
 * Evaluator for the assignment statments.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class AssignmentEvaluator(private val expressionEvaluator: ExpressionEvaluator) {
    /**
     * Evaluates the assignment context into either a name-value pair or error.
     * @param evalContext evaluation context with the current variables.
     * @param assignmentContext assignment context of the parser.
     * @return either error or evaluated pair.
     */
    suspend fun evaluate(
        evalContext: EvalContext,
        assignmentContext: JccParser.AssignmentContext
    ): Either<EvalError, Pair<String, Var>> {
        val expression = assignmentContext.expression()
            ?: return assignmentContext.toError(EvalError.Type.MissingVariableAssignment).asLeft()
        return expressionEvaluator.evaluateExpression(evalContext, expression)
            .mapRight { variable ->
                val id = assignmentContext.NAME().text
                id to variable
            }
    }
}