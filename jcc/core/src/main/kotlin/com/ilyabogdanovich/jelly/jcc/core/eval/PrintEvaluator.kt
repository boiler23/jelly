package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser

/**
 * Evaluator for the "print" statements.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class PrintEvaluator {
    /**
     * Evaluates the given printing context and prints the result to the given builder.
     * Special sequences supported: \n - newlines, \t - tabulation. \r is ignored and skipped.
     * If string can't be extracted - does nothing. We don't raise an error in this case to avoid duplication,
     * as error was already raised before on the lexer/parser level.
     * @param ctx the printing context we have.
     * @param sb string builder to build upon.
     */
    fun evaluate(ctx: JccParser.PrintingContext, sb: StringBuilder) {
        val stringCtx = ctx.STRING()
        val string = stringCtx?.text ?: return
        val tokenIndex = stringCtx.symbol.tokenIndex
        if (tokenIndex < 0) {
            return
        }
        val startIndex = if (string.startsWith('\"')) 1 else 0
        val endIndex = if (string.endsWith('\"')) string.lastIndex else string.length

        var i = startIndex
        while (i < endIndex) {
            val cur = string[i]
            if (cur == '\\') {
                if (i < endIndex - 1) {
                    processSpecialCharacter(sb, cur, next = string[i + 1])
                    ++i
                } else {
                    sb.append(cur)
                }
            } else {
                sb.append(cur)
            }
            ++i
        }
    }

    private fun processSpecialCharacter(sb: StringBuilder, cur: Char, next: Char) {
        when (next) {
            'n' -> sb.appendLine()
            'r' -> Unit
            't' -> sb.append('\t')
            '\\' -> sb.append('\\')
            '"' -> sb.append(next)
            else -> {
                sb.append(cur)
                sb.append(next)
            }
        }
    }
}
