package com.example.calc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.sqrt

class CalculatorLogic {
    var displayText by mutableStateOf("0")
        private set

    private var firstOperand: Double = 0.0
    private var currentOperation: String? = null
    private var isWaitingForNextNumber: Boolean = false

    fun onInput(char: String, radix: String = "DEC") {
        val radixInt = when (radix) {
            "HEX" -> 16
            "BIN" -> 2
            else -> 10
        }

        when (char) {
            // МЕНЯЕМ СТРОГО: Реагируем на специальную команду сброса
            "RESET_ALL", "CE" -> {
                displayText = "0"
                firstOperand = 0.0
                currentOperation = null
                isWaitingForNextNumber = false
            }

            "←" -> {
                if (!isWaitingForNextNumber) {
                    displayText = if (displayText.length > 1) displayText.dropLast(1) else "0"
                }
            }

            "±" -> {
                if (displayText != "0") {
                    displayText = if (displayText.startsWith("-")) displayText.substring(1) else "-$displayText"
                }
            }

            "√" -> {
                val currentVal = parseToDouble(displayText, radixInt)
                if (currentVal >= 0) {
                    displayText = formatResult(sqrt(currentVal), radix)
                    isWaitingForNextNumber = true
                } else {
                    displayText = "Error"
                }
            }

            "1/x" -> {
                val currentVal = parseToDouble(displayText, radixInt)
                if (currentVal != 0.0) {
                    displayText = formatResult(1.0 / currentVal, radix)
                } else {
                    displayText = "Error"
                }
                isWaitingForNextNumber = true
            }

            "%" -> {
                val currentVal = parseToDouble(displayText, radixInt)
                displayText = formatResult(currentVal / 100.0, radix)
                isWaitingForNextNumber = true
            }

            "+", "-", "*", "/", "Mod", "And", "Or", "Xor" -> {
                firstOperand = parseToDouble(displayText, radixInt)
                currentOperation = char
                isWaitingForNextNumber = true
            }

            "=" -> {
                val secondOperand = parseToDouble(displayText, radixInt)
                val result = when (currentOperation) {
                    "+" -> firstOperand + secondOperand
                    "-" -> firstOperand - secondOperand
                    "*" -> firstOperand * secondOperand
                    "/" -> if (secondOperand != 0.0) firstOperand / secondOperand else Double.NaN
                    "Mod" -> if (secondOperand != 0.0) firstOperand % secondOperand else Double.NaN
                    "And" -> (firstOperand.toLong() and secondOperand.toLong()).toDouble()
                    "Or" -> (firstOperand.toLong() or secondOperand.toLong()).toDouble()
                    "Xor" -> (firstOperand.toLong() xor secondOperand.toLong()).toDouble()
                    else -> secondOperand
                }
                displayText = if (result.isNaN()) "Error" else formatResult(result, radix)
                currentOperation = null
                isWaitingForNextNumber = true
            }

            "," -> {
                if (radix == "DEC" && !displayText.contains(",")) {
                    if (isWaitingForNextNumber) {
                        displayText = "0,"
                        isWaitingForNextNumber = false
                    } else if (displayText.length < 9) {
                        displayText += ","
                    }
                }
            }

            else -> {
                // Сюда попадут все цифры, HEX-буквы (включая "C")
                if (isWaitingForNextNumber || displayText == "0") {
                    displayText = char
                    isWaitingForNextNumber = false
                } else {
                    val limit = if (radix == "DEC") 9 else 16
                    if (displayText.length < limit) {
                        displayText += char
                    }
                }
            }
        }
    }

    fun convertRadix(oldRadix: String, newRadix: String) {
        val oldRadixInt = if (oldRadix == "HEX") 16 else if (oldRadix == "BIN") 2 else 10
        val value = parseToDouble(displayText, oldRadixInt)
        displayText = formatResult(value, newRadix)
    }

    private fun parseToDouble(text: String, radix: Int): Double {
        val cleanText = text.replace(",", ".")
        return if (radix == 10) cleanText.toDoubleOrNull() ?: 0.0
        else cleanText.toLongOrNull(radix)?.toDouble() ?: 0.0
    }

    private fun formatResult(value: Double, radix: String): String {
        if (radix != "DEC") {
            val radixInt = if (radix == "HEX") 16 else 2
            val res = value.toLong().toString(radixInt).uppercase()
            return if (res.length > 16) res.take(16) else res
        }
        val s = value.toString()
        var formatted = if (s.endsWith(".0")) s.substring(0, s.length - 2) else s
        if (formatted.length > 9) {
            formatted = if (value > 999999999 || value < -99999999) "%.3e".format(value) else formatted.take(9)
        }
        return formatted.trimEnd('.').replace(".", ",")
    }
}