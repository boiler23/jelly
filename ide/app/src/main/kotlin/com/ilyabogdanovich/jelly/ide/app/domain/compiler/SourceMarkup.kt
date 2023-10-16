package com.ilyabogdanovich.jelly.ide.app.domain.compiler

/**
 * Markup for the compilation source: provides info about line splits and lengths.
 *
 * @author Ilya Bogdanovich on 16.10.2023
 */
data class SourceMarkup(
    val lineLengths: List<Int>,
    val lineStarts: List<Int>,
) {
    companion object {
        // todo: optimize it
        fun from(source: String): SourceMarkup {
            val inputLines = source.split("\n")
            var cur = 0
            val lineStarts = inputLines.map {
                val start = cur
                cur += it.length + 1
                start
            }
            return SourceMarkup(inputLines.map { it.length }, lineStarts)
        }
    }
}
