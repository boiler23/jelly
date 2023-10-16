package com.ilyabogdanovich.jelly.ide.app.data.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.SourceMarkup
import com.ilyabogdanovich.jelly.jcc.core.eval.EvalError

/**
 * Helper to transform the list of evaluation errors from compiler into the IDE's domain-level representation.
 *
 * @author Ilya Bogdanovich on 16.10.2023
 */
interface ErrorListBuilder {
    /**
     * Builds the errors list.
     * @param sourceMarkup source code markup to use.
     * @param errors list of [EvalError], received from the compiler.
     * @return list of mapped [CompilationResults.ErrorMessage].
     */
    fun build(sourceMarkup: SourceMarkup, errors: List<EvalError>): List<CompilationResults.ErrorMessage>
}
