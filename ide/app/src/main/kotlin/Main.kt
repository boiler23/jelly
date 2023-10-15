import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ilyabogdanovich.jelly.ide.app.data.compiler.CompilationServiceClientImpl
import com.ilyabogdanovich.jelly.ide.app.data.compiler.ErrorMarkupBuilderImpl
import com.ilyabogdanovich.jelly.ide.app.data.documents.DocumentRepositoryImpl
import com.ilyabogdanovich.jelly.ide.app.presentation.AppViewModel
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.App
import com.ilyabogdanovich.jelly.jcc.core.Compiler
import com.ilyabogdanovich.jelly.logging.DefaultLoggerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.FileSystem

/**
 * Application entry point.
 */
fun main() {
    // application-level coroutine scope
    val scope = CoroutineScope(Dispatchers.Default + Job())

    // di block begin
    // todo: introduce some DI framework, like Dagger
    val compiler = Compiler()
    val errorMarkupBuilder = ErrorMarkupBuilderImpl()
    val compilationServiceClient = CompilationServiceClientImpl(compiler, errorMarkupBuilder)
    val documentRepository = DocumentRepositoryImpl(FileSystem.SYSTEM, DefaultLoggerFactory)
    val viewModel = AppViewModel(compilationServiceClient, documentRepository, DefaultLoggerFactory)
    // di block end

    // launch view model event processing
    scope.launch { viewModel.processCompilationRequests() }
    scope.launch { viewModel.processDocumentUpdates() }
    scope.launch { viewModel.startApp() }

    // launch application UI
    application {
        Window(onCloseRequest = ::exitApplication) {
            App(
                splashScreenVisible = viewModel.splashScreenVisible,
                sourceInput = viewModel.sourceInput,
                errorMarkup = viewModel.errorMarkup,
                resultOutput = viewModel.resultOutput,
                errorOutput = viewModel.errorOutput,
                compilationTimeOutput = viewModel.compilationTimeOutput,
                compilationInProgress = viewModel.compilationInProgress,
                onSourceInputChanged = viewModel::notifySourceInputChanged,
            )
        }
    }
}
