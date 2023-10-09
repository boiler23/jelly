package com.ilyabiogdanovich.jelly.jcc.eval

/**
 * Holds the information about the current evaluation context.
 * @property vars map of known variable names and associated values.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
data class EvalContext(val vars: Map<String, Var>)
