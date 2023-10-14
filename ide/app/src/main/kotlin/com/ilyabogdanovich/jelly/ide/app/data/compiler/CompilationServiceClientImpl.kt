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
class CompilationServiceClientImpl(private val compiler: Compiler) : CompilationServiceClient {
    override suspend fun compile(input: String): CompilationResults {
        val (result, duration) = measureTimedValue { compiler.compile(input) }
        return CompilationResults(
            out = result.output.joinToString(""),
            err = result.errors.joinToString("\n"),
            duration = duration,
        )
    }
}
