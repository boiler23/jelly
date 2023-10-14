package com.ilyabogdanovich.jelly.logging

/**
 * Abstract factory, that produces instances of [LocalLogger].
 *
 * @author Ilya Bogdanovich on 13.10.2023
 */
interface LoggerFactory {
    /**
     * Returns new instance of [LocalLogger], associated with the given [tag].
     * @param tag tag, associated with this logger. Usually it is a class name.
     * @return new instance of [LocalLogger]
     */
    fun get(tag: String): LocalLogger
}

/**
 * Helper to automatically assign the given class name as a tag to the [LocalLogger].
 */
inline fun <reified T> LoggerFactory.get() = get(T::class.java.simpleName)
