package com.ilyabiogdanovich.jelly.jcc.eval

import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import com.ilyabogdanovich.jelly.utils.mapEitherRight
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    @JvmInline
    value class Array(val elements: List<Var>) : Seq
}

/**
 * Parallel mapping operation for the sequence.
 * It maps every element of the sequence with the given [mapper],
 * which can return either new [Var] or [EvalError].
 * In the latter case, whole mapping operation returns error.
 * The sequence is split into a number of chunks, according to the [maxParallelism] parameter,
 * each of those chunks is transformed in parallel.
 *
 * @param maxParallelism defines the maximum possible parallel mappings running, effectively defining the chunk size.
 * @param mapper mapper used for this operation.
 * @return wither [Seq] or [EvalError].
 */
suspend inline fun Seq.parallelMap(
    maxParallelism: Int = Runtime.getRuntime().availableProcessors(),
    crossinline mapper: suspend (Var) -> Either<EvalError, Var>
): Either<EvalError, Seq> = with(asArray()) {
    coroutineScope {
        val out = elements
            .chunked(getParallelChunkSize(maxParallelism))
            .map { chunk ->
                async {
                    val output = ArrayList<Var>(elements.size)
                    for (e in chunk) {
                        when (val mapResult = mapper(e)) {
                            is Either.Left -> return@async mapResult.value.asLeft()
                            is Either.Right -> output.add(mapResult.value)
                        }
                    }
                    output.asRight<EvalError, List<Var>>()
                }
            }
            .awaitAll()
        val error = out.find { it is Either.Left }
        if (error != null) {
            error as Either.Left
            error.value.asLeft()
        } else {
            out.map { it as Either.Right; it.value }.flatten().toSeq().asRight()
        }
    }
}

/**
 * Sequential mapping operation for the sequence.
 * It maps every element of the sequence with the given [mapper],
 * which can return either new [Var] or [EvalError].
 * In the latter case, whole mapping operation returns error.
 * The sequence elements are processed one by one, without any parallelism.
 *
 * @param mapper mapper used for this operation.
 * @return wither [Seq] or [EvalError].
 */
inline fun Seq.map(
    crossinline mapper: (Var) -> Either<EvalError, Var>
): Either<EvalError, Seq> = with(asArray()) {
    val output = ArrayList<Var>(elements.size)
    for (e in elements) {
        when (val mapResult = mapper(e)) {
            is Either.Left -> return@with mapResult.value.asLeft()
            is Either.Right -> output.add(mapResult.value)
        }
    }
    output.toSeq().asRight()
}

/**
 * Parallel reduce operation for the sequence.
 * It reduces this sequence to a [Var] using the given [neutral] value and reduce [operation],
 * which can return either new [Var] or [EvalError].
 * In the latter case, whole reducing operation returns error.
 * The sequence is split into a number of chunks, according to the [maxParallelism] parameter,
 * each of those chunks is transformed in parallel.
 * In order for this parallel computing to run correctly, [operation] has to be associative.
 * Otherwise, correctness of the final result is not guaranteed!
 *
 * @param neutral neutral element, which is used as an initial accumulation value.
 * @param maxParallelism defines the maximum possible parallel reductions running, effectively defining the chunk size.
 * @param operation associative reducer used for this operation.
 *                  It takes to parameters as an input: first one is the accumulated value,
 *                  second one is the next element picked.
 *                  Returned result is used to update the accumulated value.
 * @return either [Seq] or [EvalError].
 */
suspend inline fun Seq.parallelReduce(
    neutral: Var,
    maxParallelism: Int = Runtime.getRuntime().availableProcessors(),
    crossinline operation: suspend (Var, Var) -> Either<EvalError, Var>
): Either<EvalError, Var> = with(asArray()) {
    coroutineScope {
        elements
            .asSequence()
            .chunked(getParallelChunkSize(maxParallelism))
            .map { chunk ->
                async {
                    var accumulator = neutral
                    for (element in chunk) {
                        when (val operationResult = operation(accumulator, element)) {
                            is Either.Left -> return@async operationResult.value.asLeft()
                            is Either.Right -> accumulator = operationResult.value
                        }
                    }
                    accumulator.asRight<EvalError, Var>()
                }
            }
            .toList()
            .awaitAll()
            .reduce { acc, p ->
                acc.mapEitherRight { accVal -> p.mapEitherRight { pVal -> operation(accVal, pVal) } }
            }
    }
}

/**
 * Sequential reduce operation for the sequence.
 * It reduces this sequence to a [Var] using the given [neutral] value and reduce [operation],
 * which can return either new [Var] or [EvalError].
 * In the latter case, whole reducing operation returns error.
 * The sequence elements are processed one by one, without any parallelism.
 *
 * @param neutral neutral element, which is used as an initial accumulation value.
 * @param operation reducer used for this operation.
 *                  It takes to parameters as an input: first one is the accumulated value,
 *                  second one is the next element picked.
 *                  Returned result is used to update the accumulated value.
 * @return either [Seq] or [EvalError].
 */
inline fun Seq.reduce(
    neutral: Var,
    crossinline operation: (Var, Var) -> Either<EvalError, Var>
): Either<EvalError, Var> = with(asArray()) {
    var accumulator = neutral
    for (element in elements) {
        when (val operationResult = operation(accumulator, element)) {
            is Either.Left -> return operationResult.value.asLeft()
            is Either.Right -> accumulator = operationResult.value
        }
    }
    return accumulator.asRight()
}

/**
 * Helper to decide on the chunk size for the parallel computing.
 */
@PublishedApi
internal fun Seq.Array.getParallelChunkSize(maxParallelism: Int): Int {
    return ceil(elements.size / maxParallelism.toDouble()).toInt()
}

/**
 * Helper, used in map/reduce operations, to always represent this sequence as an [Array].
 */
@PublishedApi
internal fun Seq.asArray() = when (this) {
    is Seq.Bounds -> Seq.Array((from..to).map { Var.NumVar(Num.Integer(it)) })
    is Seq.Array -> this
}

/**
 * Helper to create [Seq.Array] from list of [Var]'s.
 */
fun List<Var>.toSeq() = Seq.Array(this)
