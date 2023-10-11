package com.ilyabiogdanovich.jelly.jcc

import com.ilyabiogdanovich.jelly.jcc.eval.AssignmentEvaluator
import com.ilyabiogdanovich.jelly.jcc.eval.EvalContext
import com.ilyabiogdanovich.jelly.jcc.eval.EvalError
import com.ilyabiogdanovich.jelly.jcc.eval.ExpressionEvaluator
import com.ilyabiogdanovich.jelly.jcc.eval.PrintEvaluator
import com.ilyabiogdanovich.jelly.jcc.eval.toError
import com.ilyabiogdanovich.jelly.jcc.print.VarPrinter
import com.ilyabogdanovich.jelly.jcc.JccLexer
import com.ilyabogdanovich.jelly.jcc.JccParser
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
        val errors = mutableListOf<String>()
        val list = mutableListOf<String>()
        private val expressionEvaluator = ExpressionEvaluator()
        private val assignmentEvaluator = AssignmentEvaluator(expressionEvaluator)
        private val varPrinter = VarPrinter()
        private val printEvaluator = PrintEvaluator()

        suspend fun assignment(evalContext: EvalContext, ctx: JccParser.AssignmentContext): EvalContext {
            return when (val evaluated = assignmentEvaluator.evaluate(evalContext, ctx)) {
                is Either.Left -> {
                    errors.add(evaluated.value.formattedMessage)
                    evalContext
                }
                is Either.Right -> {
                    val (id, variable) = evaluated.value
                    when (val result = evalContext + mapOf(id to variable)) {
                        is Either.Left -> {
                            errors.add(ctx.toError(result.value).formattedMessage)
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
                is Either.Left -> errors.add(evaluated.value.formattedMessage)
                is Either.Right -> list.add(varPrinter.print(evaluated.value))
            }
        }

        fun print(ctx: JccParser.PrintingContext) {
            val output = printEvaluator.evaluate(ctx)
            if (output != null) {
                list.add(output)
            }
        }
    }

    class ErrorListener : ANTLRErrorListener {
        val list = mutableListOf<String>()

        override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?,
            line: Int,
            charPositionInLine: Int,
            message: String?,
            e: RecognitionException?
        ) {
            list.add("line $line:$charPositionInLine $message")
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
        val output: List<String>,
        val errors: List<String>,
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
            output = resultListener.list,
            errors = errorListener.list + resultListener.errors,
        )
    }

    fun view(src: String) {
        val errorListener = ErrorListener()
        val lexer = JccLexer(CharStreams.fromString(src))
        lexer.addErrorListener(errorListener)
        val parser = JccParser(CommonTokenStream(lexer))
        parser.addErrorListener(errorListener)
        val tree = parser.program()
        val viewer = TreeViewer(listOf(*parser.ruleNames), tree)
        viewer.open()
    }
}
