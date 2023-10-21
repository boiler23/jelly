package com.ilyabogdanovich.jelly.jcc.core

import com.ilyabogdanovich.jelly.jcc.core.eval.ProgramEvaluator
import com.ilyabogdanovich.jelly.jcc.core.parse.ParseTreeBuilder

/**
 * Implementation for [CompilationService]
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
internal class CompilationServiceImpl(
    private val parseTreeBuilder: ParseTreeBuilder,
    private val programEvaluator: ProgramEvaluator,
) : CompilationService {
    override suspend fun compile(sourceCode: String): ExecutionResult {
        val parseResult = parseTreeBuilder.build(sourceCode)
        val evaluationResult = programEvaluator.evaluate(parseResult.tree)
        return ExecutionResult(
            output = evaluationResult.output,
            errors = (parseResult.syntaxErrors + evaluationResult.errors)
                .sortedWith { o1, o2 ->
                    val compareLines = o1.start.line.compareTo(o2.start.line)
                    if (compareLines == 0) {
                        o1.start.positionInLine.compareTo(o2.start.positionInLine)
                    } else {
                        compareLines
                    }
                },
        )
    }
}
