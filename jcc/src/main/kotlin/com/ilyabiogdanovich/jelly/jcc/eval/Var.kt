package com.ilyabiogdanovich.jelly.jcc.eval

/**
 * Represents the evaluated variable, either Number or Sequence.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
sealed interface Var {
    /**
     * Represents a numeric variable.
     */
    @JvmInline
    value class NumVar(val v: Num) : Var

    /**
     * Represents a sequence variable.
     */
    @JvmInline
    value class SeqVar(val v: Seq) : Var
}

/**
 * Helper to create [Var] from [Int].
 */
fun Int.toVar() = Var.NumVar(this.num)

/**
 * Helper to create [Var] from [Double].
 */
fun Double.toVar() = Var.NumVar(this.num)

/**
 * Helper to create sequence [Var] from a list of [Var]'s.
 */
fun List<Var>.toVar() = Var.SeqVar(this.toSeq())
