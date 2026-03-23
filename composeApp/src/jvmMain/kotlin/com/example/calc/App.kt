package com.example.calc

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowState

@Composable
fun App(
    windowState: WindowState,
    logic: CalculatorLogic,
    radixMode: String,
    onRadixChange: (String) -> Unit
) {
    var calcMode by remember { mutableIntStateOf(0) } // 0 - Std, 1 - Prg
    var showAbout by remember { mutableStateOf(false) }

    // Автоматическое изменение размера окна (About = Prg Size)
    LaunchedEffect(calcMode, showAbout) {
        windowState.size = when {
            showAbout || calcMode == 1 -> DpSize(477.dp, 600.dp)
            else -> DpSize(240.dp, 400.dp)
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize().focusable(),
            color = Color.LightGray
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Панель выбора режимов
                Row(
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeButton("Std", calcMode == 0 && !showAbout, {
                        calcMode = 0
                        showAbout = false
                    }, Modifier.weight(1f))

                    ModeButton("Prg", calcMode == 1 && !showAbout, {
                        calcMode = 1
                        showAbout = false
                    }, Modifier.weight(1f))

                    ModeButton("About", showAbout, {
                        showAbout = true
                    }, Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                if (showAbout) {
                    // Контент блока ABOUT (занимает всё пространство окна Prg)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(24.dp)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text("Embedded Calc v1.1", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF566EAC))
                            Spacer(Modifier.height(12.dp))
                            Text("Инженерный инструмент разработчика для работы с регистрами и битами (STM32/Embedded).", fontSize = 14.sp)
                            Spacer(Modifier.height(16.dp))
                            Text("Функции:", fontWeight = FontWeight.SemiBold)
                            val f = listOf("64-битный Long", "Побитовая панель 0-63", "HEX/BIN/DEC", "Сдвиги RoL/RoR/Lsh/Rsh", "Копирование по Ctrl+C")
                            f.forEach { Text("• $it", fontSize = 13.sp) }
                            Spacer(Modifier.height(24.dp))
                            Text("Created by Vasiltsov Yurii", fontWeight = FontWeight.Medium)
                            Text("telebite@yandex.ru")
                            Spacer(Modifier.height(16.dp))
                            Text("Barsik Approved 🐾", color = Color.Gray, fontSize = 12.sp)

                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = { showAbout = false },
                                modifier = Modifier.align(Alignment.End).padding(top = 20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF566EAC))
                            ) {
                                Text("Вернуться", color = Color.White)
                            }
                        }
                    }
                } else {
                    // Основной экран калькулятора
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = logic.displayText,
                            fontSize = 32.sp,
                            color = Color.Black,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        )
                    }

                    if (calcMode == 1) {
                        ProgrammerLayout(
                            logic = logic,
                            radix = radixMode,
                            onRadixChange = { newR ->
                                logic.convertRadix(radixMode, newR)
                                onRadixChange(newR)
                            },
                            onInput = { char -> logic.onInput(char, radixMode) }
                        )
                    } else {
                        StandardLayout(onInput = { char -> logic.onInput(char, "DEC") })
                    }
                }
            }
        }
    }
}

// --- ВСЕ ТВОИ ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ ---

@Composable
fun StandardLayout(onInput: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("←", "CE", "C", "±", "√").forEach { char ->
                CalcButton(char, Modifier.weight(1f).height(42.dp)) {
                    onInput(if (char == "C") "RESET_ALL" else char)
                }
            }
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(modifier = Modifier.weight(4f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val rows = listOf(listOf("7", "8", "9", "/"), listOf("4", "5", "6", "*"), listOf("1", "2", "3", "-"))
                rows.forEach { row ->
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { char -> CalcButton(char, Modifier.weight(1f).fillMaxHeight()) { onInput(char) } }
                    }
                }
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalcButton("0", Modifier.weight(2.05f).fillMaxHeight()) { onInput("0") }
                    CalcButton(",", Modifier.weight(1f).fillMaxHeight()) { onInput(",") }
                    CalcButton("+", Modifier.weight(1f).fillMaxHeight()) { onInput("+") }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcButton("%", Modifier.fillMaxWidth().height(42.dp)) { onInput("%") }
                CalcButton("1/x", Modifier.fillMaxWidth().height(42.dp)) { onInput("1/x") }
                CalcButton("=", Modifier.fillMaxWidth().weight(1f), isAccent = true) { onInput("=") }
            }
        }
    }
}

