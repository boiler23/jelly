package com.ilyabogdanovich.jelly.ide.app.di

import com.ilyabogdanovich.jelly.jcc.core.di.CompilationServiceApi

/**
 * Represents application DI graph.
 *
 * @author Ilya Bogdanovich on 21.10.2023
 */
interface AppComponent {
    val compilationServiceApi: CompilationServiceApi
    val mainApi: MainApi

    companion object {
        fun create(): AppComponent = object : AppComponent {
            override val compilationServiceApi by lazy { CompilationServiceApi.create() }
            override val mainApi by lazy { MainApi.create(compilationServiceApi) }
        }
    }
}
