package com.ilyabiogdanovich.jelly.jcc.print

import com.ilyabiogdanovich.jelly.jcc.eval.Num
import com.ilyabiogdanovich.jelly.jcc.eval.Seq
import com.ilyabiogdanovich.jelly.jcc.eval.Var

/**
 * Printer for variables. This printer is used in "out" command of our language.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class VarPrinter {
    /**
     * Prints the given variable [variable]. Result is returned in a string.
     * @param variable to print.
     * @return string with the printed result.
     */
    fun print(variable: Var): String = when (variable) {
        is Var.NumVar -> variable.v.print()
        is Var.SeqVar -> variable.v.print()
    }

    private fun Num.print(): String = when (this) {
        is Num.Integer -> v.toString()
        is Num.Real -> r.toString()
    }

    private fun Seq.print() = when (this) {
        is Seq.Bounds -> printBounds()
        is Seq.Array -> printArray()
    }

    private fun Seq.Bounds.printBounds() = buildString {
        append("{ ")
        if (from <= to) {
            for (n in from until to) {
                append("$n, ")
            }
            append(to)
        }
        append(" }")
    }

    private fun Seq.Array.printArray() = buildString {
        append("{ ")
        for (n in 0 until elements.lastIndex) {
            append(print(elements[n]))
            append(", ")
        }
        if (elements.isNotEmpty()) {
            val v = elements.last()
            append(print(v))
        }
        append(" }")
    }
}
