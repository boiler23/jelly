package com.ilyabogdanovich.jelly.logging

/**
 * Empty logger factory implementation.
 * Produces [EmptyLocalLogger] instances.
 *
 * @author Ilya Bogdanovich on 13.10.2023
 */
object EmptyLoggerFactory : LoggerFactory {
    override fun get(tag: String): LocalLogger = EmptyLocalLogger
}
