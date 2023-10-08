package com.ilyabiogdanovich.jelly.jcc

import com.ilyabogdanovich.jelly.jcc.JccBaseListener
import com.ilyabogdanovich.jelly.jcc.JccLexer
import com.ilyabogdanovich.jelly.jcc.JccParser
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.tree.TerminalNode
import java.util.BitSet

/**
 * Sample compiler, based on ANTLR output.
 *
 * @author Ilya Bogdanovich on 08.10.2023
 */
class Compiler {
    class ResultListener : JccBaseListener() {
        val builder = StringBuilder()

        override fun visitTerminal(node: TerminalNode?) {
            if (node != null) {
                when (node.symbol.type) {
                    JccParser.GREET -> builder.appendLine(node.text)
                    JccParser.NAME -> builder.appendLine(node.text)
                }
            }

            super.visitTerminal(node)
        }

        override fun visitErrorNode(node: ErrorNode?) {
            if (node != null) {
                builder.appendLine(node.text)
            }

            super.visitErrorNode(node)
        }

        override fun enterEveryRule(ctx: ParserRuleContext?) {
            super.enterEveryRule(ctx)
        }

        override fun exitEveryRule(ctx: ParserRuleContext?) {
            super.exitEveryRule(ctx)
        }

        override fun enterParse(ctx: JccParser.ParseContext?) {
            super.enterParse(ctx)
        }

        override fun exitParse(ctx: JccParser.ParseContext?) {
            super.exitParse(ctx)
        }
    }

    class ErrorListener : ANTLRErrorListener {
        val builder = StringBuilder()

        override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?,
            line: Int,
            charPositionInLine: Int,
            message: String?,
            e: RecognitionException?
        ) {
            builder.appendLine("line $line:$charPositionInLine $message")
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
        val result: String,
        val errors: String,
    )

    fun compile(src: String): Output {
        val resultListener = ResultListener()
        val errorListener = ErrorListener()

        val lexer = JccLexer(CharStreams.fromString(src))
        lexer.addErrorListener(errorListener)
        val parser = JccParser(CommonTokenStream(lexer))
        parser.addErrorListener(errorListener)
        val tree = parser.parse()
        ParseTreeWalker.DEFAULT.walk(resultListener, tree)
        return Output(
            result = resultListener.builder.toString(),
            errors = errorListener.builder.toString(),
        )
    }
}
