package com.example.calc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CalculatorLogic {
    var displayText by mutableStateOf("0")
        private set

    private var firstOperand: Double = 0.0
    private var currentOperation: String? = null
    private var isWaitingForNextNumber: Boolean = false

    fun onInput(char: String, radix: String = "DEC") {
        val radixInt = if (radix == "HEX") 16 else if (radix == "BIN") 2 else 10

        when (char) {
            "RESET_ALL", "C", "CE" -> {
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
                val currentValue = parseToLong(displayText, radixInt)
                displayText = renderLong(-currentValue, radixInt)
            }
            "Not" -> {
                val currentValue = parseToLong(displayText, radixInt)
                displayText = renderLong(currentValue.inv(), radixInt)
                isWaitingForNextNumber = true
            }
            "+", "-", "*", "/", "Mod", "And", "Or", "Xor", "Lsh", "Rsh", "RoL", "RoR" -> {
                firstOperand = parseToDouble(displayText, radixInt)
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
            else -> {
                if (isWaitingForNextNumber || displayText == "0") {
                    displayText = char
                    isWaitingForNextNumber = false
                } else {
                    if (displayText.length < 16) displayText += char
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

    private fun parseToLong(txt: String, r: Int) = txt.toULongOrNull(r)?.toLong() ?: 0L
    private fun parseToDouble(txt: String, r: Int) = if (r == 10) txt.replace(",", ".").toDoubleOrNull() ?: 0.0 else parseToLong(txt, r).toDouble()
    private fun renderLong(v: Long, r: Int) = if (r == 10) v.toString() else v.toULong().toString(r).uppercase()
    private fun formatDec(v: Double) = v.toString().replace(".0", "").replace(".", ",")
}