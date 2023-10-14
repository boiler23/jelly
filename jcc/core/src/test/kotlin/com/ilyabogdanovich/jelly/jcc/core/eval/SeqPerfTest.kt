package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asRight
import io.kotest.matchers.doubles.shouldBeGreaterThan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import kotlin.time.measureTime

/**
 * Performance test for [Seq] map/reduce operations.
 *
 * @author Ilya Bogdanovich on 12.10.2023
 */
@Ignore
class SeqPerfTest {
    private inline fun measureAvgTime(block: () -> Unit): Double {
        return (1..10).map {
            measureTime(block).inWholeMilliseconds
        }.average()
    }

    @Test
    fun map(): Unit = runBlocking(Dispatchers.Default) {
        // Prepare
        val seq = Seq.fromBounds(1, 1_000_000)
        val mapper: (Var) -> Either<EvalError, Var> = {
            it as Var.NumVar
            Var.NumVar(it.v.pow(2.num)).asRight()
        }

        // Do
        print("Measuring map performance... ")
        val timeSequntial = measureAvgTime { seq.map(mapper) }
        val timeParallel = measureAvgTime { seq.parallelMap(mapper = mapper) }
        println("Done!")
        println("Time sequential: ${timeSequntial / 1000.0}s")
        println("Time parallel: ${timeParallel / 1000.0}s")

        // Check
        // >15% improvement is expected
        (1 - timeParallel / timeSequntial) * 100 shouldBeGreaterThan 15.0
    }

    @Test
    fun reduce(): Unit = runBlocking(Dispatchers.Default) {
        // Prepare
        val seq = Seq.fromBounds(1, 1_000_000)
        val reduction: (Var, Var) -> Either<EvalError, Var> = { acc, p ->
            acc as Var.NumVar
            p as Var.NumVar
            Var.NumVar(acc.v.pow(p.v)).asRight()
        }

        // Do
        print("Measuring reduce performance... ")
        val timeSequntial = measureAvgTime { seq.reduce(1.toVar(), reduction) }
        val timeParallel = measureAvgTime { seq.parallelReduce(1.toVar(), operation = reduction) }
        println("Done!")
        println("Time sequential: ${timeSequntial / 1000.0}s")
        println("Time parallel: ${timeParallel / 1000.0}s")

        // Check
        // >15% improvement is expected
        (1 - timeParallel / timeSequntial) * 100 shouldBeGreaterThan 15.0
    }
}
