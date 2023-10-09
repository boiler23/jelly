package com.ilyabogdanovich.jelly.utils

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [Either]
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class EitherTest {
    @Test
    fun `as left`() {
        // Prepare

        // Do
        val result = 123.asLeft<Int, String>()

        // Check
        result shouldBe Either.Left(123)
    }

    @Test
    fun `as right`() {
        // Prepare

        // Do
        val result = "test".asRight<Int, String>()

        // Check
        result shouldBe Either.Right("test")
    }
}
