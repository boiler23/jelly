package com.ilyabogdanovich.jelly.ide.app.domain.documents

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import okio.Path

/**
 * Tracks opened document status: it's path, changes and dirty state.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
interface DocumentContentTracker {
    /**
     * Allows to observe the changes to the content, caused by opening external files.
     */
    val internalContentChanges: Flow<Document>

    /**
     * Allows to observe changes to the opened document dirty state.
     * Dirty - means document has unsaved changes, compared to the original source content.
     */
    val dirtyState: StateFlow<Boolean>

    /**
     * Allows to track changes to the opened document external path.
     */
    val externalPath: StateFlow<Path?>

    /**
     * Runs actions needed on the app's startup - reads the contents of the recently opened document,
     * and updates [dirtyState] and [externalPath] accordingly.
     */
    @WorkerThread
    fun startup()

    /**
     * Creates a new document.
     */
    @WorkerThread
    fun new()

    /**
     * Opens a document with the given [externalPath].
     */
    @WorkerThread
    fun open(externalPath: Path)

    /**
     * Saves the opened document to the given [path].
     * @return true if save operation was successful. Otherwise, returns false.
     */
    @WorkerThread
    fun save(path: Path): Boolean

    /**
     * Handles changes to the opened content, made in the editor,
     * updates to the dirty state and stores the document contents to the local app storage.
     */
    @WorkerThread
    fun handleContentChanges(content: Document)
}