@Composable
fun ProgrammerLayout(logic: CalculatorLogic, radix: String, onRadixChange: (String) -> Unit, onInput: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxHeight()) {
        Spacer(Modifier.height(8.dp))
        Win7BitsPanel(bits = logic.getBitsString(radix), onBitClick = { logic.toggleBit(it, radix) })
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(
                modifier = Modifier.width(80.dp).fillMaxHeight().border(1.dp, Color.LightGray).padding(vertical = 12.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Hex", "Dec", "Bin").forEach { mode ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onRadixChange(mode.uppercase()) }) {
                        RadioButton(selected = radix == mode.uppercase(), onClick = { onRadixChange(mode.uppercase()) })
                        Text(mode, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val rows = listOf(
                    listOf("A", "(", ")", "Mod", "←", "CE", "C"),
                    listOf("B", "RoL", "RoR", "7", "8", "9", "/"),
                    listOf("C", "Or", "Xor", "4", "5", "6", "*"),
                    listOf("D", "Lsh", "Rsh", "1", "2", "3", "-"),
                    listOf("E", "Not", "And", "0", ",", "+")
                )
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEachIndexed { index, char ->
                            val isDigit = char.any { it.isDigit() }
                            val isHexL = index == 0 && char.length == 1 && char[0] in 'A'..'E'
                            val isEnabled = when (radix) {
                                "BIN" -> if (isDigit) (char == "0" || char == "1") else !isHexL
                                "DEC" -> !isHexL
                                else -> true
                            }
                            CalcButton(char, Modifier.size(if (char == "0") 86.dp else 40.dp, 40.dp), isEnabled) {
                                onInput(if (char == "C" && index != 0) "RESET_ALL" else char)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalcButton("F", Modifier.size(40.dp), enabled = (radix == "HEX")) { onInput("F") }
                    CalcButton("=", Modifier.size(270.dp, 40.dp), isAccent = true) { onInput("=") }
                }
            }
        }
    }
}

@Composable
fun CalcButton(text: String, modifier: Modifier, enabled: Boolean = true, isAccent: Boolean = false, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier, shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isAccent) Color(0xFF788EAF) else Color(0xFF5493A8),
            contentColor = if (isAccent) Color.White else Color.Black,
            disabledContainerColor = Color(0xFFF5F5F5)
        )
    ) { Text(text, fontSize = 14.sp) }
}

@Composable
fun ModeButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick, modifier = modifier.fillMaxHeight(), shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF566EAC) else Color.White,
            contentColor = if (isSelected) Color.White else Color(0xFF507DA4)
        ),
        border = BorderStroke(1.dp, Color(0xFF505EA4))
    ) { Text(text, fontSize = 14.sp) }
}

@Composable
fun Win7BitsPanel(bits: String, onBitClick: (Int) -> Unit) {
    @Composable
    fun BitGroup(group: String, startIndex: Int) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            group.forEachIndexed { i, char ->
                val actualBitIndex = 63 - (startIndex + i)
                Text(
                    text = char.toString(), fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                    color = if (char == '1') Color(0xFF005FB8) else Color.DarkGray,
                    modifier = Modifier.clickable { onBitClick(actualBitIndex) }.padding(horizontal = 1.dp)
                )
            }
        }
    }
    Box(Modifier.fillMaxWidth().background(Color(0xFFE1E8ED)).padding(8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { (0 until 8).forEach { BitGroup(bits.substring(it * 4, it * 4 + 4), it * 4) } }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("63", fontSize = 10.sp); Text("47", fontSize = 10.sp); Text("32", fontSize = 10.sp) }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { (8 until 16).forEach { BitGroup(bits.substring(it * 4, it * 4 + 4), it * 4) } }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("31", fontSize = 10.sp); Text("15", fontSize = 10.sp); Text("0", fontSize = 10.sp) }
        }
    }
}