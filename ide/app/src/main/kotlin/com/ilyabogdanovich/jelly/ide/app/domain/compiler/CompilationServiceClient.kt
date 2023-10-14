package com.ilyabogdanovich.jelly.ide.app.domain.compiler

/**
 * Client of the compilation service.
 * Receives the compilation requests, passes them to the service, and returns the results.
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
interface CompilationServiceClient {
    /**
     * Makes a request to the compilation service, and asynchronously returns the results.
     * @param input code to compile.
     * @return [CompilationResults] object.
     */
    suspend fun compile(input: String): CompilationResults
}
