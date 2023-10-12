package com.ilyabogdanovich.jelly.jcc.eval

import com.ilyabogdanovich.jelly.jcc.JccParser

/**
 * Evaluator for the "print" statements.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class PrintEvaluator {
    /**
     * Evaluates the given printing context into an actual string.
     * @param ctx the printing context we have.
     * @return actual string to print to the output. If string can't be extracted - returns null.
     *         We don't raise an error in this case to avoid duplication,
     *         as error was already raised before on the lexer/parser level.
     */
    fun evaluate(ctx: JccParser.PrintingContext): String? {
        val stringCtx = ctx.STRING()
        val string = stringCtx?.text ?: return null
        val tokenIndex = stringCtx.symbol.tokenIndex
        if (tokenIndex < 0) {
            return null
        }
        val startIndex = if (string.startsWith('\"')) 1 else 0
        val endIndex = if (string.endsWith('\"')) string.lastIndex else string.length
        return string.substring(startIndex, endIndex).replace("\\\"", "\"")
    }
}
