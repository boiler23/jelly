package com.ilyabogdanovich.jelly.jcc.core.di

import com.ilyabogdanovich.jelly.jcc.core.CompilationServiceImpl
import com.ilyabogdanovich.jelly.jcc.core.eval.AssignmentEvaluator
import com.ilyabogdanovich.jelly.jcc.core.eval.ExpressionEvaluator
import com.ilyabogdanovich.jelly.jcc.core.eval.PrintEvaluator
import com.ilyabogdanovich.jelly.jcc.core.eval.ProgramEvaluator
import com.ilyabogdanovich.jelly.jcc.core.parse.ParseTreeBuilder
import com.ilyabogdanovich.jelly.jcc.core.parse.ParseTreeViewerImpl
import com.ilyabogdanovich.jelly.jcc.core.print.VarPrinter

/**
 * Component, implementing the [CompilationServiceApi].
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
internal class CompilationServiceComponent : CompilationServiceApi {
    private val parseTreeBuilder by lazy { ParseTreeBuilder() }
    private val expressionEvaluator by lazy { ExpressionEvaluator() }
    private val assignmentEvaluator by lazy { AssignmentEvaluator(expressionEvaluator) }
    private val printEvaluator by lazy { PrintEvaluator() }
    private val varPrinter by lazy { VarPrinter() }
    private val programEvaluator by lazy {
        ProgramEvaluator(expressionEvaluator, printEvaluator, assignmentEvaluator, varPrinter)
    }

    override val compilationService by lazy { CompilationServiceImpl(parseTreeBuilder, programEvaluator) }

    override val parseTreeViewer by lazy { ParseTreeViewerImpl(parseTreeBuilder) }
}
