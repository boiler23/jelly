package com.ilyabogdanovich.jelly.ide.app.domain.documents

import okio.Path

/**
 * Repository, persisting the recently opened file path.
 *
 * @author Ilya Bogdanovich on 22.10.2023
 */
interface RecentsRepository {
    /**
     * Read the recently opened path from the repository.
     * @return [Path] to the recently opened file, or null if nothing is stored or reading failed.
     */
    fun read(): Path?

    /**
     * Writes the most recently opened path to the repository.
     * @param path path of the file to write, or null - if it needs to be cleaned up.
     */
    fun write(path: Path?)
}
