package com.ilyabogdanovich.jelly.ide.app.data.compiler

import com.ilyabogdanovich.jelly.ide.app.domain.compiler.ErrorMarkup
import com.ilyabogdanovich.jelly.jcc.core.eval.EvalError
import kotlin.math.max

/**
 * Implementation for [ErrorMarkupBuilder]
 *
 * @author Ilya Bogdanovich on 15.10.2023
 */
class ErrorMarkupBuilderImpl : ErrorMarkupBuilder {
    override fun buildMarkup(inputLines: List<String>, evalErrors: List<EvalError>): ErrorMarkup {
        val result = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()

        fun markup(line: Int, pos: Int) {
            if (line <= inputLines.size) { // because lines in EvalError are numerated from 1
                result.getOrPut(line - 1) { mutableListOf() }.add(pos to inputLines[line - 1].length)
            }
        }

        fun markup(line: Int, start: Int, stop: Int) {
            if (line <= inputLines.size) {
                result.getOrPut(line - 1) { mutableListOf() }.add(start to stop + 1)
            }
        }

        fun between(start: EvalError.TokenPosition, stop: EvalError.TokenPosition) {
            markup(line = start.line, pos = start.positionInLine)
            for (line in start.line + 1 until stop.line) {
                markup(line = line, pos = 0)
            }
            markup(line = stop.line, start = 0, stop = stop.positionInLine)
        }

        fun lineRange(start: EvalError.TokenPosition, stop: EvalError.TokenPosition) {
            if (start.line < stop.line) {
                between(start = start, stop = stop)
            } else {
                between(start = stop, stop = start)
            }
        }

        for (error in evalErrors) {
            val start = error.start
            val stop = error.stop
            if (stop != null) {
                if (stop.line == start.line) {
                    markup(line = start.line, start = start.positionInLine, stop = stop.positionInLine)
                } else {
                    lineRange(start = start, stop = stop)
                }
            } else {
                markup(line = start.line, pos = start.positionInLine)
            }
        }

        return ErrorMarkup(
            result
                .flatMap { (line, underlines) ->
                    underlines
                        .withoutOverlaps()
                        .map { ErrorMarkup.Underline(line = line, start = it.first, stop = it.second) }
                }
        )
    }

    private fun MutableList<Pair<Int, Int>>.withoutOverlaps(): List<Pair<Int, Int>> {
        if (isEmpty()) {
            return emptyList()
        }

        sortBy { it.first }

        val result = mutableListOf<Pair<Int, Int>>()
        val i = iterator()
        var current = i.next()
        while (i.hasNext()) {
            val next = i.next()
            current = if (next.first > current.second) {
                result.add(current)
                next
            } else {
                current.first to max(current.second, next.second)
            }
        }
        result.add(current)
        return result
    }
}
