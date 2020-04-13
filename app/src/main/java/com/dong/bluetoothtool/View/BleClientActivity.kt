package com.dong.bluetoothtool.View

import android.app.Activity
import android.bluetooth.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dong.bluetoothtool.R
import com.dong.bluetoothtool.ServiceListAdapter
import com.dong.bluetoothtool.Util.DialogUtil
import com.dong.easybluetooth.Ble.BluetoothLeClient
import kotlinx.android.synthetic.main.activity_ble_client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class BleClientActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var serverDevice: BluetoothDevice
    private lateinit var client: BluetoothLeClient
    private val context = this

    companion object {
        @JvmStatic
        fun start(activity: Activity, device: BluetoothDevice) {
            val intent = Intent(activity, BleClientActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("device", device)
            intent.putExtra("device", bundle)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_client)

        serverDevice = intent.getBundleExtra("device")!!.getParcelable("device")!!
        client = BluetoothLeClient(serverDevice, this)

        val callback = object : BluetoothGattCallback() {
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                // 这里是非主线程啊！！！
                if (status == 0) {
                    val list = gatt!!.services
                    if (list.isNotEmpty()) {
                        // 打印
                        val builder = StringBuilder()
                        for (service in list) {
                            builder.clear()
                            builder.append("服务：${service.uuid}\r\n特性：")
                            for (chara in service.characteristics) {
                                builder.append("${chara.uuid}\r\n描述：")
                                for (des in chara.descriptors) {
                                    builder.append(des.uuid.toString())
                                }
                            }
                            Log.d("dong", builder.toString())
                        }

                        launch(Dispatchers.Main) {
                            DialogUtil.showServiceDialog(
                                list,
                                context,
                                object : ServiceListAdapter.OnChildClickListener {
                                    override fun onChildClick(
                                        parentPosition: Int,
                                        childPosition: Int
                                    ) {
                                        Log.d("dong", "执行到这里")
                                        this@BleClientActivity.et_characteristic_uuid.setText(list[parentPosition].characteristics[childPosition].uuid.toString())
                                        this@BleClientActivity.et_server_uuid.setText(list[parentPosition].uuid.toString())
                                        DialogUtil.dismiss()
                                    }
                                })
                        }
                    } else {
                        this@BleClientActivity.blackboard.addWarnMsg("当前服务端尚未有服务")
                    }

                } else {
                    this@BleClientActivity.blackboard.addErrMsg("发现服务失败，错误码：$status")
                }
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    this@BleClientActivity.blackboard.addMsg("已连接到服务端")
                    launch(Dispatchers.Main) {
                        this@BleClientActivity.btn_connect_server.text = "断开与服务端的连接"
                    }
                } else {
                    this@BleClientActivity.blackboard.addWarnMsg("无法与服务端建立连接")
                    launch(Dispatchers.Main) {
                        this@BleClientActivity.btn_connect_server.text = "连接服务端"
                    }
                }
                launch(Dispatchers.Main) {
                    this@BleClientActivity.btn_connect_server.isEnabled = true
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                val data = characteristic?.value
                this@BleClientActivity.blackboard.addMsg("读特性操作结果：$status，内容：${String(data!!)}")
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                val data = characteristic?.value
                this@BleClientActivity.blackboard.addMsg("写特性操作结果：$status，内容：${String(data!!)}")
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                val data = characteristic?.value
                this@BleClientActivity.blackboard.addMsg("特性：${characteristic?.uuid}的内容有变化，${data?.contentToString()}")
            }
        }

        this.btn_connect_server.setOnClickListener {
            if (client.isConnected) {
                // 断开连接
                client.disconnectServer()
//                this.btn_connect_server.text = "连接服务端"
            } else {
                // 连接服务端
                if (client.connectServer(callback)) {
                    this.blackboard.addMsg("正在尝试与服务端建立连接...")
                    this.btn_connect_server.isEnabled = false
                } else {
                    this.blackboard.addErrMsg("连接服务端失败")
                }
            }
        }

        this.btn_check_server.setOnClickListener {
            if (!client.isConnected) {
                this.blackboard.addWarnMsg("尚未与服务端建立连接")
                return@setOnClickListener
            }
            this.blackboard.addMsg("请求查询服务列表")
            client.checkService()
        }

        this.btn_send.setOnClickListener {
            if (!client.isConnected) {
                this.blackboard.addWarnMsg("尚未与服务端建立连接")
                return@setOnClickListener
            }

            val serviceUUID = this.et_server_uuid.text.toString()
            if (serviceUUID.isEmpty()) {
                this.blackboard.addErrMsg("请输入服务UUID")
                return@setOnClickListener
            }

            val charaUUID = this.et_characteristic_uuid.text.toString()
            if (charaUUID.isEmpty()) {
                this.blackboard.addErrMsg("请输入特性UUID")
                return@setOnClickListener
            }

            val service = client.getServiceByUUID(UUID.fromString(serviceUUID))
            if (service == null) {
                this.blackboard.addErrMsg("没找到对应的服务")
                return@setOnClickListener
            }

            val chara = service.getCharacteristic(UUID.fromString(charaUUID))
            if (chara == null) {
                this.blackboard.addErrMsg("在服务中没找到对应的特性")
                return@setOnClickListener
            }

            if (chara.properties and BluetoothGattCharacteristic.PROPERTY_WRITE == 0
                && (chara.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0
            ) {
                this.blackboard.addErrMsg("该特性不允许写入")
                return@setOnClickListener
            }

            val data = this.et_data.text.toString()
            if (data.isEmpty()) {
                this.blackboard.addErrMsg("请输入要写入的内容")
                return@setOnClickListener
            }

            this.blackboard.addMsg("正在发送写特性请求...")

            if (!client.writeCharacteristic(
                    chara,
                    data.toByteArray()
                )
            ) this.blackboard.addErrMsg("写入特性失败")
        }

        this.btn_get_chara_data.setOnClickListener {
            if (!client.isConnected) {
                this.blackboard.addWarnMsg("尚未与服务端建立连接")
                return@setOnClickListener
            }

            val serviceUUID = this.et_server_uuid.text.toString()
            if (serviceUUID.isEmpty()) {
                this.blackboard.addErrMsg("请输入服务UUID")
                return@setOnClickListener
            }

            val charaUUID = this.et_characteristic_uuid.text.toString()
            if (charaUUID.isEmpty()) {
                this.blackboard.addErrMsg("请输入特性UUID")
                return@setOnClickListener
            }

            val service = client.getServiceByUUID(UUID.fromString(serviceUUID))
            if (service == null) {
                this.blackboard.addErrMsg("没找到对应的服务")
                return@setOnClickListener
            }

            val chara = service.getCharacteristic(UUID.fromString(charaUUID))
            if (chara == null) {
                this.blackboard.addErrMsg("在服务中没找到对应的特性")
                return@setOnClickListener
            }

            if (chara.properties and BluetoothGattCharacteristic.PROPERTY_READ == 0) {
                this.blackboard.addErrMsg("该特性不允许读取")
                return@setOnClickListener
            }

            this.blackboard.addMsg("正在发送读特性请求...")

            client.readCharacteristic(chara)
        }

        this.btn_reg_notify.setOnClickListener {
            if (!client.isConnected) {
                this.blackboard.addWarnMsg("尚未与服务端建立连接")
                return@setOnClickListener
            }

            val serviceUUID = this.et_server_uuid.text.toString()
            if (serviceUUID.isEmpty()) {
                this.blackboard.addErrMsg("请输入服务UUID")
                return@setOnClickListener
            }

            val charaUUID = this.et_characteristic_uuid.text.toString()
            if (charaUUID.isEmpty()) {
                this.blackboard.addErrMsg("请输入特性UUID")
                return@setOnClickListener
            }

            val service = client.getServiceByUUID(UUID.fromString(serviceUUID))
            if (service == null) {
                this.blackboard.addErrMsg("没找到对应的服务")
                return@setOnClickListener
            }

            val chara = service.getCharacteristic(UUID.fromString(charaUUID))
            if (chara == null) {
                this.blackboard.addErrMsg("在服务中没找到对应的特性")
                return@setOnClickListener
            }

//            if (chara.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY == 0) {
//                this.blackboard.addErrMsg("该特性不支持通知")
//                return@setOnClickListener
//            }

            if (client.server?.setCharacteristicNotification(chara, true) == true) {
                this.blackboard.addMsg("注册特性：${chara.uuid}通知成功")
            } else {
                this.blackboard.addErrMsg("注册特性：${chara.uuid}通知失败")
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Job()
}
