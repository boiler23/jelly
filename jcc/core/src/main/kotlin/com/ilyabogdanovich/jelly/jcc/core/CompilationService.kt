package com.ilyabogdanovich.jelly.jcc.core

/**
 * Main entry point for the compilation engine.
 * It consumes the source code, and produces the results.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
interface CompilationService {
    /**
     * Asynchronously compiles the given source code.
     * @param sourceCode source code to compile.
     * @return [ExecutionResult] holding the compilation results.
     */
    suspend fun compile(sourceCode: String): ExecutionResult
}
