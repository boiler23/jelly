package com.ilyabogdanovich.jelly.ide.app.di

import com.ilyabogdanovich.jelly.ide.app.data.compiler.CompilationServiceClientImpl
import com.ilyabogdanovich.jelly.ide.app.data.compiler.ErrorListBuilderImpl
import com.ilyabogdanovich.jelly.ide.app.data.compiler.ErrorMarkupBuilderImpl
import com.ilyabogdanovich.jelly.ide.app.data.compiler.OutputTrimmerImpl
import com.ilyabogdanovich.jelly.ide.app.data.documents.DocumentRepositoryImpl
import com.ilyabogdanovich.jelly.ide.app.data.documents.RecentsRepositoryImpl
import com.ilyabogdanovich.jelly.ide.app.domain.documents.DocumentContentTrackerImpl
import com.ilyabogdanovich.jelly.ide.app.presentation.MainContentViewModel
import com.ilyabogdanovich.jelly.ide.app.presentation.MainWindowViewModel
import com.ilyabogdanovich.jelly.jcc.core.di.CompilationServiceApi
import com.ilyabogdanovich.jelly.logging.DefaultLoggerFactory
import kotlinx.coroutines.Dispatchers
import okio.FileSystem

/**
 * micro-DI, implementing [MainApi].
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
class MainComponent(
    private val complicationServiceApi: CompilationServiceApi
) : MainApi {
    private val fileSystem
        get() = FileSystem.SYSTEM

    private val loggerFactory
        get() = DefaultLoggerFactory

    private val outputTrimmer by lazy { OutputTrimmerImpl() }

    private val errorListBuilder by lazy { ErrorListBuilderImpl() }

    private val errorMarkupBuilder by lazy { ErrorMarkupBuilderImpl() }

    private val compilationServiceClient by lazy {
        CompilationServiceClientImpl(
            complicationServiceApi.compilationService,
            outputTrimmer,
            errorListBuilder,
            errorMarkupBuilder,
        )
    }

    private val documentRepository by lazy { DocumentRepositoryImpl(fileSystem, loggerFactory) }

    private val recentsRepository by lazy { RecentsRepositoryImpl(fileSystem, loggerFactory) }

    private val documentContentTracker by lazy {
        DocumentContentTrackerImpl(
            documentRepository,
            recentsRepository,
            loggerFactory
        )
    }

    override val mainContentViewModel by lazy {
        MainContentViewModel(
            compilationServiceClient,
            documentContentTracker,
            loggerFactory,
        )
    }

    override val mainWindowViewModel by lazy {
        MainWindowViewModel(
            documentContentTracker,
            Dispatchers.Default,
            loggerFactory
        )
    }
}
