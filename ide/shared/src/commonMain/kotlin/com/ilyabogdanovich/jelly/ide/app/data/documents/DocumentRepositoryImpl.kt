package com.ilyabogdanovich.jelly.ide.app.data.documents

import androidx.annotation.VisibleForTesting
import com.ilyabogdanovich.jelly.ide.app.domain.documents.Document
import com.ilyabogdanovich.jelly.ide.app.domain.documents.DocumentRepository
import com.ilyabogdanovich.jelly.logging.LoggerFactory
import com.ilyabogdanovich.jelly.logging.get
import okio.FileSystem
import okio.Path
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Implementation for [DocumentRepository]
 *
 * @author Ilya Bogdanovich on 13.10.2023
 */
class DocumentRepositoryImpl(
    private val fileSystem: FileSystem,
    loggerFactory: LoggerFactory
) : DocumentRepository {
    private val logger = loggerFactory.get<DocumentRepositoryImpl>()

    override fun read() = try {
        val text = fileSystem.read(INTERNAL_SOURCE) { readUtf8() }
        Document(text = text)
    } catch (e: FileNotFoundException) {
        logger.d(e) { "File not found - likely no cache yet" }
        Document.empty()
    } catch (e: IOException) {
        logger.e(e) { "IO exception occurred while restoring the source state" }
        Document.empty()
    }

    override fun write(state: Document) = try {
        if (!fileSystem.exists(INTERNAL_DIR)) {
            fileSystem.createDirectories(INTERNAL_DIR)
        }
        fileSystem.write(INTERNAL_SOURCE) {
            writeUtf8(state.text)
            Unit
        }
    } catch (e: IOException) {
        logger.e(e) { "IO exception occurred while storing the source state" }
    }

    override fun import(from: Path): Document = try {
        fileSystem.createDirectories(INTERNAL_DIR)
        fileSystem.copy(from, INTERNAL_SOURCE)
        read()
    } catch (e: IOException) {
        logger.d(e) { "I/O exception occured while trying to import from $from" }
        Document.empty()
    }

    override fun export(to: Path) = try {
        to.parent?.let { fileSystem.createDirectories(it) }
        fileSystem.copy(INTERNAL_SOURCE, to)
        read()
    } catch (e: IOException) {
        logger.d(e) { "Failed to export internal content to $to" }
        null
    }

    override fun readExternal(from: Path) = try {
        val text = fileSystem.read(from) { readUtf8() }
        Document(text = text)
    } catch (e: IOException) {
        logger.e(e) { "IO exception occurred while reading file $from" }
        Document.empty()
    }

    companion object {
        @VisibleForTesting
        internal val INTERNAL_SOURCE = INTERNAL_DIR / "source.jy"
    }
}
