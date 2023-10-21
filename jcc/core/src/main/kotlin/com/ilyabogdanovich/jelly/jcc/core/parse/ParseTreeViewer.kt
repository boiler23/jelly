package com.ilyabogdanovich.jelly.jcc.core.parse

import org.antlr.v4.gui.TreeViewer

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
        val parseResult = ParseTreeBuilder().build(sourceCode)
        val viewer = TreeViewer(parseResult.ruleNames.toList(), parseResult.tree)
        viewer.open()
    }
}
