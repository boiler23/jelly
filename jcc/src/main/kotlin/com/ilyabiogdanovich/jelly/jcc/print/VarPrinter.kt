package com.ilyabiogdanovich.jelly.jcc.print

import com.ilyabiogdanovich.jelly.jcc.eval.Num
import com.ilyabiogdanovich.jelly.jcc.eval.Seq
import com.ilyabiogdanovich.jelly.jcc.eval.Var
import java.text.DecimalFormat
import java.util.Locale


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
        is Num.Real -> DecimalFormat.getInstance(Locale.ROOT).format(r)
    }

    private fun Seq.print() = buildString {
        append("{ ")
        if (from <= to) {
            for (n in from until to) {
                append("$n, ")
            }
            append(to)
        }
        append(" }")
    }
}
