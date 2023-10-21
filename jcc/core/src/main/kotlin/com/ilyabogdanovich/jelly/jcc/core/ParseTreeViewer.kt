package com.ilyabogdanovich.jelly.jcc.core

import com.ilyabogdanovich.jelly.jcc.core.antlr.JccLexer
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser
import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

/**
 * Utility, that helps to view the parse tree, built by ANTLR.
 * This class is intended to be used only for debug purposes.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
class ParseTreeViewer {
    /**
     * Runs an embedded ANTLR UI upon building the parse tree.
     * @param sourceCode source code used to build the parse tree.
     */
    fun run(sourceCode: String) {
        val syntaxErrorListener = SyntaxErrorListener()
        val lexer = JccLexer(CharStreams.fromString(sourceCode))
        lexer.addErrorListener(syntaxErrorListener)
        val parser = JccParser(CommonTokenStream(lexer))
        parser.addErrorListener(syntaxErrorListener)
        val tree = parser.program()
        @Suppress("SpreadOperator")
        val viewer = TreeViewer(listOf(*parser.ruleNames), tree)
        viewer.open()
    }
}
