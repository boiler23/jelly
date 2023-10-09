package com.ilyabiogdanovich.jelly.jcc

import com.ilyabogdanovich.jelly.jcc.JccBaseListener
import com.ilyabogdanovich.jelly.jcc.JccLexer
import com.ilyabogdanovich.jelly.jcc.JccParser
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.util.BitSet

/**
 * Sample compiler, based on ANTLR output.
 *
 * @author Ilya Bogdanovich on 08.10.2023
 */
class Compiler {
    class ResultListener : JccBaseListener() {
        val list = mutableListOf<String>()

        override fun enterMap(ctx: JccParser.MapContext?) {
            if (ctx != null) {
                list.add("map: input=${ctx.expression().text}, lambda = { ${ctx.lambda1().text} }")
            }
            super.enterMap(ctx)
        }

        override fun enterReduce(ctx: JccParser.ReduceContext?) {
            if (ctx != null) {
                list.add("reduce: input=${ctx.expression(0).text}, neutral=${ctx.expression(1).text}, lambda = { ${ctx.lambda2().text} }")
            }
            super.enterReduce(ctx)
        }

        override fun visitErrorNode(node: ErrorNode?) {
            list.add(node?.text ?: "Unknown error")

            super.visitErrorNode(node)
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

    data class Output(
        val results: List<String>,
        val errors: List<String>,
    )

    fun compile(src: String): Output {
        val resultListener = ResultListener()
        val errorListener = ErrorListener()

        val lexer = JccLexer(CharStreams.fromString(src))
        lexer.addErrorListener(errorListener)
        val parser = JccParser(CommonTokenStream(lexer))
        parser.addErrorListener(errorListener)
        val tree = parser.program()
        ParseTreeWalker.DEFAULT.walk(resultListener, tree)
        return Output(
            results = resultListener.list,
            errors = errorListener.list,
        )
    }
}
