package com.ilyabogdanovich.jelly.ide.app.domain

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [DeepLink]
 *
 * @author Ilya Bogdanovich on 16.10.2023
 */
class DeepLinkTest {
    @Test
    fun `build cursor deep link`() {
        // Prepare
        val cursor = DeepLink.Cursor(position = 10)

        // Do
        val result = cursor.buildString()

        // Check
        result shouldBe "jelly://cursor?pos=10"
    }

    @Test
    fun `parse cursor deep link`() {
        // Prepare

        // Do
        val result = DeepLink.parseString("jelly://cursor?pos=10")

        // Check
        result shouldBe DeepLink.Cursor(position = 10)
    }

    @Test
    fun `parse cursor deep link - no pos`() {
        // Prepare

        // Do
        val result = DeepLink.parseString("jelly://cursor?")

        // Check
        result shouldBe null
    }
}
