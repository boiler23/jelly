package com.ilyabiogdanovich.jelly.jcc

import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

/**
 * Test for [Compiler]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
@RunWith(Parameterized::class)
class CompilerTest(
    private val src: String,
    private val output: List<String>,
    private val errors: List<String>,
) {
    private val compiler = Compiler()

    @Before
    fun setUp() {
        Locale.setDefault(Locale.ROOT)
    }

    @Test
    fun test() {
        // Prepare

        // Do
        val result = compiler.compile(src)

        // Check
        result shouldBe Compiler.Result(
            output = output,
            errors = errors,
        )
    }

    companion object {
        private fun empty() = listOf<String>()

        @JvmStatic
        @Parameterized.Parameters(name = "src={0}; out={1}; err={2}")
        fun getTestData(): List<Array<Any>> = listOf(
            arrayOf("", empty(), empty()),
            arrayOf("out 500", listOf("500"), empty()),
            arrayOf("out 123.456", listOf("123.456"), empty()),
            arrayOf("out 500;", listOf("500"), listOf("line 1:7 token recognition error at: ';'")),
            arrayOf("print", empty(), listOf("line 1:5 missing STRING at '<EOF>'")),
            arrayOf("print \"text\"", listOf("text"), empty()),
            arrayOf("print \"text\";", listOf("text"), listOf("line 1:12 token recognition error at: ';'")),
            arrayOf("print \"line 1\nline 2\"", empty(), listOf("line 1:6 mismatched input '\"line 1' expecting STRING")),
            arrayOf("print text", empty(), listOf("line 1:6 mismatched input 'text' expecting STRING")),
            arrayOf("print \"'text'\"", listOf("'text'"), empty()),
            arrayOf("print \"text", empty(), listOf("line 1:6 mismatched input '\"text' expecting STRING")),
            arrayOf("print text\"", empty(), listOf("line 1:6 mismatched input 'text' expecting STRING")),
            arrayOf("print \"te\\\"xt\"", listOf("te\"xt"), empty()),
            arrayOf("print \"te\\xt\"", listOf("te\\xt"), empty()),
            arrayOf(
                """
                    print "pi = "
                    out 3.1415926
                """.trimIndent(),
                listOf("pi = ", "3.1415926"),
                empty(),
            ),
        )
    }
}
