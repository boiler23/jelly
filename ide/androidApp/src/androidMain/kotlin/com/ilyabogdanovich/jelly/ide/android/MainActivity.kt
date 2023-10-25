package com.ilyabogdanovich.jelly.ide.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ilyabogdanovich.jelly.ide.app.data.documents.INTERNAL_DIR
import com.ilyabogdanovich.jelly.ide.app.di.MainApi
import com.ilyabogdanovich.jelly.ide.app.presentation.MainContentViewModel
import com.ilyabogdanovich.jelly.ide.app.presentation.MainWindowViewModel
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.MainView
import com.ilyabogdanovich.jelly.jcc.core.di.CompilationServiceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.Path.Companion.toOkioPath

/**
 * Main activity for the Android app.
 *
 * @author Ilya Bogdanovich on 25.10.2023
 */
class MainActivity : AppCompatActivity() {
    private val mainContentViewModel: MainContentViewModel by viewModels { ViewModelFactory }
    private val mainWindowViewModel: MainWindowViewModel by viewModels { ViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        INTERNAL_DIR = filesDir.toOkioPath()

        lifecycleScope.launch(Dispatchers.Default) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainContentViewModel.processCompilationRequests()
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainContentViewModel.processDocumentUpdates()
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainContentViewModel.processContentChanges()
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainWindowViewModel.processWindowTitleChanges()
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainWindowViewModel.startApp()
            }
        }

        setContent {
            MainView(
                splashScreenVisible = mainContentViewModel.splashScreenVisible,
                sourceInput = mainContentViewModel.sourceInput,
                errorMarkup = mainContentViewModel.errorMarkup,
                resultOutput = mainContentViewModel.resultOutput,
                navigationEffect = mainContentViewModel.navigationEffect,
                errorMessages = mainContentViewModel.errorMessages,
                compilationStatus = mainContentViewModel.compilationStatus,
                onSourceInputChanged = mainContentViewModel::notifySourceInputChanged,
                onDeepLinkClicked = mainContentViewModel::notifyDeepLinkClicked,
            )
        }
    }
}

val ViewModelFactory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            MainContentViewModel::class.java -> mainApi.mainContentViewModel as T
            MainWindowViewModel::class.java -> mainApi.mainWindowViewModel as T
            else -> throw IllegalStateException("ViewModel class is not registered: $modelClass")
        }
    }
}

private val compilationServiceApi = CompilationServiceApi.create()
private val mainApi = MainApi.create(compilationServiceApi)

