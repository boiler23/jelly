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
internal data class EvalContext(private val vars: Map<String, Var> = mapOf()) {
    /**
     * Gets variable from the context by its name.
     * @param id variable name
     * @return variable [Var], or null if it wasn't found in the context
     */
    operator fun get(id: String): Var? = vars[id]

    /**
     * Adds new variables and returns a new context with the given variables.
     * @param vars a map of new variables to add to the context.
     * @return either new contexts with those variables included,
     *         or a subset of the provided variables, already existing in the context.
     */
    operator fun plus(vars: Map<String, Var>): Either<Set<String>, EvalContext> {
        val existingKeys = this.vars.keys.intersect(vars.keys)
        return if (existingKeys.isNotEmpty()) {
            existingKeys.asLeft()
        } else {
            EvalContext(this.vars + vars).asRight()
        }
    }
}
