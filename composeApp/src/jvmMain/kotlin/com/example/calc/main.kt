import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(
        size = DpSize(240.dp, 400.dp) // Возвращено к 240x375
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "CalcKb",
        state = windowState,
        resizable = false
    ) {
        App(windowState)
    }
}