package com.ilyabogdanovich.jelly.ide.app.domain.documents

import com.ilyabogdanovich.jelly.logging.EmptyLoggerFactory
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.Path
import org.junit.Test

/**
 * Test for [DocumentContentTrackerImpl]
 *
 * @author Ilya Bogdanovich on 22.10.2023
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DocumentContentTrackerImplTest {
    private val documentRepository = mockk<DocumentRepository>()
    private val recentsRepository = mockk<RecentsRepository>()
    private val dispatcher = UnconfinedTestDispatcher()
    private val scope = TestScope(dispatcher)

    private val tracker = DocumentContentTrackerImpl(documentRepository, recentsRepository, EmptyLoggerFactory)

    private fun <T> Flow<T>.collectToList(scope: TestScope): List<T> {
        val emissions = mutableListOf<T>()
        scope.launch { collect { emissions.add(it) } }
        return emissions
    }

    @Test
    fun init() = runTest(dispatcher) {
        // Prepare
        val contentChanges = tracker.internalContentChanges.collectToList(scope)

        // Do
        advanceUntilIdle()

        // Check
        tracker.dirtyState.value shouldBe false
        tracker.externalPath.value shouldBe null
        tracker.externalContentFlow.value shouldBe Document.empty()
        contentChanges shouldBe listOf()
    }

    @Test
    fun open() = runTest(dispatcher) {
        // Prepare
        val externalPath = mockk<Path>()
        every { documentRepository.import(externalPath) } returns Document("test")
        every { recentsRepository.write(externalPath) } returns Unit
        val contentChanges = tracker.internalContentChanges.collectToList(scope)
        tracker.dirtyState.value = true

        // Do
        tracker.open(externalPath)
        advanceUntilIdle()

        // Check
        verifySequence {
            documentRepository.import(externalPath)
            recentsRepository.write(externalPath)
        }
        tracker.dirtyState.value shouldBe false
        tracker.externalPath.value shouldBe externalPath
        tracker.externalContentFlow.value shouldBe Document("test")
        contentChanges shouldBe listOf(Document("test"))
    }

    @Test
    fun `save - exported`() = runTest(dispatcher) {
        // Prepare
        val externalPath = mockk<Path>()
        every { documentRepository.export(externalPath) } returns Document("test")
        every { recentsRepository.write(externalPath) } returns Unit
        tracker.dirtyState.value = true
        val contentChanges = tracker.internalContentChanges.collectToList(scope)

        // Do
        val result = tracker.save(externalPath)
        advanceUntilIdle()

        // Check
        verifySequence {
            documentRepository.export(externalPath)
            recentsRepository.write(externalPath)
        }
        tracker.dirtyState.value shouldBe false
        tracker.externalPath.value shouldBe externalPath
        tracker.externalContentFlow.value shouldBe Document("test")
        contentChanges shouldBe listOf()
        result shouldBe true
    }

    @Test
    fun `save - not exported`() = runTest(dispatcher) {
        // Prepare
        val externalPath = mockk<Path>()
        every { documentRepository.export(externalPath) } returns null
        tracker.dirtyState.value = true
        val contentChanges = tracker.internalContentChanges.collectToList(scope)

        // Do
        val result = tracker.save(externalPath)
        advanceUntilIdle()

        // Check
        verifySequence {
            documentRepository.export(externalPath)
        }
        confirmVerified(recentsRepository)
        tracker.dirtyState.value shouldBe true
        tracker.externalPath.value shouldBe null
        tracker.externalContentFlow.value shouldBe Document.empty()
        contentChanges shouldBe listOf()
        result shouldBe false
    }

    @Test
    fun new() = runTest(dispatcher) {
        // Prepare
        tracker.dirtyState.value = true
        tracker.externalPath.value = mockk()
        tracker.externalContentFlow.value = Document("test")
        every { recentsRepository.write(null) } returns Unit
        every { documentRepository.write(Document.empty()) } returns Unit
        val contentChanges = tracker.internalContentChanges.collectToList(scope)

        // Do
        tracker.new()
        advanceUntilIdle()

        // Check
        verifySequence {
            recentsRepository.write(null)
            documentRepository.write(Document.empty())
        }
        tracker.dirtyState.value shouldBe false
        tracker.externalPath.value shouldBe null
        tracker.externalContentFlow.value shouldBe Document.empty()
        contentChanges shouldBe listOf(Document.empty())
    }

    @Test
    fun `startup - has recent path - has local content - has external content - same`() = runTest(dispatcher) {
        // Prepare
        val externalPath = mockk<Path>()
        every { recentsRepository.read() } returns externalPath
        every { documentRepository.read() } returns Document("test")
        every { documentRepository.readExternal(externalPath) } returns Document("test")
        val contentChanges = tracker.internalContentChanges.collectToList(scope)

        // Do
        tracker.startup()
        advanceUntilIdle()

        // Check
        verifySequence {
            recentsRepository.read()
            documentRepository.read()
            documentRepository.readExternal(externalPath)
        }
        tracker.externalPath.value shouldBe externalPath
        tracker.externalContentFlow.value shouldBe Document("test")
        tracker.dirtyState.value shouldBe false
        contentChanges shouldBe listOf(Document("test"))
    }

    @Test
    fun `startup - has recent path - has local content - has external content - not same`() = runTest(dispatcher) {
        // Prepare
        val externalPath = mockk<Path>()
        every { recentsRepository.read() } returns externalPath
        every { documentRepository.read() } returns Document("test")
        every { documentRepository.readExternal(externalPath) } returns Document("external")
        val contentChanges = tracker.internalContentChanges.collectToList(scope)

        // Do
        tracker.startup()
        advanceUntilIdle()

        // Check
        verifySequence {
            recentsRepository.read()
            documentRepository.read()
            documentRepository.readExternal(externalPath)
        }
        tracker.externalPath.value shouldBe externalPath
        tracker.externalContentFlow.value shouldBe Document("external")
        tracker.dirtyState.value shouldBe true
        contentChanges shouldBe listOf(Document("test"))
    }

    @Test
    fun `startup - has recent path - has local content - no external content`() = runTest(dispatcher) {
        // Prepare
        val externalPath = mockk<Path>()
        every { recentsRepository.read() } returns externalPath
        every { documentRepository.read() } returns Document("test")
        every { documentRepository.readExternal(externalPath) } returns Document.empty()
        val contentChanges = tracker.internalContentChanges.collectToList(scope)

        // Do
        tracker.startup()
        advanceUntilIdle()

        // Check
        verifySequence {
            recentsRepository.read()
            documentRepository.read()
            documentRepository.readExternal(externalPath)
        }
        tracker.externalPath.value shouldBe externalPath
        tracker.externalContentFlow.value shouldBe Document.empty()
        tracker.dirtyState.value shouldBe true
        contentChanges shouldBe listOf(Document("test"))
    }

    @Test
    fun `startup - has recent path - no local content - has external content`() = runTest(dispatcher) {
        // Prepare
        val externalPath = mockk<Path>()
        every { recentsRepository.read() } returns externalPath
        every { documentRepository.read() } returns Document.empty()
        every { documentRepository.readExternal(externalPath) } returns Document("external")
        val contentChanges = tracker.internalContentChanges.collectToList(scope)

        // Do
        tracker.startup()
        advanceUntilIdle()

        // Check
        verifySequence {
            recentsRepository.read()
            documentRepository.read()
            documentRepository.readExternal(externalPath)
        }
        tracker.externalPath.value shouldBe externalPath
        tracker.externalContentFlow.value shouldBe Document("external")
        tracker.dirtyState.value shouldBe true
        contentChanges shouldBe listOf(Document.empty())
    }

    @Test
    fun `startup - has recent path - no local content - no external content`() = runTest(dispatcher) {
        // Prepare
        val externalPath = mockk<Path>()
        every { recentsRepository.read() } returns externalPath
        every { documentRepository.read() } returns Document.empty()
        every { documentRepository.readExternal(externalPath) } returns Document.empty()
        val contentChanges = tracker.internalContentChanges.collectToList(scope)

        // Do
        tracker.startup()
        advanceUntilIdle()

        // Check
        verifySequence {
            recentsRepository.read()
            documentRepository.read()
            documentRepository.readExternal(externalPath)
        }
        tracker.externalPath.value shouldBe externalPath
        tracker.externalContentFlow.value shouldBe Document.empty()
        tracker.dirtyState.value shouldBe false
        contentChanges shouldBe listOf(Document.empty())
    }

    @Test
    fun `startup - no recent path - has local content`() = runTest(dispatcher) {
        // Prepare
        every { recentsRepository.read() } returns null
        every { documentRepository.read() } returns Document("test")
        val contentChanges = tracker.internalContentChanges.collectToList(scope)

        // Do
        tracker.startup()
        advanceUntilIdle()

        // Check
        verifySequence {
            recentsRepository.read()
            documentRepository.read()
        }
        tracker.externalPath.value shouldBe null
        tracker.externalContentFlow.value shouldBe Document.empty()
        tracker.dirtyState.value shouldBe true
        contentChanges shouldBe listOf(Document("test"))
    }

    @Test
    fun `startup - no recent path - no local content`() = runTest(dispatcher) {
        // Prepare
        every { recentsRepository.read() } returns null
        every { documentRepository.read() } returns Document.empty()
        val contentChanges = tracker.internalContentChanges.collectToList(scope)

        // Do
        tracker.startup()
        advanceUntilIdle()

        // Check
        verifySequence {
            recentsRepository.read()
            documentRepository.read()
        }
        tracker.externalPath.value shouldBe null
        tracker.externalContentFlow.value shouldBe Document.empty()
        tracker.dirtyState.value shouldBe false
        contentChanges shouldBe listOf(Document.empty())
    }

    @Test
    fun `handle content changes - external equals internal`() {
        // Prepare
        every { documentRepository.write(Document("test")) } returns Unit
        tracker.externalContentFlow.value = Document("test")

        // Do
        tracker.handleContentChanges(Document("test"))

        // Check
        verifySequence { documentRepository.write(Document("test")) }
        tracker.dirtyState.value shouldBe false
    }

    @Test
    fun `handle content changes - external not equals internal`() {
        // Prepare
        every { documentRepository.write(Document("test")) } returns Unit
        tracker.externalContentFlow.value = Document("external")

        // Do
        tracker.handleContentChanges(Document("test"))

        // Check
        verifySequence { documentRepository.write(Document("test")) }
        tracker.dirtyState.value shouldBe true
    }
}
