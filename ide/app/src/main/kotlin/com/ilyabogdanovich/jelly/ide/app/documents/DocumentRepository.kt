package com.ilyabogdanovich.jelly.ide.app.documents

import androidx.annotation.WorkerThread

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
}
