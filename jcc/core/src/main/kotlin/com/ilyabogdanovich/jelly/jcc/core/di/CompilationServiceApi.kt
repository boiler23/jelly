package com.ilyabogdanovich.jelly.jcc.core.di

import com.ilyabogdanovich.jelly.jcc.core.CompilationService
import com.ilyabogdanovich.jelly.jcc.core.parse.ParseTreeViewer

/**
 * Public API of the compilation service.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
interface CompilationServiceApi {
    /**
     * Provides access to the [CompilationService] object.
     */
    val compilationService: CompilationService

    /**
     * Provides access to the [ParseTreeViewer] object.
     */
    val parseTreeViewer: ParseTreeViewer

    companion object {
        /**
         * Creates an instance of the [CompilationServiceApi].
         * This is typically a singleton, but it's up to the library's consumer to decide.
         */
        fun create(): CompilationServiceApi = CompilationServiceComponent()
    }
}
