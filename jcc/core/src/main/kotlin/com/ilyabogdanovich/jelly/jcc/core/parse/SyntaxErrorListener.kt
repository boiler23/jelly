package com.ilyabogdanovich.jelly.jcc.core.parse

import com.ilyabogdanovich.jelly.jcc.core.eval.EvalError
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.util.BitSet

/**
 * Implementation of [ANTLRErrorListener] to collect syntax errors during the lexer phase.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
internal class SyntaxErrorListener : ANTLRErrorListener {
    /**
     * Holds the list of collected errors.
     */
    val errors = mutableListOf<EvalError>()

    /**
     * Upon syntax error, notify any interested parties.
     */
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

    /**
     * This method is called by the parser when a full-context prediction results in an ambiguity.
     * Not used by our compiler.
     */
    override fun reportAmbiguity(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        exact: Boolean,
        ambigAlts: BitSet?,
        configs: ATNConfigSet?
    ) = Unit

    /**
     * Called when an SLL conflict occurs full context information is about to be used to make an LL decision.
     * Not used by our compiler.
     */
    override fun reportAttemptingFullContext(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        conflictingAlts: BitSet?,
        configs: ATNConfigSet?
    ) = Unit

    /**
     * This method is called by the parser when a full-context prediction has a unique result.
     * Not used by our compiler.
     */
    override fun reportContextSensitivity(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        prediction: Int,
        configs: ATNConfigSet?
    ) = Unit
}
