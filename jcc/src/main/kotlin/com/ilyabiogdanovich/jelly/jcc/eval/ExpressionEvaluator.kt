package com.ilyabiogdanovich.jelly.jcc.eval

import com.ilyabogdanovich.jelly.jcc.JccParser

/**
 * Helper class to evaluate expressions into internal interpreter representation.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class ExpressionEvaluator {
    /**
     * Does evaluation of the expression into [Var] instance.
     * @param evalContext current context to use.
     * @param expressionContext given expression context, coming from the parser.
     * @return evaluated [Var] or null, if the evaluation fails.
     */
    fun evaluateExpression(evalContext: EvalContext, expressionContext: JccParser.ExpressionContext): Var? {
        val number = expressionContext.number()
        return if (number != null) {
            val intNum = number.text.toIntOrNull()
            Var.NumVar(
                if (intNum != null) {
                    Num.Integer(intNum)
                } else {
                    Num.Real(number.text.toDouble())
                }
            )
        } else {
            null
        }
    }
}
