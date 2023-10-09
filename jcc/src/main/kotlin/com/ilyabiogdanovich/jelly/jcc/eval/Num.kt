package com.ilyabiogdanovich.jelly.jcc.eval

import kotlin.math.pow

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

    private val doubleValue: Double
        get() = when(this) {
            is Integer -> v.toDouble()
            is Real -> r
        }

    operator fun plus(n: Num): Num {
        return when (this) {
            is Integer -> when (n) {
                is Integer -> Integer(v + n.v)
                is Real -> Real(v + n.r)
            }
            is Real -> Real(r + n.doubleValue)
        }
    }

    operator fun minus(n: Num): Num {
        return when (this) {
            is Integer -> when (n) {
                is Integer -> Integer(v - n.v)
                is Real -> Real(v - n.r)
            }
            is Real -> Real(r - n.doubleValue)
        }
    }

    operator fun times(n: Num): Num {
        return when (this) {
            is Integer -> when (n) {
                is Integer -> Integer(v * n.v)
                is Real -> Real(v * n.r)
            }
            is Real -> Real(r * n.doubleValue)
        }
    }

    operator fun div(n: Num): Num {
        return when (this) {
            is Integer -> when (n) {
                is Integer -> Real(v.toDouble() / n.v)
                is Real -> Real(v / n.r)
            }
            is Real -> Real(r / n.doubleValue)
        }
    }

    fun pow(n: Num): Num {
        return when (this) {
            is Integer -> when (n) {
                is Integer -> Integer(doubleValue.pow(n.doubleValue).toInt())
                is Real -> Real(doubleValue.pow(n.r))
            }
            is Real -> Real(r.pow(n.doubleValue))
        }
    }
}
