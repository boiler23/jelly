package com.ilyabogdanovich.jelly.ide.app.data.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.DeepLink
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.SourceMarkup
import com.ilyabogdanovich.jelly.jcc.core.eval.EvalError

/**
 * Implementation for [ErrorListBuilder]
 *
 * @author Ilya Bogdanovich on 16.10.2023
 */
class ErrorListBuilderImpl : ErrorListBuilder {
    override fun build(sourceMarkup: SourceMarkup, errors: List<EvalError>): List<CompilationResults.ErrorMessage> {
        return errors.map {
            CompilationResults.ErrorMessage(
                it.formattedMessage,
                DeepLink.Cursor(position = sourceMarkup.lineStarts[it.start.line - 1] + it.start.positionInLine)
            )
        }
    }
}
