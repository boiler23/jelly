package com.ilyabogdanovich.jelly.ide.app.data.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationServiceClient
import com.ilyabogdanovich.jelly.jcc.core.Compiler
import kotlin.time.measureTimedValue

/**
 * Implementation of the [CompilationServiceClient]
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
class CompilationServiceClientImpl(
    private val compiler: Compiler,
    private val errorMarkupBuilder: ErrorMarkupBuilder,
) : CompilationServiceClient {
    override suspend fun compile(input: String): CompilationResults {
        val inputLines = input.split("\n")
        val (result, duration) = measureTimedValue { compiler.compile(input) }
        return CompilationResults(
            out = result.output,
            err = result.errors.joinToString("\n") { it.formattedMessage },
            duration = duration,
            errorMarkup = errorMarkupBuilder.buildMarkup(inputLines, result.errors)
        )
    }
}
