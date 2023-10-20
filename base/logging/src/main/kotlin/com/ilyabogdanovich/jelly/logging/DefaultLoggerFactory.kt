package com.ilyabogdanovich.jelly.logging

/**
 * Default logger factory implementation.
 * Produces [PrintLocalLogger] instances.
 *
 * @author Ilya Bogdanovich on 13.10.2023
 */
object DefaultLoggerFactory : LoggerFactory {
    override fun get(tag: String): LocalLogger =
        if (ENABLE_LOGGING) PrintLocalLogger(tag) else EmptyLocalLogger
}
