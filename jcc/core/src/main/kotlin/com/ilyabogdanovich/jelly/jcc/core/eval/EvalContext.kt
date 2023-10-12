package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight

/**
 * Holds the information about the current evaluation context.
 * @property vars map of known variable names and associated values.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class EvalContext(private val vars: Map<String, Var> = mapOf()) {
    operator fun get(id: String): Var? = vars[id]

    operator fun plus(vars: Map<String, Var>): Either<EvalError.Type, EvalContext> {
        return if (vars.keys.any { it in this.vars.keys }) {
            EvalError.Type.VariableRedeclaration.asLeft()
        } else {
            EvalContext(this.vars + vars).asRight()
        }
    }
}
