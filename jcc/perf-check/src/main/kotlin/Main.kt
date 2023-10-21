import com.ilyabogdanovich.jelly.jcc.core.Error
import com.ilyabogdanovich.jelly.jcc.core.di.CompilationServiceApi
import com.ilyabogdanovich.jelly.jcc.core.eval.Num
import com.ilyabogdanovich.jelly.jcc.core.eval.Seq
import com.ilyabogdanovich.jelly.jcc.core.eval.map
import com.ilyabogdanovich.jelly.jcc.core.eval.num
import com.ilyabogdanovich.jelly.jcc.core.eval.parallelMap
import com.ilyabogdanovich.jelly.jcc.core.eval.parallelReduce
import com.ilyabogdanovich.jelly.jcc.core.eval.reduce
import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asRight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime

private inline fun measureAvgTime(block: () -> Unit): Double {
    return (1..10).map {
        measureTime(block).inWholeMilliseconds
    }.average()
}

private fun improvement(from: Double, to: Double): Int {
    return ((1.0 - to / from) * 100.0).toInt()
}

private suspend fun map() {
    val seq = Seq.fromBounds(1, 10_000_000)
    val mapper: (Num) -> Either<Error, Num> = { (it.pow(3.num) + it.pow(2.num) + 1.num).asRight() }

    print("Measuring map performance... ")
    val timeSequntial = measureAvgTime { seq.map(mapper) }
    val timeParallel = measureAvgTime { seq.parallelMap(mapper = mapper) }
    println("Done!")
    println("Time sequential: ${timeSequntial / 1000.0}s")
    println("Time parallel: ${timeParallel / 1000.0}s")
    println("Improvement: ${improvement(from = timeSequntial, to = timeParallel)}%")
}

private suspend fun reduce() {
    val seq = Seq.fromBounds(1, 10_000_000)
    val reduction: (Num, Num) -> Either<Error, Num> = { acc, p ->
        (acc.pow(p) * p.pow(acc)).asRight()
    }

    print("Measuring reduce performance... ")
    val timeSequntial = measureAvgTime { seq.reduce(1.num, reduction) }
    val timeParallel = measureAvgTime { seq.parallelReduce(1.num, operation = reduction) }
    println("Done!")
    println("Time sequential: ${timeSequntial / 1000.0}s")
    println("Time parallel: ${timeParallel / 1000.0}s")
    println("Improvement: ${improvement(from = timeSequntial, to = timeParallel)}%")
}

private suspend fun powerPerformance() {
    val compiler = CompilationServiceApi.create().compilationService
    print("Measuring calculate power performance...")
    val time = measureAvgTime {
        compiler.compile(
            """
                var seq = map({1, 1000000}, x -> x^(1000000 - x/2))
                out seq
            """.trimIndent()
        )
    }
    println("Done!")
    println("Average time spent: ${time / 1000.0}s")
}

fun main() = runBlocking(Dispatchers.Default) {
    map()
    reduce()
    powerPerformance()
}
