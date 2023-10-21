import com.ilyabogdanovich.jelly.jcc.core.ParseTreeViewer

fun main() {
    val parseTreeViewer = ParseTreeViewer()
    parseTreeViewer.run(
        """
            map({0, 5}, i -> i^2)
            out 1
        """.trimIndent()
    )
}
