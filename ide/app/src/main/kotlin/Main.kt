import androidx.compose.ui.window.application
import com.ilyabogdanovich.jelly.ide.app.data.compiler.CompilationServiceClientImpl
import com.ilyabogdanovich.jelly.ide.app.data.compiler.ErrorListBuilderImpl
import com.ilyabogdanovich.jelly.ide.app.data.compiler.ErrorMarkupBuilderImpl
import com.ilyabogdanovich.jelly.ide.app.data.compiler.OutputTrimmerImpl
import com.ilyabogdanovich.jelly.ide.app.data.documents.DocumentRepositoryImpl
import com.ilyabogdanovich.jelly.ide.app.presentation.MainViewModel
import com.ilyabogdanovich.jelly.ide.app.presentation.compose.MainWindow
import com.ilyabogdanovich.jelly.jcc.core.di.CompilationServiceApi
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
    val compilationService = CompilationServiceApi.create().compilationService
    val errorListBuilder = ErrorListBuilderImpl()
    val errorMarkupBuilder = ErrorMarkupBuilderImpl()
    val outputTrimmer = OutputTrimmerImpl()
    val compilationServiceClient =
        CompilationServiceClientImpl(compilationService, outputTrimmer, errorListBuilder, errorMarkupBuilder)
    val documentRepository = DocumentRepositoryImpl(FileSystem.SYSTEM, DefaultLoggerFactory)
    val viewModel = MainViewModel(compilationServiceClient, documentRepository, DefaultLoggerFactory)
    // di block end

    // launch view model event processing
    scope.launch { viewModel.processCompilationRequests() }
    scope.launch { viewModel.processDocumentUpdates() }
    scope.launch { viewModel.startApp() }

    // launch application UI
    application { MainWindow(viewModel) }
}
