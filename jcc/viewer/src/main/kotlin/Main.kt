import com.ilyabogdanovich.jelly.jcc.core.di.CompilationServiceApi

fun main() {
    val parseTreeViewer = CompilationServiceApi.create().parseTreeViewer
    parseTreeViewer.run(
        """
            map({0, 5}, i -> i^2)
            out 1
        """.trimIndent()
    )
}
