package com.example.myapplication

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : ComponentActivity() {

    // 多播锁
    private var multicastLock: WifiManager.MulticastLock? = null

    // 用于管理 UDP 线程的协程作用域
    private val udpScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取多播锁
        acquireMulticastLock()

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UdpDiscoveryScreen(
                        modifier = Modifier.padding(innerPadding),
                        onStartDiscovery = { port, message ->
                            startUdpDiscovery(port, message)
                        }
                    )
                }
            }
        }
    }

    /**
     * 获取多播锁，确保屏幕关闭或应用在后台时仍能接收 UDP 广播/组播包
     */
    private fun acquireMulticastLock() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        wifiManager?.let {
            multicastLock = it.createMulticastLock("G-DLL_Discovery_Lock").apply {
                acquire()
            }
        }
    }

    /**
     * 启动 UDP 发现过程
     * @param port 监听端口，例如 8888
     * @param broadcastMessage 可选，需要发送的广播消息
     */
    private fun startUdpDiscovery(port: Int, broadcastMessage: String?) {
        udpScope.launch {
            var socket: DatagramSocket? = null
            try {
                // 创建 DatagramSocket 并绑定到指定端口
                socket = DatagramSocket(port).apply {
                    // 允许发送广播
                    setBroadcast(true)
                    // 可选：设置接收超时，避免永久阻塞
                    // soTimeout = 5000
                }

                // 如果需要发送广播消息
                broadcastMessage?.takeIf { it.isNotBlank() }?.let { msg ->
                    val broadcastAddr = InetAddress.getByName("255.255.255.255")
                    val sendData = msg.toByteArray()
                    val sendPacket = DatagramPacket(sendData, sendData.size, broadcastAddr, port)
                    socket.send(sendPacket)
                    showToast("已发送广播: $msg")
                }

                // 持续接收 UDP 数据包
                val buffer = ByteArray(1024)
                while (!socket.isClosed) {
                    val receivePacket = DatagramPacket(buffer, buffer.size)
                    socket.receive(receivePacket)  // 阻塞，直到收到数据

                    val receivedMessage = String(receivePacket.data, 0, receivePacket.length)
                    val senderAddress = receivePacket.address.hostAddress

                    // 处理接收到的消息（例如更新 UI）
                    showToast("收到来自 $senderAddress 的消息: $receivedMessage")

                    // 这里可以解析你的 G-DLL 发现协议
                    // 如果收到期望的响应，可以 break 退出循环
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("UDP 错误: ${e.message}")
            } finally {
                socket?.close()
            }
        }
    }

    /**
     * 在 UI 线程显示 Toast
     */
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放多播锁
        multicastLock?.takeIf { it.isHeld }?.release()
        // 取消所有协程，关闭 socket
        udpScope.coroutineContext.cancelChildren()
    }
}

/**
 * 简单的 UI 界面，包含一个按钮来启动 UDP 发现
 */
@Composable
fun UdpDiscoveryScreen(
    modifier: Modifier = Modifier,
    onStartDiscovery: (port: Int, broadcastMessage: String?) -> Unit
) {
    val context = LocalContext.current
    var isDiscovering by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isDiscovering) "正在发现设备..." else "G-DLL 发现协议",
            fontSize = 24.sp
        )
        Button(
            onClick = {
                isDiscovering = true
                // 示例：监听 8888 端口，并发送一条广播消息
                onStartDiscovery(8888, "G-DLL_DISCOVER")
            },
            enabled = !isDiscovering
        ) {
            Text("开始发现")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UdpDiscoveryScreenPreview() {
    MyApplicationTheme {
        UdpDiscoveryScreen { _, _ -> }
    }
}
