package com.example.myapplication // ⚠️保留你真实的包名

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // 在这里传递菜单事件处理器
                        CalculatorTopBar(
                            onMenuAction = { action ->
                                // 顶层不处理，但需要传递给 Screen
                                // 这里只是示例，实际在 Screen 中处理
                            }
                        )
                    }
                ) { innerPadding ->
                    // 将菜单事件处理器传递给 CalculatorScreen
                    CalculatorScreen(
                        modifier = Modifier.padding(innerPadding),
                        onMenuAction = { action ->
                            when (action) {
                                "RESET" -> {
                                    // 重置逻辑在这里
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorTopBar(onMenuAction: (String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("计算器", fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF1C1C1E),
            titleContentColor = Color.White
        ),
        actions = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "打开菜单",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = Color(0xFF2C2C2E)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("重置计算器", color = Color.White)
                        },
                        onClick = {
                            showMenu = false
                            onMenuAction("RESET")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("关于", color = Color.White)
                        },
                        onClick = {
                            showMenu = false
                            onMenuAction("ABOUT")
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    onMenuAction: (String) -> Unit
) {
    var displayText by remember { mutableStateOf("0") }
    var currentInput by remember { mutableStateOf("") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var isNewInput by remember { mutableStateOf(false) }

    // 菜单事件处理
    fun handleMenuAction(action: String) {
        when (action) {
            "RESET" -> {
                currentInput = ""
                firstOperand = null
                operator = null
                isNewInput = false
                displayText = "0"
            }
        }
    }

    // 格式化数字，去掉尾随 .0
    fun formatNumber(num: Double): String {
        return if (num == num.toLong().toDouble()) {
            num.toLong().toString()
        } else {
            String.format("%.6f", num).trimEnd('0').trimEnd('.')
        }
    }

    // 核心计算逻辑
    fun calculate() {
        val first = firstOperand ?: return
        val second = currentInput.toDoubleOrNull() ?: return

        val result = when (operator) {
            "+" -> first + second
            "-" -> first - second
            "*" -> first * second
            "/" -> {
                if (second == 0.0) {
                    displayText = "错误"
                    currentInput = ""
                    firstOperand = null
                    operator = null
                    isNewInput = true
                    return
                }
                first / second
            }
            else -> second
        }

        currentInput = formatNumber(result)
        firstOperand = result
    }

    // 统一的按钮点击处理
    fun onButtonClick(action: String) {
        when {
            action in "0123456789" -> {
                if (isNewInput) {
                    currentInput = if (action == "0") "" else action
                    isNewInput = false
                } else {
                    if (currentInput == "0" && action != "0") currentInput = action
                    else if (currentInput != "0") currentInput += action
                }
            }
            action == "." -> {
                if (isNewInput) {
                    currentInput = "0."
                    isNewInput = false
                } else if (!currentInput.contains(".")) {
                    if (currentInput.isEmpty()) currentInput = "0"
                    currentInput += "."
                }
            }
            action in "+-*/" -> {
                if (currentInput.isNotEmpty()) {
                    if (firstOperand != null && operator != null && !isNewInput) {
                        calculate()
                    } else {
                        firstOperand = currentInput.toDoubleOrNull()
                    }
                    operator = action
                    isNewInput = true
                }
            }
            action == "=" -> {
                if (currentInput.isNotEmpty() && firstOperand != null && operator != null) {
                    calculate()
                    operator = null
                    isNewInput = true
                }
            }
            action == "C" -> {
                currentInput = ""
                firstOperand = null
                operator = null
                isNewInput = false
            }
            action == "⌫" -> {
                if (!isNewInput && currentInput.isNotEmpty()) {
                    currentInput = currentInput.dropLast(1)
                }
            }
        }

        displayText = when {
            currentInput.isEmpty() && !isNewInput -> "0"
            currentInput.isEmpty() && isNewInput && firstOperand != null -> formatNumber(firstOperand!!)
            else -> currentInput.ifEmpty { "0" }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E)),
        horizontalAlignment = Alignment.End
    ) {
        // 显示屏
        Text(
            text = displayText,
            color = Color.White,
            fontSize = 56.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
        )

        // 按钮区
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorRow(items = listOf("C", "⌫", "/", "*")) { onButtonClick(it) }
            CalculatorRow(items = listOf("7", "8", "9", "-")) { onButtonClick(it) }
            CalculatorRow(items = listOf("4", "5", "6", "+")) { onButtonClick(it) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(3f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcButton("1", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick("1") }
                        CalcButton("2", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick("2") }
                        CalcButton("3", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick("3") }
                    }
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcButton("0", Color(0xFF333333), Modifier.weight(2f)) { onButtonClick("0") }
                        CalcButton(".", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick(".") }
                    }
                }

                CalcButton(
                    text = "=",
                    bgColor = Color(0xFFFF9500),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    onButtonClick("=")
                }
            }
        }
    }
}

@Composable
fun CalculatorRow(items: List<String>, onAction: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val color = when {
                item in listOf("C", "⌫") -> Color(0xFFA5A5A5)
                item in "+-*/" -> Color(0xFFFF9500)
                else -> Color(0xFF333333)
            }
            CalcButton(item, color, Modifier.weight(1f)) { onAction(item) }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val textColor = if (bgColor == Color(0xFFA5A5A5)) Color.Black else Color.White

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(bgColor, MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = if (text == "⌫") 24.sp else 28.sp,
            fontWeight = FontWeight.W500
        )
    }
}
