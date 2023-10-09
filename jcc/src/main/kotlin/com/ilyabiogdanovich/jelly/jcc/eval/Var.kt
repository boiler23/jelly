package com.ilyabiogdanovich.jelly.jcc.eval

/**
 * Represents the evaluated variable, either Number or Sequence.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
sealed interface Var {
    @JvmInline
    value class NumVar(val v: Num) : Var
    @JvmInline
    value class SeqVar(val v: Seq) : Var
}
