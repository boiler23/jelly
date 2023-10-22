package com.ilyabogdanovich.jelly.jcc.core

import com.ilyabogdanovich.jelly.jcc.core.di.CompilationServiceApi
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
 * Integration test for [CompilationServiceApi] components all together.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
@RunWith(Parameterized::class)
class CompilationServiceIntegrationTest(
    private val src: String,
    private val output: String,
    private val errors: List<String>,
) {
    private val compiler = CompilationServiceApi.create().compilationService

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
        result.output shouldBe output
        result.errors.map { it.formattedMessage } shouldBe errors
    }

    companion object {
        private fun empty() = listOf<String>()

        @JvmStatic
        @Parameterized.Parameters(name = "src={0}; out={1}; err={2}")
        fun getTestData(): List<Array<Any>> = listOf(
            arrayOf("", "", empty()),
            arrayOf("out 500", "500", empty()),
            arrayOf("out 0500", "500", empty()),
            arrayOf("out -500", "-500", empty()),
            arrayOf("out --500", "500", empty()),
            arrayOf("out +500", "500", empty()),
            arrayOf("out ++500", "500", empty()),
            arrayOf("out +-500", "-500", empty()),
            arrayOf("out -+500", "-500", empty()),
            arrayOf("out 0.456", "0.456", empty()),
            arrayOf("out .5", "0.5", empty()),
            arrayOf("out 0.0", "0", empty()),
            arrayOf("out 123.456", "123.456", empty()),
            arrayOf("out -123.456", "-123.456", empty()),
            arrayOf("out +123.456", "123.456", empty()),
            arrayOf("out 1e-7", "1E-7", empty()),
            arrayOf("out 1e+6", "1E+6", empty()),
            arrayOf("out 1e6", "1E+6", empty()),
            arrayOf("out -1e-7", "-1E-7", empty()),
            arrayOf("out -1e+6", "-1E+6", empty()),
            arrayOf("out -1e6", "-1E+6", empty()),
            arrayOf("out 1.5e6", "1.5E+6", empty()),
            arrayOf("out 1.5e6.5", "1.5E+6", listOf("line 1:9: Unexpected top-level expression: '.5'.")),
            arrayOf("out 0.5e6", "5E+5", empty()),
            arrayOf("out .5e6", "5E+5", empty()),
            arrayOf("out 12e2", "1.2E+3", empty()),
            arrayOf("out -12e2", "-1.2E+3", empty()),
            arrayOf("out 123456e2", "1.23456E+7", empty()),
            arrayOf("out 1e-9", "1E-9", empty()),
            arrayOf("out 1e9", "1E+9", empty()),
            arrayOf("out .e6", "", listOf("line 1:4: Syntax error: extraneous input '.' expecting {'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}.", "line 1:5: Variable undeclared: 'e6'.")),
            arrayOf("out -.e6", "", listOf("line 1:5: Syntax error: extraneous input '.' expecting {'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}.", "line 1:6: Variable undeclared: 'e6'.")),
            arrayOf("out e1", "", listOf("line 1:4: Variable undeclared: 'e1'.")),
            arrayOf("out e", "", listOf("line 1:4: Variable undeclared: 'e'.")),
            arrayOf("out -e1", "", listOf("line 1:5: Variable undeclared: 'e1'.")),
            arrayOf("out -e", "", listOf("line 1:5: Variable undeclared: 'e'.")),
            arrayOf("out 500;", "500", listOf("line 1:7: Syntax error: token recognition error at: ';'.")),
            arrayOf("out 1abc", "1", listOf("line 1:5: Unexpected top-level expression: 'abc'.")),
            arrayOf(
                "out 1) out 2",
                "12",
                listOf(
                    "line 1:5: Syntax error: extraneous input ')' expecting {<EOF>, 'print', 'out', 'var', 'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}."
                )
            ),
            arrayOf("print", "", listOf("line 1:5: Syntax error: missing STRING at '<EOF>'.")),
            arrayOf("print \"text\"", "text", empty()),
            arrayOf("print \"text\";", "text", listOf("line 1:12: Syntax error: token recognition error at: ';'.")),
            arrayOf(
                """
                    print "line 1\nline 2"
                """.trimIndent(),
                """
                    line 1
                    line 2
                """.trimIndent(),
                empty()
            ),
            arrayOf(
                "print text",
                "",
                listOf(
                    "line 1:6: Syntax error: missing STRING at 'text'.",
                    "line 1:6: Unexpected top-level expression: 'text'."
                )
            ),
            arrayOf("print \"'text'\"", "'text'", empty()),
            arrayOf("print \"text", "", listOf("line 1:6: Syntax error: mismatched input '\"text' expecting STRING.")),
            arrayOf(
                "print text\"",
                "",
                listOf(
                    "line 1:6: Syntax error: missing STRING at 'text'.",
                    "line 1:6: Unexpected top-level expression: 'text'.",
                    "line 1:10: Syntax error: extraneous input '\"' expecting {<EOF>, 'print', 'out', 'var', 'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}."
                )
            ),
            arrayOf("print \"te\\\"xt\"", "te\"xt", empty()),
            arrayOf("print \"te\\xt\"", "te\\xt", empty()),
            arrayOf("print \"te\txt\"", "te\txt", empty()),
            arrayOf(
                """
                    print "te\nxt"
                """.trimIndent(),
                """
                    te
                    xt
                """.trimIndent(),
                empty()
            ),
            arrayOf(
                """
                    print "te\rxt"
                """.trimIndent(),
                "text",
                empty()
            ),
            arrayOf(
                """
                    print "pi = "
                    out 3.1415926
                """.trimIndent(),
                "pi = 3.1415926",
                empty(),
            ),
            arrayOf(
                """
                    var n = 1.5
                    out n
                """.trimIndent(),
                "1.5",
                empty(),
            ),
            arrayOf("out n", "", listOf("line 1:4: Variable undeclared: 'n'.")),
            arrayOf(
                """
                    var n = 1
                    var n1 = n
                    print "n1="
                    out n1
                    print " n="
                    out n
                """.trimIndent(),
                "n1=1 n=1",
                empty(),
            ),
            arrayOf(
                """
                    var n = 1
                    var n = 2
                    out n
                """.trimIndent(),
                "1",
                listOf("line 2:0: Variable redeclaration: 'n'."),
            ),
            arrayOf(
                """
                    var var = 1
                    out var
                """.trimIndent(),
                "",
                listOf(
                    "line 1:0: Missing variable assignment: 'var'.",
                    "line 1:4: Syntax error: mismatched input 'var' expecting NAME.",
                    "line 1:8: Syntax error: missing NAME at '='.",
                    "line 2:4: Syntax error: mismatched input 'var' expecting {'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}.",
                    "line 2:4: Unsupported expression encountered: ''.",
                    "line 2:4: Missing variable assignment: 'var'.",
                    "line 2:7: Syntax error: mismatched input '<EOF>' expecting NAME.",
                ),
            ),
            arrayOf("out 2 + 3", "5", empty()),
            arrayOf("out 15 - 2.5", "12.5", empty()),
            arrayOf("out 15 / 5", "3", empty()),
            arrayOf("out 5 / 2", "2.5", empty()),
            arrayOf("out 3 * 5", "15", empty()),
            arrayOf("out 2 ^ 5", "32", empty()),
            arrayOf("out 2 + 3 * 4", "14", empty()),
            arrayOf("out 2 ++ 3 * 4", "14", empty()),
            arrayOf("out (2 + 3) * 4", "20", empty()),
            arrayOf("out (1 + 3 * 5) / 4", "4", empty()),
            arrayOf("out 2 ^ (5 + 1)", "64", empty()),
            arrayOf("out 2^(5-1)", "16", empty()),
            arrayOf("out 1 / 0", "Infinity", empty()),
            arrayOf("out 1 / 0.0", "Infinity", empty()),
            arrayOf("out 1.0 / 0", "Infinity", empty()),
            arrayOf("out 1.0 / 0.0", "Infinity", empty()),
            arrayOf("out -1/0", "-Infinity", empty()),
            arrayOf("out -1/0.0", "-Infinity", empty()),
            arrayOf("out -1.0/0", "-Infinity", empty()),
            arrayOf("out -1.0/0.0", "-Infinity", empty()),
            arrayOf("out 0/0", "NaN", empty()),
            arrayOf("out 0/0.0", "NaN", empty()),
            arrayOf("out 0.0/0", "NaN", empty()),
            arrayOf("out 0.0/0.0", "NaN", empty()),
            arrayOf("out 1e1 + 1e2", "1.1E+2", empty()),
            arrayOf("out 1 - 1e-1", "0.9", empty()),
            arrayOf("out 1 + 1e+1", "11", empty()),
            arrayOf("out (1 + 1e+1) * 1e-1", "1.1", empty()),
            arrayOf("out 1 + (1e+1 * 2) / 1e-1", "201", empty()),
            arrayOf("out 0^0", "1", empty()),
            arrayOf("out 0^1", "0", empty()),
            arrayOf("out 0^(-1)", "Infinity", empty()),
            arrayOf("out 0.0^0", "1", empty()),
            arrayOf("out 0.0^1", "0", empty()),
            arrayOf("out 0.0^(-1)", "Infinity", empty()),
            arrayOf("out 0.0^0.0", "1", empty()),
            arrayOf("out 0.0^1.0", "0", empty()),
            arrayOf("out 0.0^(-1.0)", "Infinity", empty()),
            arrayOf("out (-1)^(-1)", "-1", empty()),
            arrayOf("out (-1)^(-2)", "1", empty()),
            arrayOf("out (-1)^(-3)", "-1", empty()),
            arrayOf("out (-1)^0", "1", empty()),
            arrayOf("out (-1)^1", "-1", empty()),
            arrayOf("out (-1)^2", "1", empty()),
            arrayOf("out (-1)^3", "-1", empty()),
            arrayOf("out (-1.0)^(-1.0)", "-1", empty()),
            arrayOf("out (-1.0)^(-2.0)", "1", empty()),
            arrayOf("out (-1.0)^(-3.0)", "-1", empty()),
            arrayOf("out (-1.0)^0.0", "1", empty()),
            arrayOf("out (-1.0)^1.0", "-1", empty()),
            arrayOf("out (-1.0)^2.0", "1", empty()),
            arrayOf("out (-1.0)^3.0", "-1", empty()),
            arrayOf("out ((0 + 1) - 23 * 1 / 2 ) * -10 -5", "1E+2", empty()),
            arrayOf("out ((0 + 1) - 23 * 1 / 2 ) * (-10) - 5", "1E+2", empty()),
            arrayOf("out ((0 + 1) - 23 * 1 / 2 ) * (-10) - 55", "5E+1", empty()),
            arrayOf("out ((0 + 1) - 23 * 1 / 2 ) * (-10) - 50", "55", empty()),
            arrayOf(
                """
                    var num1 = 12
                    var num2 = 3
                    var num3 = 2 + num1 / (10 - num2 * 2)
                    print "result = "
                    out num3
                """.trimIndent(),
                "result = 5",
                empty()
            ),
            arrayOf(
                """
                    var i = 5
                    var res = 2^(i + 1)
                    out res
                """.trimIndent(),
                "64",
                empty()
            ),
            arrayOf(
                """
                    var i = 5
                    var res = 2^(i+1)
                    out res
                """.trimIndent(),
                "64",
                empty()
            ),
            arrayOf("out {1,5}", "{ 1, 2, 3, 4, 5 }", empty()),
            arrayOf("out {1,5-2}", "{ 1, 2, 3 }", empty()),
            arrayOf("out {1,6/2}", "{ 1, 2, 3 }", empty()),
            arrayOf("out {3/1,6/2}", "{ 3 }", empty()),
            arrayOf("out {-4/2,2}", "{ -2, -1, 0, 1, 2 }", empty()),
            arrayOf("out {2,5/2}", "", listOf("line 1:7: Sequence's end is expected to be an integer: '5/2'.")),
            arrayOf("out {1.5,2}", "", listOf("line 1:5: Sequence's begin is expected to be an integer: '1.5'.")),
            arrayOf("out {1,2.5}", "", listOf("line 1:7: Sequence's end is expected to be an integer: '2.5'.")),
            arrayOf("var seq = {0,200000000}", "", empty()),
            arrayOf("var seq = {0,200000001}", "", listOf("line 1:10: Sequence's length is exceeding the limits: 200000001 > 200000000.")),
            arrayOf(
                "out {1,5,7}",
                "{ 1, 2, 3, 4, 5 }",
                listOf(
                    "line 1:8: Syntax error: mismatched input ',' expecting {PLUSMINUS, MULDIV, '^', '}'}.",
                    "line 1:9: Unexpected top-level expression: '7'.",
                    "line 1:10: Syntax error: extraneous input '}' expecting {<EOF>, 'print', 'out', 'var', 'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}."
                )
            ),
            arrayOf("out {-2,3}", "{ -2, -1, 0, 1, 2, 3 }", empty()),
            arrayOf("out {5,1}", "", listOf("line 1:4: Sequence's upper bound is less than lower bound: 5 > 1.")),
            arrayOf(
                "out {1}",
                "",
                listOf(
                    "line 1:4: Missing sequence's upper bound: '{1}'.",
                    "line 1:6: Syntax error: mismatched input '}' expecting {PLUSMINUS, MULDIV, '^', ','}.",
                )
            ),
            arrayOf(
                "out {}",
                "",
                listOf(
                    "line 1:4: Missing sequence's upper bound: '{}'.",
                    "line 1:5: Syntax error: mismatched input '}' expecting {'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}.",
                )
            ),
            arrayOf(
                """
                    var from = 2
                    var to = 8
                    var seq = {from,to-3}
                    out seq
                """.trimIndent(),
                "{ 2, 3, 4, 5 }",
                empty()
            ),
            arrayOf("out map({1,5},i->i^2)", "{ 1, 4, 9, 16, 25 }", empty()),
            arrayOf("out map({1,5},i->{i,i+2})", "", listOf("line 1:17: Lambda in map() is expected to return a number: '{i,i+2}'.")),
            arrayOf("var i = 1 out map({1,5},i->i^2)", "", listOf("line 1:24: Variable redeclaration: 'i'.")),
            arrayOf("out reduce(map({1,5},i->i^2), 1, x y -> x*y)", "14400", empty()),
            arrayOf("out reduce({1,5},0,i j->i+j)", "15", empty()),
            arrayOf("out reduce({1,5},{1,2},i j->i+j)", "", listOf("line 1:17: Neutral element in reduce() should be a number expression: '{1,2}'.")),
            arrayOf("out reduce({1,5},1,i j->{i,j})", "", listOf("line 1:24: Lambda in reduce() is expected to return a number: '{i,j}'.")),
            arrayOf("var i = 1 out reduce({1,5},0,i j->i+j)", "", listOf("line 1:29: Variable redeclaration: 'i'.")),
            arrayOf("var j = 1 out reduce({1,5},0,i j->i+j)", "", listOf("line 1:31: Variable redeclaration: 'j'.")),
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
                "",
                listOf("line 5:17: Lambda in map() is expected to return a number: 'map({i,i+2},j->j)'.", "line 12:4: Variable undeclared: 'r'.")
            ),
            arrayOf(
                """
                    var n = 500
                    var seq = map({0, n}, i -> (-1)^i / (2 * i + 1))
                    var pi = 4 * reduce(seq, 0, x y -> x + y)
                    print "pi = "
                    out pi
                """.trimIndent(),
                "pi = ${calculatePi()}",
                empty()
            ),
            arrayOf(
                "out 4 * reduce(map({0, 500}, i -> (-1)^i / (2 * i + 1)), 0, x y -> x + y)",
                calculatePi(),
                empty()
            ),
            arrayOf(
                """
                    var n = map({0, 5}, i -> i^2)
                    out i
                """.trimIndent(),
                "",
                listOf("line 2:4: Variable undeclared: 'i'."),
            ),
            arrayOf(
                """
                    map({0, 5}, i -> i^2)
                    out 1
                """.trimIndent(),
                "1",
                listOf("line 1:0: Unexpected top-level expression: 'map({0,5},i->i^2)'."),
            ),
            arrayOf(
                """
                    var i = 5
                    var seq = 2^i / 2 * i + 1)
                    print "seq = "
                    out seq
                """.trimIndent(),
                "seq = 81",
                listOf(
                    "line 2:25: Syntax error: extraneous input ')' expecting {<EOF>, 'print', 'out', 'var', 'map', 'reduce', PLUSMINUS, '(', '{', NUMBER, NAME}."
                ),
            ),
            arrayOf(
                """
                    var n = 50000
                    var seq = map({0, n}, i -> (-1)^i / (2 * i + 1))
                    var pi = 4 * reduce(seq, 0, x y -> x + y)
                    print "pi="
                    out pi
                    
                    print "; "
                    
                    var x = pi/2
                    var n2 = 10
                    var seq2 = map({1, n2}, i -> (-1)^i * x^(2*i + 1) / reduce({1,2*i + 1}, 1, j k -> j*k))
                    var sinx = x + reduce(seq2, 0, j k -> j+k)
                    print "sin(x)="
                    out sinx
                    
                    print "; "
                    
                    var seq3 = map({1, n2}, i -> (-1)^i * x^(2*i) / reduce({1,2*i}, 1, j k -> j*k))
                    var cosx = 1 + reduce(seq3, 0, j k -> j+k)
                    print "cos(x)="
                    out cosx
                    
                    print "; sin^2(x)+cos^2(x)="
                    out sinx^2+cosx^2
                """.trimIndent(),
                "pi=3.14161265319; sin(x)=0.99999999995; cos(x)=-0.0000099998; sin^2(x)+cos^2(x)=1",
                empty(),
            ),
            arrayOf(
                """
                    print "Calculating pi, sin(pi/2), cos(pi/2), sin^2(pi/2)+cos^2(pi/2)...\n"
                    var n = 500
                    var n2 = 10
                    print "Iterations for pi: "
                    out n
                    print "\n"
                    print "Iterations for sin, cos: "
                    out n2
                    print "\n"
                    var seq = map({0, n}, i -> (-1)^i / (2 * i + 1))
                    var pi = 4 * reduce(seq, 0, x y -> x + y)
                    print "pi=\""
                    out pi

                    print "\";\n"

                    var x = pi/2
                    var seq2 = map({1, n2}, i -> (-1)^i * x^(2*i + 1) / reduce({1,2*i + 1}, 1, j k -> j*k))
                    var sinx = x + reduce(seq2, 0, j k -> j+k)
                    print "sin(x)="
                    out sinx

                    print ";\t" 

                    var seq3 = map({1, n2}, i -> (-1)^i * x^(2*i) / reduce({1,2*i}, 1, j k -> j*k))
                    var cosx = 1 + reduce(seq3, 0, j k -> j+k)
                    print "cos(x)="
                    out cosx

                    print ";\nsin^2(x)+cos^2(x)="
                    out sinx^2+cosx^2
                    print "\nDone!"
                """.trimIndent(),
                """
                    Calculating pi, sin(pi/2), cos(pi/2), sin^2(pi/2)+cos^2(pi/2)...
                    Iterations for pi: 500
                    Iterations for sin, cos: 10
                    pi="3.143588659586";
                    sin(x)=0.999999501995;	cos(x)=-0.000998002832;
                    sin^2(x)+cos^2(x)=1
                    Done!
                """.trimIndent(),
                empty(),
            )
        )

        /**
         * This one is used to verify the reference example.
         * @return pi value approximation
         */
        private fun calculatePi() =
            VarPrinter().print(
                (
                    4.0 * (0..500)
                        .map { i -> (if (i % 2 == 0) 1.0 else -1.0) / (2 * i + 1) }
                        .fold(0.0) { x, y -> x + y }
                    ).toVar()
            )
    }
}
