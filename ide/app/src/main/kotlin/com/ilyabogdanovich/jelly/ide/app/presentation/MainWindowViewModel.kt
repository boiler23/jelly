package com.ilyabogdanovich.jelly.ide.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ilyabogdanovich.jelly.ide.app.domain.documents.DocumentContentTracker
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds.ConfirmDialogResult
import com.ilyabogdanovich.jelly.logging.LoggerFactory
import com.ilyabogdanovich.jelly.logging.get
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import okio.Path
import okio.Path.Companion.toPath

/**
 * View model for the main window contents: menus, window title, etc.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
class MainWindowViewModel(
    private val documentContentTracker: DocumentContentTracker,
    private val workerDispatcher: CoroutineDispatcher,
    loggerFactory: LoggerFactory,
    private val openDialogState: DialogState<Path?> = DialogState(),
    private val saveDialogState: DialogState<Path?> = DialogState(),
    private val closeDialogState: DialogState<ConfirmDialogResult> = DialogState(),
    private val failedOpenDialogState: DialogState<Unit> = DialogState(),
) {
    private val logger = loggerFactory.get<MainWindowViewModel>()
    var windowTitle by mutableStateOf("")

    val isOpenFileDialogVisible: Boolean
        get() = openDialogState.isAwaiting

    val isSaveFileDialogVisible: Boolean
        get() = saveDialogState.isAwaiting

    val isCloseFileDialogVisible: Boolean
        get() = closeDialogState.isAwaiting

    val isFailedOpenDialogVisible: Boolean
        get() = failedOpenDialogState.isAwaiting

    private suspend fun askToSave(): Boolean {
        if (documentContentTracker.dirtyState.value) {
            when (closeDialogState.awaitResult()) {
                ConfirmDialogResult.Yes -> {
                    if (save()) {
                        return true
                    }
                }
                ConfirmDialogResult.No -> {
                    return true
                }
                ConfirmDialogResult.Cancel -> return false
                null -> return false
            }
        } else {
            return true
        }

        return false
    }

    /**
     * Runs a new document creation flow.
     */
    suspend fun new() = withContext(workerDispatcher) {
        if (askToSave()) {
            documentContentTracker.new()
        }
    }

    /**
     * Runs an open document flow. Asks to save the existing one, if it is dirty.
     */
    suspend fun open() = withContext(workerDispatcher) {
        if (askToSave()) {
            val path = openDialogState.awaitResult()
            if (path != null) {
                if (path.toString().endsWith(".jy")) {
                    logger.d { "Picked file: $path" }
                    documentContentTracker.open(path)
                } else {
                    logger.d { "Picked unsupported file: $path" }
                    failedOpenDialogState.awaitResult()
                }
            }
        }
    }

    /**
     * Runs a save document flow. Picks an external path, if it's not there yet.
     */
    suspend fun save(): Boolean = withContext(workerDispatcher) {
        val knownPath = documentContentTracker.externalPath.value
        if (knownPath == null) {
            val pickedPath = saveDialogState.awaitResult()
            if (pickedPath != null) {
                val finalPickedPath = if (!pickedPath.toString().endsWith(".jy")) {
                    "$pickedPath.jy".toPath()
                } else {
                    pickedPath
                }
                logger.d { "save path picked: $pickedPath" }
                documentContentTracker.save(finalPickedPath).also { logger.d { "save result: $it" } }
            } else {
                logger.d { "save path not picked" }
                false
            }
        } else {
            logger.d { "save path already known - saving" }
            documentContentTracker.save(knownPath).also { logger.d { "save result: $it" } }
        }
    }

    /**
     * Runs the startup flow, where we restore the contents from local storage.
     */
    fun startApp() {
        documentContentTracker.startup()
    }

    /**
     * Observes the changes to document path & dirty state, and updates window title accordingly.
     */
    suspend fun processWindowTitleChanges() {
        combine(
            documentContentTracker.dirtyState,
            documentContentTracker.externalPath
        ) { isDirty, path -> isDirty to path }
            .collectLatest { (isDirty, path) ->
                val fileName = path?.toString() ?: "Untitled"
                windowTitle = if (isDirty) "* $fileName" else fileName
            }
    }

    /**
     * Handles the result of the open file dialog.
     */
    fun openResult(path: Path?) =
        openDialogState.onResult(path)

    /**
     * Handles the result of the save file dialog.
     */
    fun saveResult(path: Path?) =
        saveDialogState.onResult(path)

    /**
     * Handles the result of the close file dialog (yes/no/cancel alert).
     */
    fun closeResult(result: ConfirmDialogResult) =
        closeDialogState.onResult(result)

    /**
     * Handles the result of the warning dialog about unsupported file type.
     */
    fun failedOpenResult() =
        failedOpenDialogState.onResult(Unit)

    class DialogState<T> {
        private var onResult: CompletableDeferred<T>? by mutableStateOf(null)

        val isAwaiting: Boolean
            get() = onResult != null

        suspend fun awaitResult(): T? {
            onResult = CompletableDeferred()
            val result = onResult?.await()
            onResult = null
            return result
        }

        fun onResult(result: T) = onResult?.complete(result)
    }
}
