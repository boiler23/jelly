package com.ilyabogdanovich.jelly.ide.app.domain.documents

import androidx.annotation.VisibleForTesting
import com.ilyabogdanovich.jelly.logging.LoggerFactory
import com.ilyabogdanovich.jelly.logging.get
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import okio.Path

/**
 * Implementation for [DocumentContentTracker]
 *
 * @author Ilya Bogdanovich on 22.10.2023
 */
class DocumentContentTrackerImpl(
    private val documentRepository: DocumentRepository,
    private val recentsRepository: RecentsRepository,
    loggerFactory: LoggerFactory
) : DocumentContentTracker {
    private val logger = loggerFactory.get<DocumentContentTracker>()
    override val dirtyState = MutableStateFlow(false)
    override val externalPath = MutableStateFlow<Path?>(null)
    override val internalContentChanges = MutableSharedFlow<Document>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    @VisibleForTesting
    internal val externalContentFlow = MutableStateFlow(Document.empty())

    override fun open(externalPath: Path) {
        val newContent = documentRepository.import(externalPath)
        externalContentFlow.value = newContent
        internalContentChanges.tryEmit(newContent)
        dirtyState.value = false
        this.externalPath.value = externalPath
        recentsRepository.write(externalPath)
    }

    override fun save(path: Path): Boolean {
        val exported = documentRepository.export(path)
        return if (exported != null) {
            dirtyState.value = false
            externalContentFlow.value = exported
            externalPath.value = path
            recentsRepository.write(path)
            true
        } else {
            false
        }
    }

    override fun new() {
        dirtyState.value = false
        externalPath.value = null
        externalContentFlow.value = Document.empty()
        internalContentChanges.tryEmit(Document.empty())
        recentsRepository.write(null)
        documentRepository.write(Document.empty())
    }

    override fun startup() {
        val externalPath = recentsRepository.read()
        logger.d { "external path: $externalPath" }
        this.externalPath.value = externalPath
        val localContent = documentRepository.read()
        internalContentChanges.tryEmit(localContent)
        if (externalPath != null) {
            val externalContent = documentRepository.readExternal(externalPath)
            externalContentFlow.value = externalContent
            dirtyState.value = externalContent.text != localContent.text
        } else {
            dirtyState.value = localContent.text.isNotEmpty()
        }
    }

    override fun handleContentChanges(content: Document) {
        dirtyState.value = content != externalContentFlow.value
        documentRepository.write(content)
    }
}
