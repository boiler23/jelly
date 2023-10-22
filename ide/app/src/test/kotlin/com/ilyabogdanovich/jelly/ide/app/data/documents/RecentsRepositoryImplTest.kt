package com.ilyabogdanovich.jelly.ide.app.data.documents

import com.ilyabogdanovich.jelly.ide.app.data.documents.RecentsRepositoryImpl.Companion.RECENTS
import com.ilyabogdanovich.jelly.logging.EmptyLoggerFactory
import io.kotest.matchers.shouldBe
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.Test

/**
 * Test for [RecentsRepositoryImpl]
 *
 * @author Ilya Bogdanovich on 22.10.2023
 */
class RecentsRepositoryImplTest {
    private val fileSystem = FakeFileSystem()
    private val repository = RecentsRepositoryImpl(fileSystem, EmptyLoggerFactory)

    @Test
    fun `read existing recents`() {
        // Prepare
        fileSystem.createDirectories(INTERNAL_DIR)
        fileSystem.write(RECENTS) { writeUtf8("path/to/recents") }

        // Do
        val result = repository.read()

        // Check
        result shouldBe "path/to/recents".toPath()
    }

    @Test
    fun `read non-existing recents`() {
        // Prepare

        // Do
        val result = repository.read()

        // Check
        result shouldBe null
    }

    @Test
    fun `write path - no existing recents`() {
        // Prepare

        // Do
        repository.write("path/to/recents".toPath())
        val result = repository.read()

        // Check
        result shouldBe "path/to/recents".toPath()
    }

    @Test
    fun `write path - existing recents`() {
        // Prepare
        fileSystem.createDirectories(INTERNAL_DIR)
        fileSystem.write(RECENTS) { writeUtf8("path/to/recents") }

        // Do
        repository.write("new/path/to/recents".toPath())
        val result = repository.read()

        // Check
        result shouldBe "new/path/to/recents".toPath()
    }

    @Test
    fun `delete path - no existing recents`() {
        // Prepare

        // Do
        repository.write(null)
        val result = repository.read()

        // Check
        result shouldBe null
        fileSystem.exists(RECENTS) shouldBe false
    }

    @Test
    fun `delete path - existing recents`() {
        // Prepare
        fileSystem.createDirectories(INTERNAL_DIR)
        fileSystem.write(RECENTS) { writeUtf8("path/to/recents") }

        // Do
        repository.write(null)
        val result = repository.read()

        // Check
        result shouldBe null
        fileSystem.exists(RECENTS) shouldBe false
    }
}
