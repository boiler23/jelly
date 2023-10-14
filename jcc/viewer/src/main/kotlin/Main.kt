import com.ilyabogdanovich.jelly.jcc.core.Compiler

fun main() {
    Compiler().view(
        """
            map({0, 5}, i -> i^2)
            out 1
        """.trimIndent()
    )
}
