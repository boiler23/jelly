package com.ilyabogdanovich.jelly.ide.app.presentation.compiler

import kotlin.time.Duration

/**
 * Represents the compilation status in the UI.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
sealed interface CompilationStatus {
    /**
     * User-visible message, associated with this status.
     */
    val message: String

    /**
     * Status is empty. This one is used only during the initial phase.
     */
    data object Empty : CompilationStatus {
        override val message = ""
    }

    /**
     * Compilation is in progress.
     */
    data object InProgress : CompilationStatus {
        override val message: String
            get() = "Compiling..."
    }

    /**
     * Compilation is done.
     */
    @JvmInline
    value class Done(private val duration: Duration) : CompilationStatus {
        override val message: String
            get() = "Last compile time: ${duration.inWholeMilliseconds / 1000.0}s"
    }

    /**
     * Compilation is done, but exception in the compiler has occurred.
     */
    @JvmInline
    value class Exception(private val e: Throwable) : CompilationStatus {
        override val message: String
            get() = "Compilation error: ${e.javaClass.canonicalName}: ${e.message}"
    }
}
