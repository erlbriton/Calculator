package com.example.calc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowState

@Composable
fun App(windowState: WindowState) {
    var calcMode by remember { mutableIntStateOf(0) } // 0 - Std, 1 - Prg
    var radixMode by remember { mutableStateOf("DEC") }
    var displayText by remember { mutableStateOf("0") }

    // Динамическое изменение размера окна
    LaunchedEffect(calcMode) {
        if (calcMode == 1) {
            windowState.size = DpSize(435.dp, 570.dp)
        } else {
            // Строго 240x400, как в main
            windowState.size = DpSize(240.dp, 400.dp)
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier
            .fillMaxSize(),
            color = Color.LightGray) {
            Column(modifier = Modifier.padding(12.dp)) {

                // ВЕРХНЯЯ ПАНЕЛЬ
                Row(
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeButton("Std", calcMode == 0, { calcMode = 0 }, Modifier.weight(1f))
                    ModeButton("Prg", calcMode == 1, { calcMode = 1 }, Modifier.weight(1f))
                    ModeButton("About", false, { /* О программе */ }, Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                // ДИСПЛЕЙ
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color(0xFFFFFFFF), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(displayText, fontSize = 32.sp, color = Color.Black)
                }

                if (calcMode == 1) {
                    ProgrammerLayout(radixMode, { radixMode = it }, { displayText = it })
                } else {
                    StandardLayout({ displayText = it })
                }
            }
        }
    }
}

@Composable
fun StandardLayout(onInput: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("←", "CE", "C", "±", "√").forEach {
                CalcButton(it, Modifier.weight(1f).height(42.dp)) { onInput(it) }
            }
        }

        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(modifier = Modifier.weight(4f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val rows = listOf(
                    listOf("7", "8", "9", "/"),
                    listOf("4", "5", "6", "*"),
                    listOf("1", "2", "3", "-")
                )
                rows.forEach { row ->
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { char ->
                            CalcButton(char, Modifier.weight(1f).fillMaxHeight()) { onInput(char) }
                        }
                    }
                }
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalcButton("0", Modifier.weight(2.05f).fillMaxHeight()) { onInput("0") }
                    CalcButton(",", Modifier.weight(1f).fillMaxHeight()) { onInput(",") }
                    CalcButton("+", Modifier.weight(1f).fillMaxHeight()) { onInput("+") }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcButton("%", Modifier.fillMaxWidth().height(42.dp))
                CalcButton("1/x", Modifier.fillMaxWidth().height(42.dp))
                CalcButton("=", Modifier.fillMaxWidth().weight(1f), isAccent = true)
            }
        }
    }
}

@Composable
fun ProgrammerLayout(radix: String, onRadixChange: (String) -> Unit, onInput: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxHeight()) {
        Spacer(Modifier.height(8.dp))
        Win7BitsPanel()
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .border(1.dp, Color.LightGray)
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                listOf("Hex", "Dec", "Bin").forEach { mode ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = radix == mode.uppercase(),
                            onClick = { onRadixChange(mode.uppercase()) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6750A4))
                        )
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
                            // Считаем HEX-буквой только если символ в первом столбце (A, B, C, D, E)
                            val isHexLetter = index == 0 && char.length == 1 && char[0] in 'A'..'E'

                            val isEnabled = when (radix) {
                                "BIN" -> if (isDigit) (char == "0" || char == "1") else !isHexLetter
                                "DEC" -> !isHexLetter
                                else -> true
                            }

                            val width = if (char == "0") 86.dp else 40.dp
                            CalcButton(char, Modifier.size(width, 40.dp), enabled = isEnabled) { onInput(char) }
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
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isAccent) Color(0xFF788EAF) else Color(0xFF5493A8),
            contentColor = if (isAccent) Color.White else Color(0xFF000000),
            disabledContainerColor = Color(0xFFF5F5F5)
        )
    ) {
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun ModeButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF566EAC) else Color.White,
            contentColor = if (isSelected) Color.White else Color(0xFF507DA4)
        ),
        border = BorderStroke(1.dp, Color(0xFF505EA4))
    ) {
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun Win7BitsPanel() {
    Box(Modifier.fillMaxWidth().background(Color(0xFFE1E8ED)).padding(8.dp)) {
        Column {
            Text("0000  0000  0000  0000  0000  0000  0000  0000", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("63", fontSize = 10.sp); Text("47", fontSize = 10.sp); Text("32", fontSize = 10.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text("0000  0000  0000  0000  0000  0000  0000  0000", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("31", fontSize = 10.sp); Text("15", fontSize = 10.sp); Text("0", fontSize = 10.sp)
            }
        }
    }
}