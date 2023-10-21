package com.ilyabogdanovich.jelly.jcc.core.eval

/**
 * Represents the evaluated variable, either Number or Sequence.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
internal sealed interface Var {
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
internal fun Int.toVar() = Var.NumVar(this.num)

/**
 * Helper to create [Var] from [Long].
 */
internal fun Long.toVar() = Var.NumVar(this.num)

/**
 * Helper to create [Var] from [Double].
 */
internal fun Double.toVar() = Var.NumVar(this.num)

/**
 * Helper to create [Var] from [Num].
 */
internal fun Num.toVar() = Var.NumVar(this)
