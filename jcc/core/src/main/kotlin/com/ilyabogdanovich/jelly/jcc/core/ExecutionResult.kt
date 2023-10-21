package com.ilyabogdanovich.jelly.jcc.core

/**
 * Holds the results of the compilation execution.
 * @property output this is the output string of the program execution.
 * @property errors this is the list of errors, encountered during the program execution.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
data class ExecutionResult(
    val output: String,
    val errors: List<Error>,
)
