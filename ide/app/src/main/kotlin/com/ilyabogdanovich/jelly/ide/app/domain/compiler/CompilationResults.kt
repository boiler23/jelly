package com.ilyabogdanovich.jelly.ide.app.domain.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.DeepLink
import kotlin.time.Duration

/**
 * Results of the compilation, as received from [CompilationServiceClient].
 * @property out Standard output of the compilation.
 * @property errors List of error messages received from the compilation service.
 * @property errorMarkup Markup of the errors, to highlight them in the source code.
 * @property duration time taken to process the given input.
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
data class CompilationResults(
    val out: String,
    val errors: List<ErrorMessage>,
    val errorMarkup: ErrorMarkup,
    val duration: Duration,
) {
    /**
     * Holds the error message information.
     * @property formattedMessage error message, ready to be presented in the error output.
     * @property deepLink deep link, holding the position in the source code, pointing to the error.
     */
    data class ErrorMessage(val formattedMessage: String, val deepLink: DeepLink)
}
