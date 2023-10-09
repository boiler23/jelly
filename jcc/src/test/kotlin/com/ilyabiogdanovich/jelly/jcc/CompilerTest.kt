package com.ilyabiogdanovich.jelly.jcc

import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

/**
 * Integration test for [Compiler] components all together.
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
            arrayOf("out 0500", listOf("500"), empty()),
            arrayOf("out -500", listOf("-500"), empty()),
            arrayOf("out --500", listOf("500"), empty()),
            arrayOf("out +500", listOf("500"), empty()),
            arrayOf("out ++500", listOf("500"), empty()),
            arrayOf("out +-500", listOf("-500"), empty()),
            arrayOf("out -+500", listOf("-500"), empty()),
            arrayOf("out 0.456", listOf("0.456"), empty()),
            arrayOf("out .5", listOf("0.5"), empty()),
            arrayOf("out 0.0", listOf("0.0"), empty()),
            arrayOf("out 123.456", listOf("123.456"), empty()),
            arrayOf("out -123.456", listOf("-123.456"), empty()),
            arrayOf("out +123.456", listOf("123.456"), empty()),
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
            arrayOf(
                """
                    var n = 1.5
                    out n
                """.trimIndent(),
                listOf("1.5"),
                empty(),
            ),
            arrayOf(
                """
                    var n = 1
                    var n1 = n
                    print "n1="
                    out n1
                    print "n="
                    out n
                """.trimIndent(),
                listOf("n1=", "1", "n=", "1"),
                empty(),
            ),
            arrayOf(
                """
                    var n = 1
                    var n = 2
                    out n
                """.trimIndent(),
                listOf("1"),
                listOf("2:0: Variable redeclaration: `varn=2`."),
            ),
            arrayOf(
                """
                    var var = 1
                    out var
                """.trimIndent(),
                empty(),
                listOf(
                    "line 1:4 mismatched input 'var' expecting NAME",
                    "line 1:8 missing NAME at '='",
                    "line 2:4 mismatched input 'var' expecting {'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}",
                    "line 2:7 mismatched input '<EOF>' expecting NAME",
                    "1:0: Missing variable assignment: `var`.",
                    "2:4: Unsupported expression encountered: ``.",
                    "2:4: Missing variable assignment: `var`."
                ),
            ),
            arrayOf("out 2 + 3", listOf("5"), empty()),
            arrayOf("out 15 - 2.5", listOf("12.5"), empty()),
            arrayOf("out 15 / 5", listOf("3.0"), empty()),
            arrayOf("out 3 * 5", listOf("15"), empty()),
            arrayOf("out 2 ^ 5", listOf("32"), empty()),
            arrayOf("out 2 + 3 * 4", listOf("14"), empty()),
            arrayOf("out 2 ++ 3 * 4", listOf("14"), empty()),
            arrayOf("out (2 + 3) * 4", listOf("20"), empty()),
            arrayOf("out (1 + 3 * 5) / 4", listOf("4.0"), empty()),
            arrayOf("out 2 ^ (5 + 1)", listOf("64"), empty()),
            arrayOf("out 2^(5-1)", listOf("16"), empty()),
            arrayOf(
                """
                    var num1 = 12
                    var num2 = 3
                    var num3 = 2 + num1 / (10 - num2 * 2)
                    print "result = "
                    out num3
                """.trimIndent(),
                listOf("result = ", "5.0"),
                empty()
            ),
            arrayOf(
                """
                    var i = 5
                    var res = 2^(i + 1)
                    out res
                """.trimIndent(),
                listOf("64"),
                empty()
            ),
            arrayOf(
                """
                    var i = 5
                    var res = 2^(i+1)
                    out res
                """.trimIndent(),
                listOf("64"),
                empty()
            ),
        )
    }
}
