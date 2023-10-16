package com.ilyabogdanovich.jelly.ide.app.data.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.DeepLink
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.jcc.core.eval.EvalError

/**
 * Implementation for [ErrorListBuilder]
 *
 * @author Ilya Bogdanovich on 16.10.2023
 */
class ErrorListBuilderImpl : ErrorListBuilder {
    private fun List<String>.getIndex(tokenPosition: EvalError.TokenPosition): Int {
        var pos = 0
        for (line in 0 until tokenPosition.line - 1) {
            pos += this[line].length + 1
        }
        pos += tokenPosition.positionInLine
        return pos
    }

    override fun build(inputLines: List<String>, errors: List<EvalError>): List<CompilationResults.ErrorMessage> {
        return errors.map {
            CompilationResults.ErrorMessage(
                it.formattedMessage,
                DeepLink.Cursor(position = inputLines.getIndex(it.start))
            )
        }
    }
}
