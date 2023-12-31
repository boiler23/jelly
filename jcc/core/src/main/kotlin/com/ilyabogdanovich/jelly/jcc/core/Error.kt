package com.ilyabogdanovich.jelly.jcc.core

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token

/**
 * Holds inofrmation of all errors, that can happen during any phase of the source code compilation.
 * @property start start token position of the error occurance.
 * @property stop stop token position of the error occurance.
 * @property expression expression that caused the error.
 * @property type type of the error.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
data class Error(
    val start: TokenPosition,
    val stop: TokenPosition?,
    private val expression: String,
    private val type: Type,
) {
    /**
     * Token coordinates in the text input.
     * @property line line of code where the token is located, starting from 1.
     * @property positionInLine position in the line of the token, starting from 0.
     */
    data class TokenPosition(val line: Int, val positionInLine: Int)

    /**
     * Possible evaluation error types.
     */
    enum class Type {
        /**
         * Syntax error. Details are written in the expression.
         */
        SyntaxError,

        /**
         * Invalid number was encountered: neither integer nor double.
         */
        InvalidNumber,

        /**
         * Attempt to re-declare already existing variable.
         */
        VariableRedeclaration,

        /**
         * Attempt to use variable, that wasn't declared before.
         */
        UndeclaredVariable,

        /**
         * Variable assignment is missing.
         */
        MissingVariableAssignment,

        /**
         * Sequence's lower bound is missing.
         */
        MissingSequenceLowerBound,

        /**
         * Sequence's upper bound is missing.
         */
        MissingSequenceUpperBound,

        /**
         * Sequence's upper bound is lower than lower bound.
         */
        SequenceInvalidBounds,

        /**
         * Sequence exceeds maximum supported length.
         */
        SequenceTooLong,

        /**
         * Left operand is missing in an arithmetic operation.
         */
        MissingLeftOperand,

        /**
         * Right operand is missing in an arithmetic operation.
         */
        MissingRightOperand,

        /**
         * Operand is missing in an arithmetic operation.
         */
        MissingUnaryOperand,

        /**
         * Operator is missing in arithmetic operation.
         */
        MissingOperator,

        /**
         * Sequence declaration is missing in map().
         */
        MapMissingSequence,

        /**
         * Lambda declaration is missing in map().
         */
        MapMissingLambda,

        /**
         * map()'s lambda is missing the local variable identifier declaration.
         */
        MapMissingLambdaId,

        /**
         * map()'s lambda is missing the expression.
         */
        MapMissingLambdaExpression,

        /**
         * Lambda function in map() is expected to return a number, however it doesn't.
         */
        MapLambdaReturnsNotNumber,

        /**
         * Sequence declaration is missing in reduce().
         */
        ReduceMissingSequence,

        /**
         * Neutral element declaration is missing in reduce().
         */
        ReduceMissingNeutral,

        /**
         * Lambda declaration is missing in reduce().
         */
        ReduceMissingLambda,

        /**
         * reduce()'s lambda is missing the accumulator variable identifier declaration.
         */
        ReduceMissingLambdaAccumulator,

        /**
         * reduce()'s lambda is missing the next element variable identifier declaration.
         */
        ReduceMissingLambdaNext,

        /**
         * reduce()'s lambda is missing the expression.
         */
        ReduceMissingLambdaExpression,

        /**
         * Reduce's neutral element is expected to be a number, however it is not.
         */
        ReduceNeutralIsNotNumber,

        /**
         * Lambda function in reduce() is expected to return a number, however it doesn't.
         */
        ReduceLambdaReturnsNotNumber,

        /**
         * Invalid operand detected in arithmetic operation.
         */
        InvalidArithmeticOperand,

        /**
         * Invalid operator detected in arithmetic operation.
         */
        InvalidArithmeticOperator,

        /**
         * Sequence start is expected to be an integer value, however it is not.
         */
        SequenceStartIsNotInteger,

        /**
         * Sequence stop is expected to be an integer value, however it is not.
         */
        SequenceStopIsNotInteger,

        /**
         * Expression has to be evaluated to sequence, however it can't.
         */
        SequenceExpected,

        /**
         * Top-level expression was encountered. They are not supported according to the languguage definition.
         */
        TopLevelExpressionsUnsupported,

        /**
         * Unsupported expression was encountered.
         */
        UnsupportedExpression,
    }

    /**
     * Formatted error message, that has all the required information.
     */
    val formattedMessage: String
        get() = "line ${start.line}:${start.positionInLine}: $message."

    /**
     * Helper to build a user-readable error message.
     * Should be moved out later on, in order to support localizer error messages.
     */
    private val message: String
        get() = when (type) {
            Type.SyntaxError -> "Syntax error: $expression"
            Type.InvalidNumber -> "Invalid number encountered in '$expression'. it is neither integer or double"
            Type.VariableRedeclaration -> "Variable redeclaration: '$expression'"
            Type.UndeclaredVariable -> "Variable undeclared: '$expression'"
            Type.UnsupportedExpression -> "Unsupported expression encountered: '$expression'"
            Type.InvalidArithmeticOperand -> "Invalid arithmetic operand: '$expression'"
            Type.InvalidArithmeticOperator -> "Invalid arithmetic operator: '$expression'"
            Type.SequenceStartIsNotInteger -> "Sequence's begin is expected to be an integer: '$expression'"
            Type.SequenceStopIsNotInteger -> "Sequence's end is expected to be an integer: '$expression'"
            Type.SequenceExpected -> "Expected sequence: '$expression'"
            Type.MissingVariableAssignment -> "Missing variable assignment: '$expression'"
            Type.MissingSequenceLowerBound -> "Missing sequence's lower bound: '$expression'"
            Type.MissingSequenceUpperBound -> "Missing sequence's upper bound: '$expression'"
            Type.SequenceInvalidBounds -> "Sequence's upper bound is less than lower bound: $expression"
            Type.SequenceTooLong -> "Sequence's length is exceeding the limits: $expression"
            Type.MissingLeftOperand -> "Missing left operand: '$expression'"
            Type.MissingRightOperand -> "Missing right operand: '$expression'"
            Type.MissingUnaryOperand -> "Missing unary operand: '$expression'"
            Type.MissingOperator -> "Operator is missing in arithmetic operation: '$expression'"
            Type.MapMissingSequence -> "Missing sequence declaration in map(): '$expression'"
            Type.MapMissingLambda -> "Missing lambda declaration in map(): '$expression'"
            Type.MapMissingLambdaId -> "Missing iterator declaration in map's lambda: '$expression'"
            Type.MapMissingLambdaExpression -> "Missing return expression in map's lambda: '$expression'"
            Type.MapLambdaReturnsNotNumber -> "Lambda in map() is expected to return a number: '$expression'"
            Type.ReduceMissingSequence -> "Missing sequence declaration in reduce(): '$expression'"
            Type.ReduceMissingNeutral -> "Missing neutral element declaration in reduce(): '$expression'"
            Type.ReduceMissingLambda -> "Missing lambda declaration in reduce(): '$expression'"
            Type.ReduceMissingLambdaAccumulator -> "Missing accumulator declaration in reduce's lambda: '$expression'"
            Type.ReduceMissingLambdaNext -> "Missing next element declaration in reduce's lambda: '$expression'"
            Type.ReduceMissingLambdaExpression ->
                "Missing return expression declaration in reduce's lambda: '$expression'"
            Type.ReduceNeutralIsNotNumber -> "Neutral element in reduce() should be a number expression: '$expression'"
            Type.ReduceLambdaReturnsNotNumber -> "Lambda in reduce() is expected to return a number: '$expression'"
            Type.TopLevelExpressionsUnsupported -> "Unexpected top-level expression: '$expression'"
        }
}

private fun Token.toPosition() =
    Error.TokenPosition(line = line, positionInLine = charPositionInLine)

internal fun ParserRuleContext.toError(type: Error.Type, expression: String? = null): Error {
    val startToken = getStart()
    val stopToken = getStop()
    return Error(
        start = startToken.toPosition(),
        stop = stopToken?.toPosition(),
        expression = expression ?: text,
        type = type,
    )
}
