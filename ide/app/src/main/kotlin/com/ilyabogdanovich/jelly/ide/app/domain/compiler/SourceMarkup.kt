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
            val lineStarts = mutableListOf<Int>()
            val lineLengths = mutableListOf<Int>()
            var curLength = 0
            var curStart = 0
            for (i in source.indices) {
                val c = source[i]
                if (c == '\n') {
                    lineStarts.add(curStart)
                    lineLengths.add(curLength)
                    curLength = 0
                    curStart = i + 1
                } else {
                    curLength++
                }
            }

            lineLengths.add(curLength)
            lineStarts.add(curStart)

            return SourceMarkup(lineLengths, lineStarts)
        }
    }
}
