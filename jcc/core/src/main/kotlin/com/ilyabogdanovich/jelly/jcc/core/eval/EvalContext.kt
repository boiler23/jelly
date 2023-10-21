package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.Error
import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight

/**
 * Holds the information about the current evaluation context.
 * @property vars map of known variable names and associated values.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
internal class EvalContext(private val vars: Map<String, Var> = mapOf()) {
    operator fun get(id: String): Var? = vars[id]

    operator fun plus(vars: Map<String, Var>): Either<Error.Type, EvalContext> {
        return if (vars.keys.any { it in this.vars.keys }) {
            Error.Type.VariableRedeclaration.asLeft()
        } else {
            EvalContext(this.vars + vars).asRight()
        }
    }
}
