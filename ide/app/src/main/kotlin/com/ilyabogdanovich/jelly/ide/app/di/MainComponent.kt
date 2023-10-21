package com.ilyabogdanovich.jelly.ide.app.di

import com.ilyabogdanovich.jelly.ide.app.data.compiler.CompilationServiceClientImpl
import com.ilyabogdanovich.jelly.ide.app.data.compiler.ErrorListBuilderImpl
import com.ilyabogdanovich.jelly.ide.app.data.compiler.ErrorMarkupBuilderImpl
import com.ilyabogdanovich.jelly.ide.app.data.compiler.OutputTrimmerImpl
import com.ilyabogdanovich.jelly.ide.app.data.documents.DocumentRepositoryImpl
import com.ilyabogdanovich.jelly.ide.app.presentation.MainViewModel
import com.ilyabogdanovich.jelly.jcc.core.di.CompilationServiceApi
import com.ilyabogdanovich.jelly.logging.DefaultLoggerFactory
import okio.FileSystem

/**
 * micro-DI, implementing [MainApi].
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
class MainComponent(
    private val complicationServiceApi: CompilationServiceApi
) : MainApi {
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
    private val documentRepository by lazy {
        DocumentRepositoryImpl(FileSystem.SYSTEM, DefaultLoggerFactory)
    }
    override val viewModel by lazy {
        MainViewModel(
            compilationServiceClient,
            documentRepository,
            DefaultLoggerFactory,
        )
    }
}
