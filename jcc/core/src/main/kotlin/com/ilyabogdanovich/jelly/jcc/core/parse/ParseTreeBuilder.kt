package com.ilyabogdanovich.jelly.jcc.core.parse

import com.ilyabogdanovich.jelly.jcc.core.antlr.JccLexer
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

/**
 * Internal utility to produce the pa`rsing tree.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
internal class ParseTreeBuilder {
    /**
     * Builds the [ParseResult] from the given [sourceCode].
     * @param sourceCode source code to parse from.
     * @return [ParseResult] object.
     */
    fun build(sourceCode: String): ParseResult {
        val syntaxErrorListener = SyntaxErrorListener()

        val lexer = JccLexer(CharStreams.fromString(sourceCode))
        lexer.addErrorListener(syntaxErrorListener)
        val parser = JccParser(CommonTokenStream(lexer))
        parser.addErrorListener(syntaxErrorListener)

        return ParseResult(parser.program(), syntaxErrorListener.errors, parser.ruleNames)
    }
}
