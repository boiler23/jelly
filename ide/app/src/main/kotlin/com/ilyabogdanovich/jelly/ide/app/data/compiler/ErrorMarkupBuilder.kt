package com.ilyabogdanovich.jelly.ide.app.data.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.compiler.ErrorMarkup
import com.ilyabogdanovich.jelly.jcc.core.eval.EvalError

/**
 * Helper to construct the [ErrorMarkup] object from the complication results.
 *
 * @author Ilya Bogdanovich on 15.10.2023
 */
interface ErrorMarkupBuilder {
    /**
     * Builds the error markup object.
     * @param inputLines list of source code lines.
     * @param evalErrors list of errors, received from compilation service.
     * @return constructed [ErrorMarkup] object.
     */
    fun buildMarkup(inputLines: List<String>, evalErrors: List<EvalError>): ErrorMarkup
}
