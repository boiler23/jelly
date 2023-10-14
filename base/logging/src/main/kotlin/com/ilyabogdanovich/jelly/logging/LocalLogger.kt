package com.ilyabogdanovich.jelly.logging

/**
 * Abstract logger interface, local to some context (usually a class).
 *
 * @author Ilya Bogdanovich on 13.10.2023
 */
interface LocalLogger {
    /**
     * Logs a message with verbose level. Has lowest priority, shouldn't affect production builds.
     * @param th optional throwable, associated with the message.
     * @param message lambda to generate a message text.
     */
    fun v(th: Throwable? = null, message: () -> String) = Unit

    /**
     * Logs a message with debug level. Useful for debugging, shouldn't affect production builds.
     * @param th optional throwable, associated with the message.
     * @param message lambda to generate a message text.
     */
    fun d(th: Throwable? = null, message: () -> String) = Unit

    /**
     * Logs info messages. Remains in production.
     * @param th optional throwable, associated with the message.
     * @param message lambda to generate a message text.
     */
    fun i(th: Throwable? = null, message: () -> String) = Unit

    /**
     * Logs a warning message, when a severe error was avoided. Remains in production.
     * @param th optional throwable, associated with the message.
     * @param message lambda to generate a message text.
     */
    fun w(th: Throwable? = null, message: () -> String) = Unit

    /**
     * Logs an error, however non-fatal so the app is still operating.
     * Remains in production, should also register the error in the analytics system.
     * @param th optional throwable, associated with the message.
     * @param message lambda to generate a message text.
     */
    fun e(th: Throwable? = null, message: () -> String) = Unit
}
