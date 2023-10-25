package com.ilyabogdanovich.jelly.ide.app.domain.documents

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Test for [Document]
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
class DocumentTest {
    @Test
    fun `empty document`() {
        // Prepare

        // Do
        val result = Document.empty()

        // Check
        result shouldBe Document(text = "")
    }
}
