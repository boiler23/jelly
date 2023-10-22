import androidx.compose.ui.window.application
import com.ilyabogdanovich.jelly.ide.app.di.AppComponent
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.MainWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Application entry point.
 */
fun main() {
    // application-level coroutine scope
    val scope = CoroutineScope(Dispatchers.Default + Job())

    val diGraph = AppComponent.create()
    val mainWindowViewModel = diGraph.mainApi.mainWindowViewModel
    val mainContentViewModel = diGraph.mainApi.mainContentViewModel

    // launch view model event processing
    scope.launch { mainContentViewModel.processCompilationRequests() }
    scope.launch { mainContentViewModel.processDocumentUpdates() }
    scope.launch { mainContentViewModel.processContentChanges() }
    scope.launch { mainWindowViewModel.processWindowTitleChanges() }
    scope.launch { mainWindowViewModel.startApp() }

    // launch application UI
    application { MainWindow(mainWindowViewModel, mainContentViewModel) }
}
