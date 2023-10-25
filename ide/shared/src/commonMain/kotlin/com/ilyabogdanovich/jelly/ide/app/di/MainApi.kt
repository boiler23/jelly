package com.ilyabogdanovich.jelly.ide.app.di

import com.ilyabogdanovich.jelly.ide.app.presentation.MainContentViewModel
import com.ilyabogdanovich.jelly.ide.app.presentation.MainWindowViewModel
import com.ilyabogdanovich.jelly.jcc.core.di.CompilationServiceApi

/**
 * Api, providing main app entities.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
interface MainApi {
    val mainContentViewModel: MainContentViewModel
    val mainWindowViewModel: MainWindowViewModel

    companion object {
        fun create(compilationServiceApi: CompilationServiceApi): MainApi =
            MainComponent(compilationServiceApi)
    }
}
