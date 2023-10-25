package com.ilyabogdanovich.jelly.ide.app.data.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.compiler.ErrorMarkup
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.SourceMarkup
import com.ilyabogdanovich.jelly.jcc.core.Error

/**
 * Helper to construct the [ErrorMarkup] object from the complication results.
 *
 * @author Ilya Bogdanovich on 15.10.2023
 */
interface ErrorMarkupBuilder {
    /**
     * Builds the error markup object.
     * @param sourceMarkup source code markup to use.
     * @param errors list of errors, received from compilation service.
     * @return constructed [ErrorMarkup] object.
     */
    fun buildMarkup(sourceMarkup: SourceMarkup, errors: List<Error>): ErrorMarkup
}
