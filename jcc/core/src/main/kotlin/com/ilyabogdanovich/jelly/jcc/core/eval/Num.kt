package com.ilyabogdanovich.jelly.jcc.core.eval

import org.apache.commons.math3.util.FastMath

/**
 * Represents the evaluated number, either integer or double.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
sealed interface Num {
    /**
     * Binary plus operator.
     * @param n right operand.
     * @return [Real] if one of the operands is [Real], otherwise - [Integer].
     */
    operator fun plus(n: Num): Num

    /**
     * Binary minus operator.
     * @param n right operand.
     * @return [Real] if one of the operands is [Real], otherwise - [Integer].
     */
    operator fun minus(n: Num): Num

    /**
     * Binary multiply operator.
     * @param n right operand.
     * @return [Real] if one of the operands is [Real], otherwise - [Integer].
     */
    operator fun times(n: Num): Num

    /**
     * Binary divide operator.
     * @param n right operand (denominator).
     * @return [Real] if one of the operands is [Real], otherwise - [Integer].
     */
    operator fun div(n: Num): Num

    /**
     * Exponentiation operation.
     * @param n power to exponentiate into this number.
     * @return [Real] if one of the operands is [Real], otherwise - [Integer].
     */
    fun pow(n: Num): Num

    /**
     * Unary minus operator.
     * @return negated value, not changing its type.
     */
    operator fun unaryMinus(): Num

    /**
     * Represents the integer number.
     */
    @JvmInline
    value class Integer(val v: Int) : Num {
        override operator fun plus(n: Num) = when (n) {
            is Integer -> Integer(v + n.v)
            is Real -> Real(v + n.r)
        }

        override operator fun minus(n: Num) = when (n) {
            is Integer -> Integer(v - n.v)
            is Real -> Real(v - n.r)
        }

        override operator fun times(n: Num) = when (n) {
            is Integer -> Integer(v * n.v)
            is Real -> Real(v * n.r)
        }

        override operator fun div(n: Num): Num = when (n) {
            is Integer -> Real(v.toDouble() / n.v)
            is Real -> Real(v / n.r)
        }

        override operator fun unaryMinus() = Integer(-v)

        override fun pow(n: Num) = when (n) {
            is Integer -> {
                when (v) {
                    1 -> Integer(1)
                    -1 -> Integer(if (n.v % 2 == 0) 1 else -1)
                    else -> Integer(FastMath.pow(v.toDouble(), n.v).toInt())
                }
            }
            is Real -> {
                Real(FastMath.pow(v.toDouble(), n.r))
            }
        }
    }

    /**
     * Represents the decimal number.
     */
    @JvmInline
    value class Real(val r: Double): Num {
        override operator fun plus(n: Num) = Real(r + n.doubleValue)
        override operator fun minus(n: Num) = Real(r - n.doubleValue)
        override operator fun times(n: Num) = Real(r * n.doubleValue)
        override operator fun div(n: Num) = Real(r / n.doubleValue)
        override operator fun unaryMinus() = Real(-r)
        override fun pow(n: Num) = when (n) {
            is Integer -> Real(FastMath.pow(r, n.v))
            is Real -> Real(FastMath.pow(r, n.r))
        }
    }

    private val doubleValue: Double
        get() = when(this) {
            is Integer -> v.toDouble()
            is Real -> r
        }
}

/**
 * Helper property to create [Num.Integer] from [Int].
 */
val Int.num
    get() = Num.Integer(this)

/**
 * Helper property to create [Num.Real] from [Double].
 */
val Double.num
    get() = Num.Real(this)
