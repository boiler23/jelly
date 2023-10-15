package com.ilyabogdanovich.jelly.jcc.core

import com.ilyabogdanovich.jelly.jcc.core.antlr.JccLexer
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser
import com.ilyabogdanovich.jelly.jcc.core.eval.AssignmentEvaluator
import com.ilyabogdanovich.jelly.jcc.core.eval.EvalContext
import com.ilyabogdanovich.jelly.jcc.core.eval.EvalError
import com.ilyabogdanovich.jelly.jcc.core.eval.ExpressionEvaluator
import com.ilyabogdanovich.jelly.jcc.core.eval.PrintEvaluator
import com.ilyabogdanovich.jelly.jcc.core.eval.toError
import com.ilyabogdanovich.jelly.jcc.core.print.VarPrinter
import com.ilyabogdanovich.jelly.utils.Either
import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.util.BitSet

/**
 * Compiler for our language.
 *
 * @author Ilya Bogdanovich on 08.10.2023
 */
class Compiler {
    class ResultListener {
        val errors = mutableListOf<EvalError>()
        val output = StringBuilder()
        private val expressionEvaluator = ExpressionEvaluator()
        private val assignmentEvaluator = AssignmentEvaluator(expressionEvaluator)
        private val varPrinter = VarPrinter()
        private val printEvaluator = PrintEvaluator()

        suspend fun assignment(evalContext: EvalContext, ctx: JccParser.AssignmentContext): EvalContext {
            return when (val evaluated = assignmentEvaluator.evaluate(evalContext, ctx)) {
                is Either.Left -> {
                    errors.add(evaluated.value)
                    evalContext
                }
                is Either.Right -> {
                    val (id, variable) = evaluated.value
                    when (val result = evalContext + mapOf(id to variable)) {
                        is Either.Left -> {
                            errors.add(ctx.toError(result.value, expression = ctx.NAME()?.text))
                            evalContext
                        }
                        is Either.Right -> {
                            result.value
                        }
                    }
                }
            }
        }

        suspend fun out(evalContext: EvalContext, ctx: JccParser.OutputContext) {
            when (val evaluated = expressionEvaluator.evaluateExpression(evalContext, ctx.expression())) {
                is Either.Left -> errors.add(evaluated.value)
                is Either.Right -> output.append(varPrinter.print(evaluated.value))
            }
        }

        fun print(ctx: JccParser.PrintingContext) =
            printEvaluator.evaluate(ctx, output)
    }

    class ErrorListener : ANTLRErrorListener {
        val errors = mutableListOf<EvalError>()

        override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?,
            line: Int,
            charPositionInLine: Int,
            message: String?,
            e: RecognitionException?
        ) {
            errors.add(
                EvalError(
                    start = EvalError.TokenPosition(line = line, positionInLine = charPositionInLine),
                    stop = null,
                    expression = message ?: "",
                    type = EvalError.Type.SyntaxError,
                )
            )
        }

        override fun reportAmbiguity(
            recognizer: Parser?,
            dfa: DFA?,
            startIndex: Int,
            stopIndex: Int,
            exact: Boolean,
            ambigAlts: BitSet?,
            configs: ATNConfigSet?
        ) = Unit

        override fun reportAttemptingFullContext(
            recognizer: Parser?,
            dfa: DFA?,
            startIndex: Int,
            stopIndex: Int,
            conflictingAlts: BitSet?,
            configs: ATNConfigSet?
        ) = Unit

        override fun reportContextSensitivity(
            recognizer: Parser?,
            dfa: DFA?,
            startIndex: Int,
            stopIndex: Int,
            prediction: Int,
            configs: ATNConfigSet?
        ) = Unit
    }

    data class Result(
        val output: String,
        val errors: List<EvalError>,
    )

    suspend fun compile(src: String): Result {
        val resultListener = ResultListener()
        val errorListener = ErrorListener()

        val lexer = JccLexer(CharStreams.fromString(src))
        lexer.addErrorListener(errorListener)
        val parser = JccParser(CommonTokenStream(lexer))
        parser.addErrorListener(errorListener)
        val tree = parser.program()

        var evalContext = EvalContext()
        tree.statement().forEach { statement ->
            statement.output()?.let { ctx ->
                resultListener.out(evalContext, ctx)
            }
            statement.printing()?.let { ctx ->
                resultListener.print(ctx)
            }
            statement.assignment()?.let { ctx ->
                evalContext = resultListener.assignment(evalContext, ctx)
            }
        }

        return Result(
            output = resultListener.output.toString(),
            errors = (errorListener.errors + resultListener.errors)
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

    fun view(src: String) {
        val errorListener = ErrorListener()
        val lexer = JccLexer(CharStreams.fromString(src))
        lexer.addErrorListener(errorListener)
        val parser = JccParser(CommonTokenStream(lexer))
        parser.addErrorListener(errorListener)
        val tree = parser.program()
        @Suppress("SpreadOperator")
        val viewer = TreeViewer(listOf(*parser.ruleNames), tree)
        viewer.open()
    }
}
