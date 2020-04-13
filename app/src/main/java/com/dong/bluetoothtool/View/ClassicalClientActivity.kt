package com.dong.bluetoothtool.View

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dong.bluetoothtool.R
import com.dong.easybluetooth.Classical.BluetoothClassicalClient
import com.dong.easybluetooth.Classical.BluetoothMessageListener
import com.dong.easybluetooth.Classical.ClassicalClientListener
import kotlinx.android.synthetic.main.activity_classical_client.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class ClassicalClientActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var serverDevice: BluetoothDevice

    private var client: BluetoothClassicalClient? = null

    companion object {
        @JvmStatic
        fun start(activity: Activity, device: BluetoothDevice) {
            val intent = Intent(activity, ClassicalClientActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("device", device)
            intent.putExtra("device", bundle)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classical_client)
//        BLManager.clear()

        serverDevice = intent.getBundleExtra("device")!!.getParcelable("device")!!

        this.classical_client_blackboard.addMsg("准备连接服务端${serverDevice.name}，MAC：${serverDevice.address}")

        this.classical_client_btn_switch.setOnClickListener {
            if (client == null) {
                connectServer()
            } else {
                if (client!!.isConnected()) {
                    // 关闭连接
                    client!!.disConnect()
                    this.classical_client_btn_switch.text = "连接服务端"
                } else {
                    connectServer()
                }
            }
        }

        this.classical_client_btn_send.setOnClickListener {
            if (client == null){
                this.classical_client_blackboard.addErrMsg("尚未与服务端建立连接")
                return@setOnClickListener
            }

            if (!client!!.isConnected()){
                this.classical_client_blackboard.addErrMsg("尚未与服务端建立连接")
                return@setOnClickListener
            }

            val data = this.classical_client_et_data.text.toString()
            if (data.isEmpty()){
                this.classical_client_blackboard.addErrMsg("请输入要发送的数据")
                return@setOnClickListener
            }

            client!!.send(data.toByteArray())
            this.classical_client_blackboard.addMsg("发送：$data")
        }
    }

    private fun connectServer() {
        val uuid = this.classical_client_et_uuid.text.toString()
        if (uuid.isEmpty()) {
            this.classical_client_blackboard.addErrMsg("请输入UUID")
            return
        }

        this.classical_client_btn_switch.text = "断开与服务端的连接"

        this.classical_client_blackboard.addMsg("正在连接服务端...")
        client = BluetoothClassicalClient(
            serverDevice,
            UUID.fromString(uuid)
        )
        client!!.connectServer()
        client!!.clientListener = object : ClassicalClientListener() {
            override fun onConnected() {
                this@ClassicalClientActivity.classical_client_blackboard.addMsg("连接服务端成功")
                // 连接成功后设置消息监听器
                client!!.messageListener = object : BluetoothMessageListener(){
                    override fun onFail(errorMsg: String) {
                        this@ClassicalClientActivity.classical_client_blackboard.addErrMsg(errorMsg)
                        this@ClassicalClientActivity.classical_client_btn_switch.text = "连接服务端"
                    }

                    override fun onReceiveMsg(msg: ByteArray) {
                        this@ClassicalClientActivity.classical_client_blackboard.addWarnMsg("接收：${String(msg)}")
                    }
                }
            }

            override fun onFail(errorMsg: String) {
                this@ClassicalClientActivity.classical_client_blackboard.addErrMsg(errorMsg)
                this@ClassicalClientActivity.classical_client_btn_switch.text = "连接服务端"
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Job()
}
