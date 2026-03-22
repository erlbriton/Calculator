package com.example.calc

import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.KeyboardFocusManager
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

fun main() = application {
    System.setProperty("compose.interop.blending", "true")
    System.setProperty("skiko.renderApi", "SOFTWARE")

    val windowState = rememberWindowState(size = DpSize(240.dp, 400.dp))
    val logic = remember { CalculatorLogic() }
    var radixMode by remember { mutableStateOf("DEC") }

    DisposableEffect(Unit) {
        val dispatcher = java.awt.KeyEventDispatcher { e ->
            if (e.id == KeyEvent.KEY_PRESSED) {
                val isCtrl = e.isControlDown
                val keyCode = e.keyCode
                val keyChar = e.keyChar

                when {
                    isCtrl && (keyCode == KeyEvent.VK_C || keyCode == 0 || keyChar.lowercaseChar() == 'c') -> {
                        try {
                            val selection = StringSelection(logic.displayText)
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
                        } catch (ex: Exception) { }
                        return@KeyEventDispatcher true
                    }
                    !isCtrl && keyChar.isDigit() -> {
                        logic.onInput(keyChar.toString(), radixMode)
                        return@KeyEventDispatcher true
                    }
                    !isCtrl && (keyCode == KeyEvent.VK_BACK_SPACE || keyChar == '\b') -> {
                        logic.onInput("←", radixMode)
                        return@KeyEventDispatcher true
                    }
                }
            }
            false
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
        onDispose {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "CalcKb",
        state = windowState,
        resizable = false,
        alwaysOnTop = true
    ) {
        LaunchedEffect(Unit) { window.requestFocus() }
        App(windowState, logic, radixMode, onRadixChange = { radixMode = it })
    }
}