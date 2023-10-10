package com.ilyabiogdanovich.jelly.jcc.eval

import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight

/**
 * Represents the evaluated sequence.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
sealed interface Seq {
    /**
     * Map operation for the sequence.
     * It maps every element of the sequence with the given [mapper],
     * which can return either new [Var] or [EvalError].
     * In the latter case, whole mapping operation returns error.
     *
     * @param mapper mapper used for this operation.
     * @return wither [Seq] or [EvalError].
     */
    fun map(mapper: (Var) -> Either<EvalError, Var>): Either<EvalError, Seq>

    /**
     * Reduce operation for the sequence.
     * It reduces this sequence to a [Var] using the given [neutral] value and reduce [operation],
     * which can return either new [Var] or [EvalError].
     * In the latter case, whole reducing operation returns error.
     *
     * @param neutral neutral element, which is used as an initial accumulation value.
     * @param operation reducer used for this operation.
     *                  It takes to parameters as an input: first one is the accumulated value,
     *                  second one is the next element picked.
     *                  Returned result is used to update the accumulated value.
     * @return wither [Seq] or [EvalError].
     */
    fun reduce(neutral: Var, operation: (Var, Var) -> Either<EvalError, Var>): Either<EvalError, Var>

    /**
     * Sequence defined by its lower and upper bound only.
     */
    data class Bounds(val from: Int, val to: Int) : Seq {
        override fun map(mapper: (Var) -> Either<EvalError, Var>): Either<EvalError, Seq> =
            Array((from..to).map { Var.NumVar(Num.Integer(it)) }).map(mapper)

        override fun reduce(neutral: Var, operation: (Var, Var) -> Either<EvalError, Var>): Either<EvalError, Var> =
            Array((from..to).map { Var.NumVar(Num.Integer(it)) }).reduce(neutral, operation)
    }

    /**
     * Sequence defined by a list of values.
     */
    data class Array(val elements: List<Var>) : Seq {
        override fun map(mapper: (Var) -> Either<EvalError, Var>): Either<EvalError, Seq> {
            val output = mutableListOf<Var>()
            for (e in elements) {
                when (val mapped = mapper(e)) {
                    is Either.Left -> return mapped.value.asLeft()
                    is Either.Right -> output.add(mapped.value)
                }
            }
            return Array(output).asRight()
        }

        override fun reduce(neutral: Var, operation: (Var, Var) -> Either<EvalError, Var>): Either<EvalError, Var> {
            var accumulated = neutral
            for (e in elements) {
                when (val transformed = operation(accumulated, e)) {
                    is Either.Left -> return transformed.value.asLeft()
                    is Either.Right -> {
                        accumulated = transformed.value
                    }
                }
            }
            return accumulated.asRight()
        }
    }
}

/**
 * Helper to create [Seq.Array] from list of [Var]'s.
 */
fun List<Var>.toSeq() = Seq.Array(this)
