package com.ilyabogdanovich.jelly.jcc.core.print

import com.ilyabogdanovich.jelly.jcc.core.eval.Num
import com.ilyabogdanovich.jelly.jcc.core.eval.Seq
import com.ilyabogdanovich.jelly.jcc.core.eval.Var
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

/**
 * Printer for variables. This printer is used in "out" command of our language.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
internal class VarPrinter {
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
            when {
                r == Double.POSITIVE_INFINITY -> "Infinity"
                r == Double.NEGATIVE_INFINITY -> "-Infinity"
                r.isNaN() -> "NaN"
                else -> {
                    val bd = BigDecimal(r).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                    if (abs(r) <= 1e-4 || abs(r) >= 1e+8) {
                        bd.toString()
                    } else {
                        bd.toPlainString()
                    }
                }
            }
        }
    }

    private fun Seq.print() = buildString {
        append("{ ")
        val i = elements.iterator()
        while (i.hasNext()) {
            append(i.next().print())
            if (i.hasNext()) {
                append(", ")
            }
        }
        append(" }")
    }
}
