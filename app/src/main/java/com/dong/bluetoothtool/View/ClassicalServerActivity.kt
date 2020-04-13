package com.dong.bluetoothtool.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dong.bluetoothtool.R
import com.dong.easybluetooth.Classical.BluetoothClassicalServer
import com.dong.easybluetooth.Classical.BluetoothMessageListener
import com.dong.easybluetooth.Classical.ClassicalServerListener
import kotlinx.android.synthetic.main.activity_classical_server.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class ClassicalServerActivity : AppCompatActivity(), CoroutineScope {
    private val tag = this::class.java.simpleName

    private var server: BluetoothClassicalServer? = null
    private var client: BluetoothClassicalServer.Client? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classical_server)

        this.classical_server_btn_uuid.setOnClickListener {
            this.classical_server_et_uuid.setText(UUID.randomUUID().toString())
        }

        this.classical_server_btn_switch.setOnClickListener {
            if (server == null) {
                startServer()
            } else {
                if (server!!.isRunning) {
                    // 关闭服务端
                    server!!.stop()
                    this.classical_server_btn_switch.text = "开启服务端"
                } else {
                    // 开启服务端
                    server!!.start()
                    this.classical_server_btn_switch.text = "关闭服务端"
                }
            }
        }

        this.classical_server_btn_send.setOnClickListener {
            if (client == null) {
                this.classical_server_blackboard.addErrMsg("没有目标客户端")
                return@setOnClickListener
            }

            if (!client!!.isConnected()) {
                this.classical_server_blackboard.addErrMsg("${client!!.remoteDevice.name}尚未连接")
                return@setOnClickListener
            }

            val data = this.classical_server_et_data.text.toString()
            if (data.isEmpty()) {
                this.classical_server_blackboard.addErrMsg("请填写要发送的内容")
                return@setOnClickListener
            }

            client!!.send(data.toByteArray())
            this.classical_server_blackboard.addMsg("发送：$data")
        }
    }

    private fun startServer() {
        val uuid = this.classical_server_et_uuid.text.toString()
        if (uuid.isEmpty()) {
            this.classical_server_blackboard.addErrMsg("请输入UUID")
            return
        }
        val name = this.classical_server_et_name.text.toString()

        this.classical_server_btn_switch.text = "关闭服务端"
        server = BluetoothClassicalServer(name, UUID.fromString(uuid))
        server!!.start()
        this.classical_server_blackboard.addMsg("已启动服务端")
        server!!.listener = object : ClassicalServerListener() {
            override fun onDeviceConnected(client: BluetoothClassicalServer.Client) {
                this@ClassicalServerActivity.client = client
                this@ClassicalServerActivity.classical_server_blackboard.addMsg(
                    "${client.remoteDevice.name}已连接到服务端，MAC：${client.remoteDevice.address}"
                )
                this@ClassicalServerActivity.classical_server_blackboard.addMsg(
                    "当前目标设备已设为${client.remoteDevice.name}"
                )

                // 设置消息监听器
                client.messageListener = object : BluetoothMessageListener() {
                    override fun onFail(errorMsg: String) {
                        this@ClassicalServerActivity.classical_server_blackboard.addErrMsg(errorMsg)
                    }

                    override fun onReceiveMsg(msg: ByteArray) {
                        this@ClassicalServerActivity.classical_server_blackboard.addWarnMsg(
                            "接收：${String(
                                msg
                            )}"
                        )
                    }
                }
            }

            override fun onFail(errorMsg: String) {
                this@ClassicalServerActivity.classical_server_blackboard.addErrMsg(errorMsg)
                this@ClassicalServerActivity.classical_server_btn_switch.text = "开启服务端"
            }
        }

        this.classical_server_btn_disconnect.setOnClickListener {
            if (client == null) {
                this.classical_server_blackboard.addErrMsg("尚未与客户端建立连接")
            } else {
                client!!.disConnect()
                this.classical_server_blackboard.addMsg("已断开与${client!!.remoteDevice.name}的连接")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override val coroutineContext: CoroutineContext
        get() = Job()
}
