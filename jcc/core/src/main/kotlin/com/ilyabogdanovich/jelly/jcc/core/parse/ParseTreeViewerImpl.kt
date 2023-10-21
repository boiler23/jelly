package com.ilyabogdanovich.jelly.jcc.core.parse

import org.antlr.v4.gui.TreeViewer

/**
 * Implementation for [ParseTreeViewer].
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
internal class ParseTreeViewerImpl(private val parseTreeBuilder: ParseTreeBuilder) : ParseTreeViewer {
    override fun run(sourceCode: String) {
        val parseResult = parseTreeBuilder.build(sourceCode)
        val viewer = TreeViewer(parseResult.ruleNames.toList(), parseResult.tree)
        viewer.open()
    }
}
