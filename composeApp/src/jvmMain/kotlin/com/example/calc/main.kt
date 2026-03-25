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
    // Настройки отрисовки для стабильности на Windows
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
                val keyChar = e.keyChar.lowercaseChar()

                when {
                    // 1. Копирование в буфер (Ctrl+C)
                    isCtrl && (keyCode == KeyEvent.VK_C || keyChar == 'c') -> {
                        try {
                            val selection = StringSelection(logic.displayText)
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
                        } catch (ex: Exception) { }
                        return@KeyEventDispatcher true
                    }

                    // 2. КОРЕНЬ по клавише R (теперь в приоритете)
                    !isCtrl && (keyCode == KeyEvent.VK_R || keyChar == 'r') -> {
                        logic.onInput("√", radixMode)
                        return@KeyEventDispatcher true
                    }

                    // 3. Ввод цифр 0-9
                    !isCtrl && e.keyChar.isDigit() -> {
                        logic.onInput(e.keyChar.toString(), radixMode)
                        return@KeyEventDispatcher true
                    }

                    // 4. Ввод букв A-F (только в режиме HEX). 'r' сюда уже не попадет.
                    !isCtrl && radixMode == "HEX" && keyChar in 'a'..'f' -> {
                        logic.onInput(keyChar.uppercase(), radixMode)
                        return@KeyEventDispatcher true
                    }

                    // 5. Клавиша C: Твоя логика обработки (Цифра в HEX / Сброс в других режимах)
                    !isCtrl && keyCode == KeyEvent.VK_C -> {
                        logic.onInput("C", radixMode)
                        return@KeyEventDispatcher true
                    }

                    // 6. ГАРАНТИРОВАННЫЙ СБРОС (Clear) для любого режима
                    !isCtrl && (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_DELETE) -> {
                        logic.onInput("RESET_ALL", radixMode)
                        return@KeyEventDispatcher true
                    }

                    // 7. Арифметика и Enter
                    !isCtrl && (keyCode == KeyEvent.VK_ENTER || keyChar == '=') -> {
                        logic.onInput("=", radixMode)
                        return@KeyEventDispatcher true
                    }
                    !isCtrl && keyChar == '+' -> {
                        logic.onInput("+", radixMode)
                        return@KeyEventDispatcher true
                    }
                    !isCtrl && (keyChar == '-' || keyCode == KeyEvent.VK_SUBTRACT) -> {
                        logic.onInput("-", radixMode)
                        return@KeyEventDispatcher true
                    }
                    !isCtrl && (keyChar == '*' || keyCode == KeyEvent.VK_MULTIPLY) -> {
                        logic.onInput("*", radixMode)
                        return@KeyEventDispatcher true
                    }
                    !isCtrl && (keyChar == '/' || keyCode == KeyEvent.VK_DIVIDE) -> {
                        logic.onInput("/", radixMode)
                        return@KeyEventDispatcher true
                    }

                    // 8. Стирание последнего символа (Backspace)
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
        alwaysOnTop = false
    ) {
        LaunchedEffect(Unit) { window.requestFocus() }
        App(windowState, logic, radixMode, onRadixChange = { radixMode = it })
    }
}