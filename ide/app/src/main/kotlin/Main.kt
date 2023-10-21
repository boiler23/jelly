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
    val viewModel = diGraph.mainApi.viewModel

    // launch view model event processing
    scope.launch { viewModel.processCompilationRequests() }
    scope.launch { viewModel.processDocumentUpdates() }
    scope.launch { viewModel.startApp() }

    // launch application UI
    application { MainWindow(viewModel) }
}
