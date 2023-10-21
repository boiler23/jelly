package com.ilyabogdanovich.jelly.ide.app.presentation.compiler

import io.kotest.matchers.shouldBe
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * Test for [CompilationStatus]
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
class CompilationStatusTest {
    @Test
    fun `empty status message`() {
        // Prepare

        // Do
        val result = CompilationStatus.Empty.message

        // Check
        result shouldBe ""
    }

    @Test
    fun `in progress status message`() {
        // Prepare

        // Do
        val result = CompilationStatus.InProgress.message

        // Check
        result shouldBe "Compiling..."
    }

    @Test
    fun `done status message`() {
        // Prepare

        // Do
        val result = CompilationStatus.Done(1500.milliseconds).message

        // Check
        result shouldBe "Last compile time: 1.5s"
    }

    @Test
    fun `exception status message - with description`() {
        // Prepare

        // Do
        val result = CompilationStatus.Exception(IllegalStateException("message")).message

        // Check
        result shouldBe "Compilation error: java.lang.IllegalStateException: message"
    }

    @Test
    fun `exception status message - without description`() {
        // Prepare

        // Do
        val result = CompilationStatus.Exception(IllegalStateException()).message

        // Check
        result shouldBe "Compilation error: java.lang.IllegalStateException: null"
    }
}
