package com.ilyabogdanovich.jelly.logging

import java.io.PrintStream

/**
 * [LocalLogger] implementation, that just prints messages to console.
 *
 * @author Ilya Bogdanovich on 13.10.2023
 */
@JvmInline
value class PrintLocalLogger(private val tag: String) : LocalLogger {
    private fun log(level: Char, th: Throwable?, message: () -> String, out: PrintStream = System.out) {
        out.println("$level/$tag: ${message()}")
        th?.printStackTrace()
    }

    override fun v(th: Throwable?, message: () -> String) = log('V', th, message)

    override fun d(th: Throwable?, message: () -> String) = log('D', th, message)

    override fun i(th: Throwable?, message: () -> String) = log('I', th, message)

    override fun w(th: Throwable?, message: () -> String) = log('W', th, message)

    override fun e(th: Throwable?, message: () -> String) = log('E', th, message, out = System.err)
}
