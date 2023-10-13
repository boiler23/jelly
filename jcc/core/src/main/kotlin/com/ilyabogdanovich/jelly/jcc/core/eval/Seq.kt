package com.ilyabogdanovich.jelly.jcc.core.eval

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
 * Stored using [Sequence] object and it's size.
 * It helps to reduce memory footprint for storing simple sequences like {0,10000},
 * because memory allocation is not required.
 * Once the map operation is performed, new mapped sequence is stored in memory effectively as an array.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class Seq(val elements: Sequence<Var>, val size: Int) {
    // equals & hashCode are needed to simplify test cases.
    // they aren't used in production code.
    // quick dirty, but quick solution.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Seq

        if (elements.toList() != other.elements.toList()) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = elements.toList().hashCode()
        result = 31 * result + size
        return result
    }

    companion object {
        fun fromBounds(from: Int, to: Int): Seq {
            if (from > to) {
                throw IllegalArgumentException("Sequence from > to: $from > $to")
            }
            return Seq((from..to).asSequence().map { it.toVar() }, to - from + 1)
        }
    }
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
): Either<EvalError, Seq> {
    return coroutineScope {
        if (size == 0) {
            return@coroutineScope this@parallelMap.asRight()
        }

        val out = elements
            .chunked(size.getParallelChunkSize(maxParallelism))
            .map { chunk ->
                async {
                    val output = ArrayList<Var>(size)
                    for (e in chunk) {
                        when (val mapResult = mapper(e)) {
                            is Either.Left -> return@async mapResult.value.asLeft()
                            is Either.Right -> output.add(mapResult.value)
                        }
                    }
                    output.asRight<EvalError, List<Var>>()
                }
            }
            .toList()
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
): Either<EvalError, Seq> {
    val output = ArrayList<Var>(size)
    for (e in elements) {
        when (val mapResult = mapper(e)) {
            is Either.Left -> return mapResult.value.asLeft()
            is Either.Right -> output.add(mapResult.value)
        }
    }
    return output.toSeq().asRight()
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
): Either<EvalError, Var> {
    return coroutineScope {
        if (size == 0) {
            return@coroutineScope neutral.asRight()
        }
        
        elements
            .chunked(size.getParallelChunkSize(maxParallelism))
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
): Either<EvalError, Var> {
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
internal fun Int.getParallelChunkSize(maxParallelism: Int): Int {
    return ceil(this / maxParallelism.toDouble()).toInt()
}

/**
 * Helper to create [Seq] from list of [Var]'s.
 */
fun List<Var>.toSeq() = Seq(asSequence(), size)
