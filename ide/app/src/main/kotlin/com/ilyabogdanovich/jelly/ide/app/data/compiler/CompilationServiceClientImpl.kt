package com.ilyabogdanovich.jelly.ide.app.data.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationResults
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.CompilationServiceClient
import com.ilyabogdanovich.jelly.ide.app.domain.compiler.SourceMarkup
import com.ilyabogdanovich.jelly.jcc.core.CompilationService
import kotlin.time.measureTimedValue

/**
 * Implementation of the [CompilationServiceClient]
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
class CompilationServiceClientImpl(
    private val compilationService: CompilationService,
    private val errorListBuilder: ErrorListBuilder,
    private val errorMarkupBuilder: ErrorMarkupBuilder,
) : CompilationServiceClient {
    override suspend fun compile(input: String): CompilationResults {
        val source = SourceMarkup.from(input)
        val (result, duration) = measureTimedValue { compilationService.compile(input) }
        return CompilationResults(
            out = result.output,
            errors = errorListBuilder.build(source, result.errors),
            duration = duration,
            errorMarkup = errorMarkupBuilder.buildMarkup(source, result.errors)
        )
    }
}
