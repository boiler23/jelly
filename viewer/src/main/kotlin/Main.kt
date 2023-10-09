import com.ilyabiogdanovich.jelly.jcc.Compiler

fun main() {
    Compiler().view("""
        out (2 + 3) * 4
    """.trimIndent())
}
