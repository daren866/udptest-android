package com.example.myapplication // ⚠️第一行请保留你真实的包名！

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
                    topBar = { CalculatorTopBar() }
                ) { innerPadding ->
                    CalculatorScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorTopBar() {
    TopAppBar(
        title = { Text("计算器", fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF1C1C1E),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    // 状态管理
    var displayText by remember { mutableStateOf("0") }
    var currentInput by remember { mutableStateOf("") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var isNewInput by remember { mutableStateOf(false) } // 标记是否刚刚按了运算符或等号

    // 格式化数字，去掉尾随的 .0
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
            // 1. 数字
            action in "0123456789" -> {
                if (isNewInput) {
                    currentInput = if (action == "0") "" else action
                    isNewInput = false
                } else {
                    if (currentInput == "0" && action != "0") currentInput = action
                    else if (currentInput != "0") currentInput += action
                }
            }
            // 2. 小数点
            action == "." -> {
                if (isNewInput) {
                    currentInput = "0."
                    isNewInput = false
                } else if (!currentInput.contains(".")) {
                    if (currentInput.isEmpty()) currentInput = "0"
                    currentInput += "."
                }
            }
            // 3. 运算符
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
            // 4. 等号
            action == "=" -> {
                if (currentInput.isNotEmpty() && firstOperand != null && operator != null) {
                    calculate()
                    operator = null
                    isNewInput = true
                }
            }
            // 5. 清除
            action == "C" -> {
                currentInput = ""
                firstOperand = null
                operator = null
                isNewInput = false
            }
            // 6. 退格
            action == "⌫" -> {
                if (!isNewInput && currentInput.isNotEmpty()) {
                    currentInput = currentInput.dropLast(1)
                }
            }
        }

        // 刷新屏幕显示文字
        displayText = when {
            currentInput.isEmpty() && !isNewInput -> "0"
            currentInput.isEmpty() && isNewInput && firstOperand != null -> formatNumber(firstOperand!!)
            else -> currentInput.ifEmpty { "0" }
        }
    }

    // ================= UI 绘制部分 =================
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
            // 前 4 行常规布局
            CalculatorRow(items = listOf("C", "⌫", "/", "*")) { onButtonClick(it) }
            CalculatorRow(items = listOf("7", "8", "9", "-")) { onButtonClick(it) }
            CalculatorRow(items = listOf("4", "5", "6", "+")) { onButtonClick(it) }

            // 最后 2 行特殊布局 (0跨两列，=跨两行)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 左侧 (1,2,3 和 0,.)
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

                // 右侧等号 (跨两行)
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

// 常规的 4 列按钮行
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

// 核心按钮组件：去除了过期的 rememberRipple，使用 Compose 默认的 M3 点击效果
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
            .clickable { onClick() } // 👈 直接使用最新的 clickable，不传 indication，系统会自动适配最新 M3 规范
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
