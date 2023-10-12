import com.ilyabogdanovich.jelly.jcc.eval.EvalError
import com.ilyabogdanovich.jelly.jcc.eval.Seq
import com.ilyabogdanovich.jelly.jcc.eval.Var
import com.ilyabogdanovich.jelly.jcc.eval.map
import com.ilyabogdanovich.jelly.jcc.eval.num
import com.ilyabogdanovich.jelly.jcc.eval.parallelMap
import com.ilyabogdanovich.jelly.jcc.eval.parallelReduce
import com.ilyabogdanovich.jelly.jcc.eval.reduce
import com.ilyabogdanovich.jelly.jcc.eval.toVar
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
    val seq = Seq.Bounds(1, 10_000_000)
    val mapper: (Var) -> Either<EvalError, Var> = {
        it as Var.NumVar
        Var.NumVar(it.v.pow(2.num)).asRight()
    }

    print("Measuring map performance... ")
    val timeSequntial = measureAvgTime { seq.map(mapper) }
    val timeParallel = measureAvgTime { seq.parallelMap(mapper = mapper) }
    println("Done!")
    println("Time sequential: ${timeSequntial/1000.0}s")
    println("Time parallel: ${timeParallel/1000.0}s")
    println("Improvement: ${improvement(from = timeSequntial, to = timeParallel)}%")
}

private suspend fun reduce() {
    val seq = Seq.Bounds(1, 10_000_000)
    val reduction: (Var, Var) -> Either<EvalError, Var> = { acc, p ->
        acc as Var.NumVar
        p as Var.NumVar
        Var.NumVar(acc.v.pow(p.v)).asRight()
    }

    print("Measuring reduce performance... ")
    val timeSequntial = measureAvgTime { seq.reduce(1.toVar(), reduction) }
    val timeParallel = measureAvgTime { seq.parallelReduce(1.toVar(), operation = reduction) }
    println("Done!")
    println("Time sequential: ${timeSequntial/1000.0}s")
    println("Time parallel: ${timeParallel/1000.0}s")
    println("Improvement: ${improvement(from = timeSequntial, to = timeParallel)}%")
}

fun main() = runBlocking(Dispatchers.Default) {
    map()
    reduce()
}
