package com.ilyabogdanovich.jelly.ide.app.data.compiler

/**
 * Helper, that trims output string to some reasonable size, so it won't impact performance.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
internal interface OutputTrimmer {
    /**
     * Trims the given compilation [output].
     * @param output compilation outout to trim.
     * @return trimmed string, ready to be displayed.
     */
    fun trim(output: String): String
}
