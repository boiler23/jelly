package com.ilyabogdanovich.jelly.ide.app.domain

/**
 * Deep link, used for the internal app navigation.
 *
 * @author Ilya Bogdanovich on 16.10.2023
 */
sealed class DeepLink {
    /**
     * This deep link type is used to navigate to a certain position in the source code.
     * @property position position in the source text.
     */
    data class Cursor(val position: Int) : DeepLink() {
        override fun buildString(): String =
            "jelly://cursor?pos=$position"
    }

    /**
     * Builds a string representation for the given deep link.
     * This representation can be used to serialize the deep-link object.
     * @return deep link string.
     */
    abstract fun buildString(): String

    companion object {
        /**
         * Parses the given [deepLink] string into a [DeepLink] object.
         * @return parsed [DeepLink] or null, if parsing failed.
         */
        fun parseString(deepLink: String): DeepLink? {
            val regex = Regex("^jelly://cursor[?]pos=(\\d+)$")
            val groups = regex.matchEntire(deepLink)?.groups ?: return null
            return if (groups.size == 2) {
                val position = groups[1]?.value?.toIntOrNull() ?: return null
                Cursor(position = position)
            } else {
                null
            }
        }
    }
}
