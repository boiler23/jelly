package com.ilyabiogdanovich.jelly.jcc.eval

import com.ilyabogdanovich.jelly.jcc.JccParser
import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Helper class to evaluate expressions into internal interpreter representation.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class ExpressionEvaluator {
    /**
     * Does evaluation of the expression into [Var] instance.
     * @param evalContext current context to use.
     * @param parseContext given expression context, coming from the parser.
     * @return evaluated [Var] or null, if the evaluation fails.
     */
    fun evaluateExpression(
        evalContext: EvalContext,
        parseContext: JccParser.ExpressionContext
    ): Either<EvalError, Var> {
        val number = parseContext.number()
        val id = parseContext.identifier()
        return if (number != null) {
            val intNum = number.text.toIntOrNull()
            if (intNum != null) {
                Var.NumVar(Num.Integer(intNum)).asRight()
            } else {
                val realNum = number.text.toDoubleOrNull()
                if (realNum != null) {
                    Var.NumVar(Num.Real(number.text.toDouble())).asRight()
                } else {
                    number.toError(EvalError.Type.InvalidNumber).asLeft()
                }
            }
        } else if (id != null) {
            evalContext[id.text]?.asRight() ?: id.toError(EvalError.Type.UndeclaredVariable).asLeft()
        } else {
            parseContext.toError(EvalError.Type.UnsupportedExpression).asLeft()
        }
    }
}
