package com.ilyabogdanovich.jelly.ide.app.data.documents

import com.ilyabogdanovich.jelly.ide.app.data.documents.DocumentRepositoryImpl.Companion.INTERNAL_DIR
import com.ilyabogdanovich.jelly.ide.app.data.documents.DocumentRepositoryImpl.Companion.INTERNAL_SOURCE
import com.ilyabogdanovich.jelly.ide.app.domain.documents.Document
import com.ilyabogdanovich.jelly.logging.EmptyLoggerFactory
import io.kotest.matchers.shouldBe
import okio.fakefilesystem.FakeFileSystem
import org.junit.Test

/**
 * Test for [DocumentRepositoryImpl]
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
class DocumentRepositoryImplTest {
    private val fileSystem = FakeFileSystem()
    private val repository = DocumentRepositoryImpl(fileSystem, EmptyLoggerFactory)

    @Test
    fun `read - no directory`() {
        // Prepare

        // Do
        val result = repository.read()

        // Check
        result shouldBe Document(text = "")
    }

    @Test
    fun `read - directory exists, no file`() {
        // Prepare
        fileSystem.createDirectory(INTERNAL_DIR)

        // Do
        val result = repository.read()

        // Check
        result shouldBe Document(text = "")
    }

    @Test
    fun `read - directory exists, file exists`() {
        // Prepare
        fileSystem.createDirectory(INTERNAL_DIR)
        fileSystem.write(INTERNAL_SOURCE) {
            writeUtf8("test")
        }

        // Do
        val result = repository.read()

        // Check
        result shouldBe Document(text = "test")
    }

    @Test
    fun `write - no directory`() {
        // Prepare

        // Do
        repository.write(Document(text = "test"))
        val result = repository.read()

        // Check
        result shouldBe Document(text = "test")
    }

    @Test
    fun `write - directory exists, no file`() {
        // Prepare
        fileSystem.createDirectory(INTERNAL_DIR)

        // Do
        repository.write(Document(text = "test"))
        val result = repository.read()

        // Check
        result shouldBe Document(text = "test")
    }

    @Test
    fun `write - directory exists, file exists`() {
        // Prepare
        fileSystem.createDirectory(INTERNAL_DIR)
        fileSystem.write(INTERNAL_SOURCE) { writeUtf8("test") }

        // Do
        repository.write(Document(text = "test"))
        val result = repository.read()

        // Check
        result shouldBe Document(text = "test")
    }
}
