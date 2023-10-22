package com.ilyabogdanovich.jelly.ide.app.data.documents

import com.ilyabogdanovich.jelly.ide.app.data.documents.DocumentRepositoryImpl.Companion.INTERNAL_SOURCE
import com.ilyabogdanovich.jelly.ide.app.domain.documents.Document
import com.ilyabogdanovich.jelly.logging.EmptyLoggerFactory
import io.kotest.matchers.shouldBe
import okio.Path.Companion.toPath
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
        fileSystem.createDirectories(INTERNAL_DIR)

        // Do
        val result = repository.read()

        // Check
        result shouldBe Document(text = "")
    }

    @Test
    fun `read - directory exists, file exists`() {
        // Prepare
        fileSystem.createDirectories(INTERNAL_DIR)
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
        fileSystem.createDirectories(INTERNAL_DIR)

        // Do
        repository.write(Document(text = "test"))
        val result = repository.read()

        // Check
        result shouldBe Document(text = "test")
    }

    @Test
    fun `write - directory exists, file exists`() {
        // Prepare
        fileSystem.createDirectories(INTERNAL_DIR)
        fileSystem.write(INTERNAL_SOURCE) { writeUtf8("test") }

        // Do
        repository.write(Document(text = "test"))
        val result = repository.read()

        // Check
        result shouldBe Document(text = "test")
    }

    @Test
    fun `import existing - no internal`() {
        // Prepare
        fileSystem.createDirectories(EXTERNAL_DIR)
        fileSystem.write(EXTERNAL_SOURCE) { writeUtf8("test") }

        // Do
        val result = repository.import(EXTERNAL_SOURCE)

        // Check
        result shouldBe Document("test")
        fileSystem.read(INTERNAL_SOURCE) { readUtf8() } shouldBe "test"
    }

    @Test
    fun `import existing - has internal`() {
        // Prepare
        fileSystem.createDirectories(INTERNAL_DIR)
        fileSystem.write(INTERNAL_SOURCE) { writeUtf8("internal") }
        fileSystem.createDirectories(EXTERNAL_DIR)
        fileSystem.write(EXTERNAL_SOURCE) { writeUtf8("test") }

        // Do
        val result = repository.import(EXTERNAL_SOURCE)

        // Check
        result shouldBe Document("test")
        fileSystem.read(INTERNAL_SOURCE) { readUtf8() } shouldBe "test"
    }

    @Test
    fun `import non-existing - no internal`() {
        // Prepare
        fileSystem.createDirectories(INTERNAL_DIR)
        fileSystem.createDirectories(EXTERNAL_DIR)

        // Do
        val result = repository.import(EXTERNAL_SOURCE)

        // Check
        result shouldBe Document.empty()
        fileSystem.exists(INTERNAL_SOURCE) shouldBe false
    }

    @Test
    fun `import non-existing - has internal`() {
        // Prepare
        fileSystem.createDirectories(INTERNAL_DIR)
        fileSystem.write(INTERNAL_SOURCE) { writeUtf8("test") }

        // Do
        val result = repository.import(EXTERNAL_SOURCE)

        // Check
        result shouldBe Document.empty()
        fileSystem.read(INTERNAL_SOURCE) { readUtf8() } shouldBe "test"
    }

    @Test
    fun `export existing`() {
        // Prepare
        fileSystem.createDirectories(INTERNAL_DIR)
        fileSystem.write(INTERNAL_SOURCE) { writeUtf8("test") }

        // Do
        val result = repository.export(EXTERNAL_SOURCE)

        // Check
        result shouldBe Document("test")
        fileSystem.read(EXTERNAL_SOURCE) { readUtf8() } shouldBe "test"
    }

    @Test
    fun `export non-existing`() {
        // Prepare

        // Do
        val result = repository.export(EXTERNAL_SOURCE)

        // Check
        result shouldBe null
        fileSystem.exists(EXTERNAL_SOURCE) shouldBe false
    }

    @Test
    fun `read external - non-existing`() {
        // Prepare
        fileSystem.createDirectories(INTERNAL_DIR)
        fileSystem.write(INTERNAL_SOURCE) { writeUtf8("test") }

        // Do
        val result = repository.readExternal(EXTERNAL_SOURCE)

        // Check
        result shouldBe Document.empty()
        fileSystem.read(INTERNAL_SOURCE) { readUtf8() } shouldBe "test"
    }

    @Test
    fun `read external - existing`() {
        // Prepare
        fileSystem.createDirectories(INTERNAL_DIR)
        fileSystem.write(INTERNAL_SOURCE) { writeUtf8("test") }
        fileSystem.createDirectories(EXTERNAL_DIR)
        fileSystem.write(EXTERNAL_SOURCE) { writeUtf8("external") }

        // Do
        val result = repository.readExternal(EXTERNAL_SOURCE)

        // Check
        result shouldBe Document("external")
        fileSystem.read(INTERNAL_SOURCE) { readUtf8() } shouldBe "test"
    }
}

private val EXTERNAL_DIR = "external".toPath()
private val EXTERNAL_SOURCE = EXTERNAL_DIR / "external.jy"
