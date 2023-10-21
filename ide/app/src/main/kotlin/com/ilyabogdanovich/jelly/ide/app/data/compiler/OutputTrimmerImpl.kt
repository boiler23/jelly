package com.ilyabogdanovich.jelly.ide.app.data.compiler

/**
 * Implementation for [OutputTrimmer]
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
internal class OutputTrimmerImpl(private val maxLength: Int = MAX_OUTPUT_LENGTH) : OutputTrimmer {
    override fun trim(output: String): String {
        return if (output.length > maxLength) {
            output.substring(0, maxLength) + "\nOutput is too large to display."
        } else {
            output
        }
    }
}

// we don't show more than 5000 characters to not impact the app performance.
// this should become configurable in the future.
private const val MAX_OUTPUT_LENGTH = 5000
