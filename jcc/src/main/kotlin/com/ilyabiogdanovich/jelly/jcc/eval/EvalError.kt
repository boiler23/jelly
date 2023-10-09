package com.ilyabiogdanovich.jelly.jcc.eval

/**
 * Holds inofrmation of an error, that can happen during the expression evaluation.
 * @property line line of code where the error occured.
 * @property positionInLine position in the line of code where the error occured.
 * @property expression expression that caused the error.
 * @property type type of the error.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
data class EvalError(
    private val line: Int,
    private val positionInLine: Int,
    private val expression: String,
    private val type: Type,
) {
    /**
     * Possible evaluation error types.
     */
    enum class Type {
        /**
         * Invalid number was encountered: neither integer or double.
         */
        InvalidNumber,

        /**
         * Unsupported expression was encountered.
         */
        UnsupportedExpression,
    }

    /**
     * Formatted error message, that has all the required information.
     */
    val formattedMessage: String
        get() = "$line:$positionInLine: $message"

    /**
     * Helper to build a user-readable error message.
     * Should be moved out later on, in order to support localizer error messages.
     */
    private val message: String
        get() = when (type) {
            Type.InvalidNumber -> "Invalid number encountered in `$expression`. It is neither integer or double."
            Type.UnsupportedExpression -> "Unsupported expression encountered: `$expression`."
        }
}
