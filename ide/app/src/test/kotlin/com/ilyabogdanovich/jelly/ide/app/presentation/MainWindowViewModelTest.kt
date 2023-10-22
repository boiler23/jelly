package com.ilyabogdanovich.jelly.ide.app.presentation

import com.ilyabogdanovich.jelly.ide.app.domain.documents.DocumentContentTracker
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.ConfirmDialogResult
import com.ilyabogdanovich.jelly.logging.EmptyLoggerFactory
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import io.mockk.verifySequence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.Path
import okio.Path.Companion.toPath
import org.junit.Test

/**
 * Test for [MainWindowViewModel]
 *
 * @author Ilya Bogdanovich on 22.10.2023
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainWindowViewModelTest {
    private val documentContentTracker = mockk<DocumentContentTracker>()
    private val openDialogState = mockk<MainWindowViewModel.DialogState<Path?>>()
    private val saveDialogState = mockk<MainWindowViewModel.DialogState<Path?>>()
    private val closeDialogState = mockk<MainWindowViewModel.DialogState<ConfirmDialogResult>>()
    private val failedOpenDialogState = mockk<MainWindowViewModel.DialogState<Unit>>()
    private val dispatcher = UnconfinedTestDispatcher()
    private val scope = TestScope(dispatcher)

    private val viewModel = MainWindowViewModel(
        documentContentTracker,
        dispatcher,
        EmptyLoggerFactory,
        openDialogState,
        saveDialogState,
        closeDialogState,
        failedOpenDialogState,
    )

    @Test
    fun `create new - current saved`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(false)
        every { documentContentTracker.new() } returns Unit

        // Do
        viewModel.new()

        // Check
        verifySequence {
            documentContentTracker.dirtyState
            documentContentTracker.new()
        }
    }

    @Test
    fun `create new - current dirty - cancel`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.Cancel

        // Do
        viewModel.new()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
        }
    }

    @Test
    fun `create new - current dirty - overwrite`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.No
        every { documentContentTracker.new() } returns Unit

        // Do
        viewModel.new()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
            documentContentTracker.new()
        }
    }

    @Test
    fun `create new - current dirty - save - has path`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.Yes
        val path = mockk<Path>()
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(path)
        every { documentContentTracker.save(path) } returns true
        every { documentContentTracker.new() } returns Unit

        // Do
        viewModel.new()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
            documentContentTracker.externalPath
            documentContentTracker.save(path)
            documentContentTracker.new()
        }
    }

    @Test
    fun `create new - current dirty - save - has no path - pick new correct`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.Yes
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(null)
        val path = "external/folder/file.jy".toPath()
        coEvery { saveDialogState.awaitResult() } returns path
        every { documentContentTracker.save(path) } returns true
        every { documentContentTracker.new() } returns Unit

        // Do
        viewModel.new()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
            documentContentTracker.externalPath
            saveDialogState.awaitResult()
            documentContentTracker.save(path)
            documentContentTracker.new()
        }
    }

    @Test
    fun `create new - current dirty - save - has no path - pick new incorrect`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.Yes
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(null)
        val path = "external/folder/file.txt".toPath()
        coEvery { saveDialogState.awaitResult() } returns path
        val fixedSavePath = "external/folder/file.txt.jy".toPath()
        every { documentContentTracker.save(fixedSavePath) } returns true
        every { documentContentTracker.new() } returns Unit

        // Do
        viewModel.new()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
            documentContentTracker.externalPath
            saveDialogState.awaitResult()
            documentContentTracker.save(fixedSavePath)
            documentContentTracker.new()
        }
    }

    @Test
    fun `create new - current dirty - save - has no path - do not pick new`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.Yes
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(null)
        coEvery { saveDialogState.awaitResult() } returns null

        // Do
        viewModel.new()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
            documentContentTracker.externalPath
            saveDialogState.awaitResult()
        }
    }

    @Test
    fun `open - current saved`() = runTest(dispatcher) {
        // Prepare
        val path = "path/to/open.jy".toPath()
        every { documentContentTracker.dirtyState } returns MutableStateFlow(false)
        coEvery { openDialogState.awaitResult() } returns path
        every { documentContentTracker.open(path) } returns Unit

        // Do
        viewModel.open()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            openDialogState.awaitResult()
            documentContentTracker.open(path)
        }
    }

    @Test
    fun `open - current dirty - cancel`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.Cancel

        // Do
        viewModel.open()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
        }
    }

    @Test
    fun `open - current dirty - overwrite`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.No
        val path = "path/to/open.jy".toPath()
        coEvery { openDialogState.awaitResult() } returns path
        every { documentContentTracker.open(path) } returns Unit

        // Do
        viewModel.open()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
            openDialogState.awaitResult()
            documentContentTracker.open(path)
        }
    }

    @Test
    fun `open - current dirty - overwrite - not supported`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.No
        val path = "path/to/open.txt".toPath()
        coEvery { openDialogState.awaitResult() } returns path
        coEvery { failedOpenDialogState.awaitResult() } returns Unit

        // Do
        viewModel.open()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
            openDialogState.awaitResult()
            failedOpenDialogState.awaitResult()
        }
    }

    @Test
    fun `open - current dirty - save - has path`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.Yes
        val savePath = mockk<Path>()
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(savePath)
        every { documentContentTracker.save(savePath) } returns true
        val openPath = "path/to/open.jy".toPath()
        coEvery { openDialogState.awaitResult() } returns openPath
        every { documentContentTracker.open(openPath) } returns Unit

        // Do
        viewModel.open()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
            documentContentTracker.externalPath
            documentContentTracker.save(savePath)
            openDialogState.awaitResult()
            documentContentTracker.open(openPath)
        }
    }

    @Test
    fun `open - current dirty - save - has no path - pick new correct`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.Yes
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(null)
        val savePath = "external/folder/file.jy".toPath()
        coEvery { saveDialogState.awaitResult() } returns savePath
        every { documentContentTracker.save(savePath) } returns true
        val openPath = "path/to/open.jy".toPath()
        coEvery { openDialogState.awaitResult() } returns openPath
        every { documentContentTracker.open(openPath) } returns Unit

        // Do
        viewModel.open()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
            documentContentTracker.externalPath
            saveDialogState.awaitResult()
            documentContentTracker.save(savePath)
            openDialogState.awaitResult()
            documentContentTracker.open(openPath)
        }
    }

    @Test
    fun `open - current dirty - save - has no path - pick new incorrect`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.Yes
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(null)
        val savePath = "external/folder/file.txt".toPath()
        coEvery { saveDialogState.awaitResult() } returns savePath
        val fixedSavePath = "external/folder/file.txt.jy".toPath()
        every { documentContentTracker.save(fixedSavePath) } returns true
        val openPath = "path/to/open.jy".toPath()
        coEvery { openDialogState.awaitResult() } returns openPath
        every { documentContentTracker.open(openPath) } returns Unit

        // Do
        viewModel.open()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
            documentContentTracker.externalPath
            saveDialogState.awaitResult()
            documentContentTracker.save(fixedSavePath)
            openDialogState.awaitResult()
            documentContentTracker.open(openPath)
        }
    }

    @Test
    fun `open - current dirty - save - has no path - do not pick new`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        coEvery { closeDialogState.awaitResult() } returns ConfirmDialogResult.Yes
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(null)
        coEvery { saveDialogState.awaitResult() } returns null

        // Do
        viewModel.open()

        // Check
        coVerifySequence {
            documentContentTracker.dirtyState
            closeDialogState.awaitResult()
            documentContentTracker.externalPath
            saveDialogState.awaitResult()
        }
    }

    @Test
    fun `save - has path`() = runTest(dispatcher) {
        // Prepare
        val savePath = mockk<Path>()
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(savePath)
        every { documentContentTracker.save(savePath) } returns true

        // Do
        viewModel.save()

        // Check
        coVerifySequence {
            documentContentTracker.externalPath
            documentContentTracker.save(savePath)
        }
    }

    @Test
    fun `save - has no path - pick new correct`() = runTest(dispatcher) {
        // Prepare
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(null)
        val savePath = "external/folder/file.jy".toPath()
        coEvery { saveDialogState.awaitResult() } returns savePath
        every { documentContentTracker.save(savePath) } returns true

        // Do
        viewModel.save()

        // Check
        coVerifySequence {
            documentContentTracker.externalPath
            saveDialogState.awaitResult()
            documentContentTracker.save(savePath)
        }
    }

    @Test
    fun `save - has no path - pick new incorrect`() = runTest(dispatcher) {
        // Prepare
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(null)
        val savePath = "external/folder/file.txt".toPath()
        coEvery { saveDialogState.awaitResult() } returns savePath
        val fixedSavePath = "external/folder/file.txt.jy".toPath()
        every { documentContentTracker.save(fixedSavePath) } returns true

        // Do
        viewModel.save()

        // Check
        coVerifySequence {
            documentContentTracker.externalPath
            saveDialogState.awaitResult()
            documentContentTracker.save(fixedSavePath)
        }
    }

    @Test
    fun `save - has no path - do not pick new`() = runTest(dispatcher) {
        // Prepare
        coEvery { documentContentTracker.externalPath } returns MutableStateFlow(null)
        coEvery { saveDialogState.awaitResult() } returns null

        // Do
        viewModel.save()

        // Check
        coVerifySequence {
            documentContentTracker.externalPath
            saveDialogState.awaitResult()
        }
    }

    @Test
    fun `start app`() {
        // Prepare
        every { documentContentTracker.startup() } returns Unit

        // Do
        viewModel.startApp()

        // Check
        verifyAll { documentContentTracker.startup() }
    }

    @Test
    fun `window title changes - dirty without path`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        every { documentContentTracker.externalPath } returns MutableStateFlow(null)

        // Do
        scope.launch { viewModel.processWindowTitleChanges() }
        scope.advanceUntilIdle()

        // Check
        viewModel.windowTitle shouldBe "* Untitled"
    }

    @Test
    fun `window title changes - dirty with path`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(true)
        every { documentContentTracker.externalPath } returns MutableStateFlow("path/to/file.jy".toPath())

        // Do
        scope.launch { viewModel.processWindowTitleChanges() }
        scope.advanceUntilIdle()

        // Check
        viewModel.windowTitle shouldBe "* path/to/file.jy"
    }

    @Test
    fun `window title changes - clean without path`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(false)
        every { documentContentTracker.externalPath } returns MutableStateFlow(null)

        // Do
        scope.launch { viewModel.processWindowTitleChanges() }
        scope.advanceUntilIdle()

        // Check
        viewModel.windowTitle shouldBe "Untitled"
    }

    @Test
    fun `window title changes - clean with path`() = runTest(dispatcher) {
        // Prepare
        every { documentContentTracker.dirtyState } returns MutableStateFlow(false)
        every { documentContentTracker.externalPath } returns MutableStateFlow("path/to/file.jy".toPath())

        // Do
        scope.launch { viewModel.processWindowTitleChanges() }
        scope.advanceUntilIdle()

        // Check
        viewModel.windowTitle shouldBe "path/to/file.jy"
    }
}
