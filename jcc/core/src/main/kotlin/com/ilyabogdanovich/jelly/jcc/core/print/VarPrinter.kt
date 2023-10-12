package com.ilyabogdanovich.jelly.jcc.core.print

import com.ilyabogdanovich.jelly.jcc.core.eval.Num
import com.ilyabogdanovich.jelly.jcc.core.eval.Seq
import com.ilyabogdanovich.jelly.jcc.core.eval.Var
import java.math.BigDecimal
import java.math.RoundingMode

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
        is Num.Real -> {
            val bd = BigDecimal(r)
            bd.setScale(9, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        }
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
        val i = elements.iterator()
        while (i.hasNext()) {
            append(print(i.next()))
            if (i.hasNext()) {
                append(", ")
            }
        }
        append(" }")
    }
}
