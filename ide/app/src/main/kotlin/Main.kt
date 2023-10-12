import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ilyabogdanovich.jelly.jcc.core.Compiler
import com.ilyabogdanovich.jelly.ide.app.App
import com.ilyabogdanovich.jelly.ide.app.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Application entry point.
 */
fun main() {
    val scope = CoroutineScope(Dispatchers.Default + Job())
    val compiler = Compiler()
    val viewModel = AppViewModel(compiler)
    scope.launch { viewModel.subscribeForTextInput() }

    application {
        Window(onCloseRequest = ::exitApplication) {
            App(
                resultOutput = viewModel.resultOutput,
                errorOutput = viewModel.errorOutput,
                compilationTimeOutput = viewModel.compilationTimeOutput,
                compilationStatus = viewModel.compilationStatus,
                onInputTextChanged = viewModel::notifyNewTextInput,
            )
        }
    }
}
