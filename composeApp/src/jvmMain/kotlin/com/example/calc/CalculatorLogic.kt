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
        val radixInt = if (radix == "HEX") 16 else if (radix == "BIN") 2 else 10

        when (char) {
            // "RESET_ALL" — это наш гарантированный сброс из Main.kt
            // "C" и "CE" работают как сброс ТОЛЬКО если это не HEX режим
            "RESET_ALL", "CE",
            if (radix != "HEX") "C" else "NOT_A_CLEAR_COMMAND" -> {
                displayText = "0"
                firstOperand = 0.0
                currentOperation = null
                isWaitingForNextNumber = false
            }
            "←" -> {
                if (!isWaitingForNextNumber) {
                    displayText = if (displayText.length > 1) {
                        val dropped = displayText.dropLast(1)
                        if (dropped == "-") "0" else dropped
                    } else "0"
                }
            }
            "±" -> {
                if (radix == "DEC") {
                    if (displayText != "0") {
                        displayText = if (displayText.startsWith("-")) displayText.drop(1) else "-$displayText"
                    }
                } else {
                    val currentValue = parseToLong(displayText, radixInt)
                    displayText = renderLong(-currentValue, radixInt)
                }
            }
            "√" -> {
                if (radix == "DEC") {
                    val currentValue = parseToDouble(displayText, 10)
                    if (currentValue >= 0) {
                        displayText = formatDec(sqrt(currentValue))
                    } else {
                        displayText = "Error"
                    }
                    isWaitingForNextNumber = true
                }
            }
            "Not" -> {
                val currentValue = parseToLong(displayText, radixInt)
                displayText = renderLong(currentValue.inv(), radixInt)
                isWaitingForNextNumber = true
            }
            "+", "-", "*", "/", "Mod", "And", "Or", "Xor", "Lsh", "Rsh", "RoL", "RoR", "%", "1/x" -> {
                val currentVal = parseToDouble(displayText, radixInt)

                when (char) {
                    "%" -> {
                        displayText = formatDec(currentVal / 100.0)
                        isWaitingForNextNumber = true
                        return
                    }
                    "1/x" -> {
                        displayText = if (currentVal != 0.0) formatDec(1.0 / currentVal) else "Error"
                        isWaitingForNextNumber = true
                        return
                    }
                }

                firstOperand = currentVal
                currentOperation = char
                isWaitingForNextNumber = true
            }
            "=" -> {
                val secondOperand = parseToDouble(displayText, radixInt)
                if (radix == "DEC") {
                    val res = when (currentOperation) {
                        "+" -> firstOperand + secondOperand
                        "-" -> firstOperand - secondOperand
                        "*" -> firstOperand * secondOperand
                        "/" -> if (secondOperand != 0.0) firstOperand / secondOperand else 0.0
                        else -> secondOperand
                    }
                    displayText = formatDec(res)
                } else {
                    val fL = firstOperand.toLong()
                    val sL = secondOperand.toLong()
                    val resL = when (currentOperation) {
                        "+" -> fL + sL
                        "-" -> fL - sL
                        "*" -> fL * sL
                        "/" -> if (sL != 0L) fL / sL else 0L
                        "Mod" -> if (sL != 0L) fL % sL else 0L
                        "And" -> fL and sL
                        "Or" -> fL or sL
                        "Xor" -> fL xor sL
                        "Lsh" -> fL shl sL.toInt()
                        "Rsh" -> fL ushr sL.toInt()
                        "RoL" -> java.lang.Long.rotateLeft(fL, sL.toInt())
                        "RoR" -> java.lang.Long.rotateRight(fL, sL.toInt())
                        else -> sL
                    }
                    displayText = renderLong(resL, radixInt)
                }
                currentOperation = null
                isWaitingForNextNumber = true
            }
            else -> { // Сюда теперь будет попадать "C" в режиме HEX
                if (isWaitingForNextNumber) {
                    displayText = if (char == ",") "0," else char
                    isWaitingForNextNumber = false
                } else {
                    if (char == ",") {
                        if (!displayText.contains(",")) {
                            displayText += ","
                        }
                    } else {
                        if (displayText == "0") {
                            displayText = char
                        } else if (displayText.length < 16) {
                            displayText += char
                        }
                    }
                }
            }
        }
    }

    fun getBitsString(radix: String): String {
        val v = parseToLong(displayText, if (radix == "HEX") 16 else if (radix == "BIN") 2 else 10)
        return v.toULong().toString(2).padStart(64, '0')
    }

    fun toggleBit(bitIndex: Int, radix: String) {
        val rInt = if (radix == "HEX") 16 else if (radix == "BIN") 2 else 10
        val v = parseToLong(displayText, rInt) xor (1L shl bitIndex)
        displayText = renderLong(v, rInt)
    }

    fun convertRadix(oldR: String, newR: String) {
        val oldInt = if (oldR == "HEX") 16 else if (oldR == "BIN") 2 else 10
        val newInt = if (newR == "HEX") 16 else if (newR == "BIN") 2 else 10
        val v = parseToLong(displayText, oldInt)
        displayText = renderLong(v, newInt)
    }

    private fun parseToLong(txt: String, r: Int): Long {
        val cleanTxt = txt.split(",")[0]
        return cleanTxt.toULongOrNull(r)?.toLong() ?: 0L
    }

    private fun parseToDouble(txt: String, r: Int): Double {
        return if (r == 10) {
            txt.replace(",", ".").toDoubleOrNull() ?: 0.0
        } else {
            parseToLong(txt, r).toDouble()
        }
    }

    private fun renderLong(v: Long, r: Int) = if (r == 10) v.toString() else v.toULong().toString(r).uppercase()

    private fun formatDec(v: Double): String {
        val s = v.toString()
        return if (s.endsWith(".0")) {
            s.substringBefore(".0")
        } else {
            s.replace(".", ",")
        }
    }
}