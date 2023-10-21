package com.ilyabogdanovich.jelly.jcc.core.parse

import com.ilyabogdanovich.jelly.jcc.core.Error
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser

/**
 * Holds the results of lexer/parser phases, done via ANTLR.
 * @property tree the resulting parse tree - it can be further used for the evaluation phase.
 * @property syntaxErrors list of syntax errors encountered during the lexing/parsing phases.
 * @property ruleNames list of the parsing rule names registered in G4 language definiition.
 *                     This one is only used for debug purposes - to visualize the parse tree.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
class ParseResult(
    val tree: JccParser.ProgramContext,
    val syntaxErrors: List<Error>,
    val ruleNames: Array<String>,
)
