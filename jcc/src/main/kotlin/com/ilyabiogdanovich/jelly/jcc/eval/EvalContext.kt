package com.ilyabiogdanovich.jelly.jcc.eval

import java.util.concurrent.ConcurrentHashMap

/**
 * Holds the information about the current evaluation context.
 * @property vars map of known variable names and associated values.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class EvalContext {
    /**
     * Holds the map of all variable currently being visible.
     */
    private val vars: ConcurrentHashMap<String, Var> = ConcurrentHashMap()

    /**
     * Pushes a new variable into the context, if it doesn't exist yet.
     * @param id variable name
     * @param variable variable value
     * @return true if the variable was successfully pushed. Otherwise, returns false.
     */
    fun push(id: String, variable: Var): Boolean {
        return if (!vars.containsKey(id)) {
            vars[id] = variable
            true
        } else {
            false
        }
    }

    fun pop(id: String) {
        vars.remove(id)
    }

    operator fun get(id: String): Var? = vars[id]

    fun clear() {
        vars.clear()
    }
}
