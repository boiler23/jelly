package com.ilyabogdanovich.jelly.ide.app.domain.compiler

/**
 * Error markup, used by presentation layer to highlight errors in the code editor.
 *
 * @author Ilya Bogdanovich on 15.10.2023
 */
@JvmInline
value class ErrorMarkup(val errors: List<Underline>) {
    /**
     * Highlight coordinates, ready to be used by presentation layer.
     * @property line line of the code, starting from 0
     * @property start index in the [line] to begin highlight (inclusive), starting from zero.
     * @property stop index in the [line] to end highlight (exclusive), starting from zero.
     */
    data class Underline(val line: Int, val start: Int, val stop: Int)

    companion object {
        fun empty() = ErrorMarkup(listOf())
    }
}
