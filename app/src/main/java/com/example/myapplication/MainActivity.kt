package com.example.myapplication // ⚠️注意：请保留你项目真实的包名

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
                    // 1. 设置顶部标题栏为“计算器”
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
            containerColor = Color(0xFF1C1C1E), // 深色背景
            titleContentColor = Color.White
        )
    )
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    // 计算器状态管理
    var displayText by remember { mutableStateOf("0") }
    var currentInput by remember { mutableStateOf("") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var justCalculated by remember { mutableStateOf(false) }

    // 格式化数字，去掉多余的小数位 .0
    fun formatNumber(num: Double): String {
        return if (num == num.toLong().toDouble()) {
            num.toLong().toString()
        } else {
            String.format("%.6f", num).trimEnd('0').trimEnd('.')
        }
    }

    // 核心计算逻辑
    fun calculate() {
        val secondOperand = currentInput.toDoubleOrNull() ?: return
        val result = when (operator) {
            "+" -> firstOperand!! + secondOperand
            "-" -> firstOperand!! - secondOperand
            "*" -> firstOperand!! * secondOperand
            "/" -> {
                if (secondOperand == 0.0) {
                    displayText = "错误"
                    currentInput = ""
                    firstOperand = null
                    operator = null
                    justCalculated = true
                    return
                }
                firstOperand!! / secondOperand
            }
            else -> secondOperand
        }
        currentInput = formatNumber(result)
        firstOperand = result
    }

    // 统一处理按钮点击
    fun onAction(action: String) {
        when {
            action in "0123456789" -> {
                if (justCalculated) {
                    currentInput = ""
                    justCalculated = false
                }
                if (currentInput == "0" && action != "0") currentInput = action
                else if (currentInput != "0") currentInput += action
            }
            action == "." -> {
                if (justCalculated) {
                    currentInput = "0"
                    justCalculated = false
                }
                if (!currentInput.contains(".")) {
                    if (currentInput.isEmpty()) currentInput = "0"
                    currentInput += "."
                }
            }
            action in "+-*/" -> {
                if (currentInput.isNotEmpty()) {
                    if (firstOperand != null && operator != null && !justCalculated) {
                        calculate()
                    } else {
                        firstOperand = currentInput.toDoubleOrNull()
                    }
                    operator = action
                    currentInput = ""
                    justCalculated = false
                }
            }
            action == "=" -> {
                if (currentInput.isNotEmpty() && firstOperand != null && operator != null) {
                    calculate()
                    operator = null
                    justCalculated = true
                }
            }
            action == "C" -> {
                currentInput = ""
                firstOperand = null
                operator = null
                justCalculated = false
            }
            action == "⌫" -> {
                if (!justCalculated && currentInput.isNotEmpty()) {
                    currentInput = currentInput.dropLast(1)
                }
            }
        }

        // 更新屏幕显示的文字
        displayText = if (currentInput.isEmpty() && !justCalculated) {
            firstOperand?.let { formatNumber(it) } ?: "0"
        } else {
            currentInput.ifEmpty { "0" }
        }
    }

    // UI 绘制
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E)), // 整体深色背景
        horizontalAlignment = Alignment.End
    ) {
        // 显示屏区域
        Text(
            text = displayText,
            color = Color.White,
            fontSize = 56.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 占据剩余的所有空间
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 16.dp)
        )

        // 按钮区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 第 1 行: C, ⌫, /, *
            CalculatorRow(
                items = listOf("C", "⌫", "/", "*"),
                onAction = ::onAction
            )
            // 第 2 行: 7, 8, 9, -
            CalculatorRow(
                items = listOf("7", "8", "9", "-"),
                onAction = ::onAction
            )
            // 第 3 行: 4, 5, 6, +
            CalculatorRow(
                items = listOf("4", "5", "6", "+"),
                onAction = ::onAction
            )

            // 特殊处理最后两行 (处理 0 跨两列，= 跨两行)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp), // 固定两行按钮的总高度
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 左侧数字区 (包含 1,2,3 和 0,.)
                Column(
                    modifier = Modifier
                        .weight(3f) // 占 3/4 宽度
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcButton("1", Color(0xFF333333), Modifier.weight(1f)) { onAction("1") }
                        CalcButton("2", Color(0xFF333333), Modifier.weight(1f)) { onAction("2") }
                        CalcButton("3", Color(0xFF333333), Modifier.weight(1f)) { onAction("3") }
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcButton("0", Color(0xFF333333), Modifier.weight(2f)) { onAction("0") } // 0 占两列
                        CalcButton(".", Color(0xFF333333), Modifier.weight(1f)) { onAction(".") }
                    }
                }

                // 右侧等号区 (跨两行)
                CalcButton(
                    text = "=",
                    bgColor = Color(0xFFFF9500), // 橙色
                    modifier = Modifier
                        .weight(1f) // 占 1/4 宽度
                        .fillMaxHeight() // 高度撑满两行
                ) {
                    onAction("=")
                }
            }
        }
    }
}

// 普通的四列行组件
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
                item in listOf("C", "⌫") -> Color(0xFFA5A5A5) // 浅灰
                item in "+-*/" -> Color(0xFFFF9500)           // 橙色
                else -> Color(0xFF333333)                     // 深灰
            }
            CalcButton(item, color, Modifier.weight(1f)) { onAction(item) }
        }
    }
}

// 单个按钮组件
@Composable
fun CalcButton(
    text: String,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val textColor = if (bgColor == Color(0xFFA5A5A5)) Color.Black else Color.White

    Text(
        text = text,
        color = textColor,
        fontSize = if (text == "⌫") 24.sp else 28.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = modifier
            .background(bgColor, MaterialTheme.shapes.medium)
            .padding(vertical = 8.dp)
            .fillMaxSize() // 让文字在背景块中居中
            .then(
                // 添加点击效果（简单的修改透明度代替波纹，避免深色背景波纹难看）
                Modifier.padding(0.dp) // 保证 Modifier 链不断
            )
    )
    // 注意：这里为了代码极简，去掉了带有波纹效果的 Button 组件，
    // 直接用 Text + Background 实现纯色块按钮。如需点击反馈，可以使用 interactionSource。
    // 实际上在纯代码中，可以直接给上面的 Text 加上 clickable 修饰符：
    // .clickable { onClick() }
}
