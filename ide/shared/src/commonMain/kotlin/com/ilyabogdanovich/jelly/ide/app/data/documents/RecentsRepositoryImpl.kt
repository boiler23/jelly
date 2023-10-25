package com.ilyabogdanovich.jelly.ide.app.data.documents

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.ilyabogdanovich.jelly.ide.app.domain.documents.RecentsRepository
import com.ilyabogdanovich.jelly.logging.LoggerFactory
import com.ilyabogdanovich.jelly.logging.get
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * Implementation for [RecentsRepository]
 *
 * @author Ilya Bogdanovich on 22.10.2023
 */
class RecentsRepositoryImpl(
    private val fileSystem: FileSystem,
    loggerFactory: LoggerFactory
) : RecentsRepository {
    private val logger = loggerFactory.get<RecentsRepositoryImpl>()

    @WorkerThread
    override fun read() = try {
        fileSystem.read(RECENTS) {
            readUtf8()
        }.toPath()
    } catch (e: IOException) {
        logger.d(e) { "Failed to read recents due to an exception" }
        null
    }

    @WorkerThread
    override fun write(path: Path?) {
        if (path != null) {
            fileSystem.createDirectories(INTERNAL_DIR)
            fileSystem.write(RECENTS) {
                writeUtf8(path.toString())
            }
        } else {
            fileSystem.delete(RECENTS)
        }
    }

    companion object {
        @VisibleForTesting
        internal val RECENTS = INTERNAL_DIR / "recents"
    }
}
