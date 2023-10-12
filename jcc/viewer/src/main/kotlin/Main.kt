import com.ilyabogdanovich.jelly.jcc.core.Compiler

fun main() {
    Compiler().view("""
        out (2 + 3) * 4
    """.trimIndent())
}
