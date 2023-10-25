package com.ilyabogdanovich.jelly.ide.app.domain.documents

import androidx.annotation.WorkerThread
import okio.Path

/**
 * Repository, holding the state of the edited document.
 *
 * @author Ilya Bogdanovich on 13.10.2023
 */
interface DocumentRepository {
    /**
     * Reads the document from the persistent storage.
     * As this is an I/O operation, it should be executed on a worker thread.
     * @return [Document] object with the read state.
     *         If no document is persisted, or reading failed - returns empty [Document]
     */
    @WorkerThread
    fun read(): Document

    /**
     * Updates the persistent storage with the given document [state].
     * As this is an I/O operation, it should be executed on a worker thread.
     * @param state state of the ducment to update.
     */
    @WorkerThread
    fun write(state: Document)

    /**
     * Imports an external document with the given path into the internal storage.
     * As this is an I/O operation, it should be executed on a worker thread.
     * @param from path to the document to import from.
     * @return imported [Document]. If import fails - the returned document is empty.
     */
    @WorkerThread
    fun import(from: Path): Document

    /**
     * Exports document from the internal storage into the given path [to].
     * As this is an I/O operation, it should be executed on a worker thread.
     * @param to path to export the internal document to.
     * @return [Document] with the exported contents, or null if export failed.
     */
    @WorkerThread
    fun export(to: Path): Document?

    /**
     * Reads an external document contents without importing them.
     * As this is an I/O operation, it should be executed on a worker thread.
     * @param from path to the document to read from.
     * @return read [Document]. If reading fails - the returned document is empty.
     */
    @WorkerThread
    fun readExternal(from: Path): Document
}
