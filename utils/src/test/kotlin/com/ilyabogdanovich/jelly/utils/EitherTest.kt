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

    @Test
    fun `map right - right`() {
        // Prepare
        val v = Either.Right<String, Int>(1)

        // Do
        val result = v.mapRight { it + 1 }

        // Check
        result shouldBe Either.Right(2)
    }

    @Test
    fun `map right - left`() {
        // Prepare
        val v = Either.Left<String, Int>("1")

        // Do
        val result = v.mapRight { it + 1 }

        // Check
        result shouldBe Either.Left("1")
    }

    @Test
    fun `map either right - right to right`() {
        // Prepare
        val v = Either.Right<String, Int>(1)

        // Do
        val result = v.mapEitherRight { Either.Right(it + 1) }

        // Check
        result shouldBe Either.Right(2)
    }

    @Test
    fun `map either right - right to left`() {
        // Prepare
        val v = Either.Right<String, Int>(1)

        // Do
        val result = v.mapEitherRight { Either.Left<String, Int>("error") }

        // Check
        result shouldBe Either.Left("error")
    }

    @Test
    fun `map either right - left to right`() {
        // Prepare
        val v = Either.Left<String, Int>("1")

        // Do
        val result = v.mapEitherRight { Either.Right<String, Int>(it + 1) }

        // Check
        result shouldBe Either.Left("1")
    }

    @Test
    fun `map either right - left to left`() {
        // Prepare
        val v = Either.Left<String, Int>("1")

        // Do
        val result = v.mapEitherRight { Either.Left<String, Int>("error") }

        // Check
        result shouldBe Either.Left("1")
    }
}
