package com.ilyabiogdanovich.jelly.jcc.eval

/**
 * Represents the evaluated number, either integer or double.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
sealed interface Num {
    @JvmInline
    value class Integer(val v: Int) : Num
    @JvmInline
    value class Real(val r: Double): Num
}
