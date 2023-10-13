package com.ilyabogdanovich.jelly.jcc.core

import com.ilyabogdanovich.jelly.jcc.core.eval.toVar
import com.ilyabogdanovich.jelly.jcc.core.print.VarPrinter
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
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
    fun test() = runTest {
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
            arrayOf("out 0.0", listOf("0"), empty()),
            arrayOf("out 123.456", listOf("123.456"), empty()),
            arrayOf("out -123.456", listOf("-123.456"), empty()),
            arrayOf("out +123.456", listOf("123.456"), empty()),
            arrayOf("out 500;", listOf("500"), listOf("line 1:7 token recognition error at: ';'")),
            arrayOf("out 1abc", listOf("1"), empty()),
            arrayOf("out 1) out 2", listOf("1", "2"), listOf("line 1:5 extraneous input ')' expecting {<EOF>, 'print', 'out', 'var', 'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}")),
            arrayOf("print", empty(), listOf("line 1:5 missing STRING at '<EOF>'")),
            arrayOf("print \"text\"", listOf("text"), empty()),
            arrayOf("print \"text\";", listOf("text"), listOf("line 1:12 token recognition error at: ';'")),
            arrayOf("print \"line 1\nline 2\"", empty(), listOf("line 1:6 mismatched input '\"line 1' expecting STRING", "line 2:6 extraneous input '\"' expecting {<EOF>, 'print', 'out', 'var', 'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}")),
            arrayOf("print text", empty(), listOf("line 1:6 missing STRING at 'text'")),
            arrayOf("print \"'text'\"", listOf("'text'"), empty()),
            arrayOf("print \"text", empty(), listOf("line 1:6 mismatched input '\"text' expecting STRING")),
            arrayOf("print text\"", empty(), listOf("line 1:6 missing STRING at 'text'", "line 1:10 extraneous input '\"' expecting {<EOF>, 'print', 'out', 'var', 'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}")),
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
            arrayOf("out n", empty(), listOf("line 1:4 Variable undeclared: 'n'.")),
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
                listOf("line 2:0 Variable redeclaration: 'n'."),
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
                    "line 1:0 Missing variable assignment: 'var'.",
                    "line 2:4 Unsupported expression encountered: ''.",
                    "line 2:4 Missing variable assignment: 'var'."
                ),
            ),
            arrayOf("out 2 + 3", listOf("5"), empty()),
            arrayOf("out 15 - 2.5", listOf("12.5"), empty()),
            arrayOf("out 15 / 5", listOf("3"), empty()),
            arrayOf("out 3 * 5", listOf("15"), empty()),
            arrayOf("out 2 ^ 5", listOf("32"), empty()),
            arrayOf("out 2 + 3 * 4", listOf("14"), empty()),
            arrayOf("out 2 ++ 3 * 4", listOf("14"), empty()),
            arrayOf("out (2 + 3) * 4", listOf("20"), empty()),
            arrayOf("out (1 + 3 * 5) / 4", listOf("4"), empty()),
            arrayOf("out 2 ^ (5 + 1)", listOf("64"), empty()),
            arrayOf("out 2^(5-1)", listOf("16"), empty()),
            arrayOf("out 1 / 0", listOf("Infinity"), empty()),
            arrayOf("out -1/0", listOf("-Infinity"), empty()),
            arrayOf("out 0/0", listOf("NaN"), empty()),
            arrayOf(
                """
                    var num1 = 12
                    var num2 = 3
                    var num3 = 2 + num1 / (10 - num2 * 2)
                    print "result = "
                    out num3
                """.trimIndent(),
                listOf("result = ", "5"),
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
            arrayOf("out {1,5}", listOf("{ 1, 2, 3, 4, 5 }"), empty()),
            arrayOf("out {1,5-2}", listOf("{ 1, 2, 3 }"), empty()),
            arrayOf("out {1,5,7}", listOf("{ 1, 2, 3, 4, 5 }"), listOf("line 1:8 mismatched input ',' expecting {PLUSMINUS, MULDIV, '^', '}'}", "line 1:10 extraneous input '}' expecting {<EOF>, 'print', 'out', 'var', 'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}")),
            arrayOf("out {-2,3}", listOf("{ -2, -1, 0, 1, 2, 3 }"), empty()),
            arrayOf("out {5,1}", empty(), listOf("line 1:4 Sequence's upper bound is less than lower bound: 5 > 1.")),
            arrayOf("out {1}", empty(), listOf("line 1:6 mismatched input '}' expecting {PLUSMINUS, MULDIV, '^', ','}", "line 1:4 Missing sequence's upper bound: '{1}'.")),
            arrayOf("out {}", empty(), listOf("line 1:5 mismatched input '}' expecting {'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}", "line 1:4 Missing sequence's upper bound: '{}'.")),
            arrayOf(
                """
                    var from = 2
                    var to = 8
                    var seq = {from,to-3}
                    out seq
                """.trimIndent(),
                listOf("{ 2, 3, 4, 5 }"),
                empty()
            ),
            arrayOf("out map({1,5},i->i^2)", listOf("{ 1, 4, 9, 16, 25 }"), empty()),
            arrayOf("out map({1,5},i->{i,i+2})", listOf("{ { 1, 2, 3 }, { 2, 3, 4 }, { 3, 4, 5 }, { 4, 5, 6 }, { 5, 6, 7 } }"), empty()),
            arrayOf("out reduce(map({1,5},i->i^2), 1, x y -> x*y)", listOf("14400"), empty()),
            arrayOf("out reduce({1,5},0,i j->i+j)", listOf("15"), empty()),
            arrayOf(
                """
                    var r = reduce(
                        map(
                            map(
                                { 1, 5 },
                                i -> map({i, i + 2}, j -> j)
                            ),
                            e -> reduce(e, 0, x y -> x + y)
                        ),
                        0,
                        x y -> x + y
                    )
                    out r
                """.trimIndent(),
                listOf("60"),
                empty()
            ),
            arrayOf(
                """
                    var n = 500
                    var seq = map({0, n}, i -> (-1)^i / (2 * i + 1))
                    var pi = 4 * reduce(seq, 0, x y -> x + y)
                    print "pi = "
                    out pi
                """.trimIndent(),
                listOf("pi = ", calculatePi()),
                empty()
            ),
            arrayOf(
                "out 4 * reduce(map({0, 500}, i -> (-1)^i / (2 * i + 1)), 0, x y -> x + y)",
                listOf(calculatePi()),
                empty()
            ),
            arrayOf(
                """
                    var n = map({0, 5}, i -> i^2)
                    out i
                """.trimIndent(),
                empty(),
                listOf("line 2:4 Variable undeclared: 'i'."),
            ),
            arrayOf(
                """
                    map({0, 5}, i -> i^2)
                    out 1
                """.trimIndent(),
                listOf("1"),
                empty(),
            ),
            arrayOf(
                """
                    var i = 5
                    var seq = 2^i / 2 * i + 1)
                    print "seq = "
                    out seq
                """.trimIndent(),
                listOf("seq = ", "81"),
                listOf("line 2:25 extraneous input ')' expecting {<EOF>, 'print', 'out', 'var', 'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}"),
            )
        )

        /**
         * This one is used to verify the reference example.
         * @return pi value approximation
         */
        private fun calculatePi() =
            VarPrinter().print((
                4.0 * (0..500)
                    .map { i -> (if (i % 2 == 0) 1.0 else -1.0) / (2 * i + 1) }
                    .fold(0.0) { x, y -> x + y }
                ).toVar())
    }
}
