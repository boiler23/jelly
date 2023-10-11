package com.ilyabiogdanovich.jelly.jcc.eval

import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import com.ilyabogdanovich.jelly.utils.mapEitherRight
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.ceil

/**
 * Represents the evaluated sequence.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
sealed interface Seq {
    /**
     * Sequence defined by its lower and upper bound only.
     */
    data class Bounds(val from: Int, val to: Int) : Seq

    /**
     * Sequence defined by a list of values.
     */
    data class Array(val elements: List<Var>) : Seq
}

/**
 * Parallel mapping operation for the sequence.
 * It maps every element of the sequence with the given [mapper],
 * which can return either new [Var] or [EvalError].
 * In the latter case, whole mapping operation returns error.
 * The sequence is split into a number of chunks, according to the maximum parallelism allowed in the system,
 * each of those chunks is transformed in parallel.
 *
 * @param mapper mapper used for this operation.
 * @return wither [Seq] or [EvalError].
 */
internal suspend inline fun Seq.parallelMap(
    crossinline mapper: suspend (Var) -> Either<EvalError, Var>
): Either<EvalError, Seq> = with(asArray()) {
    coroutineScope {
        val out = elements
            .chunked(getParallelChunkSize())
            .map { chunk ->
                async { chunk.map { mapper(it) } }
            }
            .flatMap { it.await() }
        val error = out.find { it is Either.Left }
        if (error != null) {
            error as Either.Left
            error.value.asLeft()
        } else {
            Seq.Array(out.filterIsInstance<Either.Right<EvalError, Var>>().map { it.value }).asRight()
        }
    }
}

/**
 * Parallel reduce operation for the sequence.
 * It reduces this sequence to a [Var] using the given [neutral] value and reduce [operation],
 * which can return either new [Var] or [EvalError].
 * In the latter case, whole reducing operation returns error.
 * The sequence is split into a number of chunks, according to the maximum parallelism allowed in the system,
 * each of those chunks is transformed in parallel.
 * In order for this parallel computing run correctly, [operation] has to be associative.
 * Otherwise, correctness of the final result is not guaranteed!
 *
 * @param neutral neutral element, which is used as an initial accumulation value.
 * @param operation associative reducer used for this operation.
 *                  It takes to parameters as an input: first one is the accumulated value,
 *                  second one is the next element picked.
 *                  Returned result is used to update the accumulated value.
 * @return wither [Seq] or [EvalError].
 */
internal suspend inline fun Seq.parallelReduce(
    neutral: Var,
    crossinline operation: suspend (Var, Var) -> Either<EvalError, Var>
): Either<EvalError, Var> = with(asArray()) {
    coroutineScope {
        elements
            .chunked(getParallelChunkSize())
            .map { chunk ->
                async {
                    chunk.fold<Var, Either<EvalError, Var>>(neutral.asRight()) { acc, p ->
                        when (acc) {
                            is Either.Left -> acc
                            is Either.Right -> operation(acc.value, p)
                        }
                    }
                }
            }
            .map { it.await() }
            .toList()
            .reduce { acc, p ->
                acc.mapEitherRight { accVal -> p.mapEitherRight { pVal -> operation(accVal, pVal) } }
            }
    }
}

/**
 * Helper to decide on the chunk size for the parallel computing.
 */
private fun Seq.Array.getParallelChunkSize(): Int {
    val chunkCount = Runtime.getRuntime().availableProcessors()
    return ceil(elements.size / chunkCount.toDouble()).toInt()
}

/**
 * Helper, used in map/reduce operations, to always represent this sequence as an [Array].
 */
private fun Seq.asArray() = when (this) {
    is Seq.Bounds -> Seq.Array((from..to).map { Var.NumVar(Num.Integer(it)) })
    is Seq.Array -> this
}

/**
 * Helper to create [Seq.Array] from list of [Var]'s.
 */
fun List<Var>.toSeq() = Seq.Array(this)
