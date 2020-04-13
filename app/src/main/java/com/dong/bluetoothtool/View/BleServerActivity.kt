package com.dong.bluetoothtool.View

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.dong.bluetoothtool.R
import com.dong.bluetoothtool.Util.DialogUtil
import com.dong.easybluetooth.Ble.*
import kotlinx.android.synthetic.main.activity_ble_server.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class BleServerActivity : AppCompatActivity() {

    private lateinit var server: BluetoothLeServer
    private var currentDevice: BluetoothDevice? = null

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            this@BleServerActivity.blackboard.addErrMsg("开启广播失败，错误码：$errorCode")
            this@BleServerActivity.btn_start_adv.text = "开启设备广播"
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            this@BleServerActivity.blackboard.addMsg("开启广播成功")
            this@BleServerActivity.btn_start_adv.text = "关闭设备广播"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_server)
        server = BluetoothLeServer(this)
        server.listener = object : BleServerListener() {
            override fun onDeviceConnectionChange(
                device: BluetoothDevice?,
                status: Int,
                isConnected: Boolean
            ) {
                val deviceName = if (device?.name != null && device.name.isNotEmpty()) {
                    device.name
                } else {
                    device?.address ?: "Null"
                }

                if (isConnected) {
                    this@BleServerActivity.blackboard.addMsg("${deviceName}已连接到服务端")
                } else {
                    this@BleServerActivity.blackboard.addWarnMsg("${deviceName}已断开与服务端的连接")
                    if (currentDevice?.address == device?.address) currentDevice = null
                }
            }

            override fun onCharacteristicReadRequest(request: CharacteristicRequest) {
                val device = request.device
                val deviceName = if (device?.name != null && device.name.isNotEmpty()) {
                    device.name
                } else {
                    device?.address ?: "Null"
                }
                this@BleServerActivity.blackboard.addMsg(
                    "${deviceName}请求读取特性：${request.characteristic?.uuid}的内容，" +
                            "该请求需要及时回复，否则设备将会断开连接"
                )
            }

            override fun onCharacteristicWriteRequest(request: CharacteristicWriteRequest) {
                val device = request.device
                val deviceName = if (device?.name != null && device.name.isNotEmpty()) {
                    device.name
                } else {
                    device?.address ?: "Null"
                }
                val response = if (request.responseNeeded) {
                    "该请求需要及时回复，否则设备将会断开连接"
                } else {
                    "该请求可不回复"
                }
                this@BleServerActivity.blackboard.addMsg(
                    "${deviceName}请求向特性：${request.characteristic?.uuid}写入内容" +
                            "：${request.value?.contentToString()}，$response"
                )
            }

            override fun onServiceAdded(service: BluetoothGattService?, isSuccess: Boolean) {
                if (isSuccess) {
                    this@BleServerActivity.blackboard.addMsg("添加服务：${service?.uuid}，成功")
                } else {
                    this@BleServerActivity.blackboard.addErrMsg("添加服务：${service?.uuid}，失败")
                }
            }
        }

        this.btn_start_server.setOnClickListener {
            if (server.isRunning()) {
                server.stop()
                this.blackboard.addMsg("已关闭服务端")
                this.btn_start_server.text = "开启服务端"
            } else {
                if (server.start()) {
                    this.blackboard.addMsg("已开启服务端")
                    this.btn_start_server.text = "关闭服务端"
                } else {
                    this.blackboard.addErrMsg("开启服务端失败")
                }
            }
        }

        this.btn_check_server.setOnClickListener {
            val serviceList = server.getServiceList()
            if (serviceList == null || serviceList.isEmpty()) {
                this.blackboard.addWarnMsg("当前服务端尚未有服务")
            } else {
                for (service in serviceList) {
                    this.blackboard.addMsg("服务：${service.uuid}")
                    for (chara in service.characteristics) {
                        this.blackboard.addMsg("-特性：${chara.uuid}")
                    }
                    this.blackboard.addMsg("↑↑↑↑--service end--↑↑↑↑")
                }
            }
        }

        this.btn_add_server.setOnClickListener {
            if (!checkIsRunning()) return@setOnClickListener

            val serviceUUid = this.et_server_uuid.text.toString()
            if (serviceUUid.isEmpty()) {
                this.blackboard.addErrMsg("请输入服务UUID")
                return@setOnClickListener
            }

            server.addService(UUID.fromString(serviceUUid))
        }

        this.btn_start_adv.setOnClickListener {
            if (!checkIsRunning()) return@setOnClickListener

            if (server.isAdving) {
                server.stopAdv(advertiseCallback)
                this.blackboard.addMsg("已关闭广播")
                this.btn_start_adv.text = "开启设备广播"
            } else {
                // 广播设置
                val advertiseSettings = AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //广播模式: 低功耗,平衡,低延迟
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) //发射功率级别: 极低,低,中,高
                    .setConnectable(true) //能否连接,广播分为可连接广播和不可连接广播
                    .build()

                // 广播内容
                val advertiseData = AdvertiseData.Builder()
                    .setIncludeDeviceName(true) //包含蓝牙名称
                    .setIncludeTxPowerLevel(true) //包含发射功率级别
                    .addManufacturerData(1, byteArrayOf(66, 66)) //设备厂商数据，自定义
                    .build()


                // 扫描回复包，可不发
//                val scanResponse = AdvertiseData.Builder()
//                    .addManufacturerData(2, byteArrayOf(66, 66)) //设备厂商数据，自定义
//                    .addServiceUuid(ParcelUuid(uuid)) //服务UUID
//                    .addServiceData(new ParcelUuid (UUID_SERVICE), new byte []{ 2 }) //服务数据，自定义
//                    .build()
                server.startAdv(advertiseSettings,advertiseData,callback = advertiseCallback)
            }
        }

        this.btn_server_uuid_random.setOnClickListener {
            this.et_server_uuid.setText(UUID.randomUUID().toString())
        }

        this.btn_characteristic_uuid_random.setOnClickListener {
            this.et_characteristic_uuid.setText(UUID.randomUUID().toString())
        }

        this.btn_add_characteristic.setOnClickListener {
            if (!checkIsRunning()) return@setOnClickListener

            val serviceUUid = this.et_server_uuid.text.toString()
            if (serviceUUid.isEmpty()) {
                this.blackboard.addErrMsg("请输入服务UUID")
                return@setOnClickListener
            }

            val charaUUID = this.et_characteristic_uuid.text.toString()
            if (charaUUID.isEmpty()) {
                this.blackboard.addErrMsg("请输入特性UUID")
                return@setOnClickListener
            }

            val isWritable = this.cb_characteristic_writable.isChecked
            val isReadable = this.cb_characteristic_readable.isChecked
            val isWriteNoResp = this.cb_characteristic_write_no_response.isChecked
            val isNotify = this.cb_characteristic_notify.isChecked

            val chara = server.buildCharacteristic(
                UUID.fromString(charaUUID), isReadable, isWritable,isWriteNoResp,isNotify
            )
            val b = server.addCharacteristicToService(UUID.fromString(serviceUUid), chara)
            if (b) {
                this.blackboard.addMsg("向服务${serviceUUid}添加特性${charaUUID}成功")
            } else {
                this.blackboard.addErrMsg("向服务${serviceUUid}添加特性${charaUUID}失败")
            }
        }

        this.btn_response.setOnClickListener {
            if (!checkIsRunning()) return@setOnClickListener

            val request = server.lastRequest
            if (request == null) {
                this.blackboard.addErrMsg("没有收到任何客户端请求")
                return@setOnClickListener
            }

            val data = this.et_data.text.toString()
            if (data.isEmpty()) {
                this.blackboard.addErrMsg("请输入要回复的内容")
                return@setOnClickListener
            }


            val resp = BleServerResponse(
                request,
                0,
                data.toByteArray()
            )
            if (server.sendResponse(resp)) {
                this.blackboard.addMsg("回复请求成功")
            } else {
                this.blackboard.addErrMsg("回复请求失败")
            }
        }

        this.btn_notify.setOnClickListener {
            if (!checkIsRunning()) return@setOnClickListener

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

            val data = this.et_data.text.toString()
            if (data.isEmpty()) {
                this.blackboard.addErrMsg("请输入通知内容")
                return@setOnClickListener
            }

            val service = server.getService(UUID.fromString(serviceUUID))
            if (service == null){
                this.blackboard.addErrMsg("没有找到对应的服务")
                return@setOnClickListener
            }

            val chara = service.getCharacteristic(UUID.fromString(charaUUID))
            if (chara == null){
                this.blackboard.addErrMsg("没有找到对应的特性")
                return@setOnClickListener
            }
            chara.value = data.toByteArray()

            if (currentDevice == null){
                this.blackboard.addErrMsg("请先选择目标设备")
                return@setOnClickListener
            }

            val deviceName = if (currentDevice!!.name.isNullOrEmpty()) {
                currentDevice!!.address
            } else {
                currentDevice!!.name
            }

            if (server.notifyCharacteristic(chara, currentDevice!!, true)) {
                this.blackboard.addMsg("发送通知给${deviceName}成功")
            } else {
                this.blackboard.addErrMsg("发送通知给${deviceName}失败")
            }
        }

        this.btn_check_device.setOnClickListener {
            if (!checkIsRunning()) return@setOnClickListener

            val deviceList = server.getConnectedDevices()
            if (deviceList.isEmpty()) {
                this.blackboard.addWarnMsg("当前服务端没有已连接的设备")
            } else {
                DialogUtil.showDeviceDialog(
                    deviceList, this
                ) { _, _, position ->
                    currentDevice = deviceList[position]
                    val deviceName = if (currentDevice!!.name.isNullOrEmpty()) {
                        currentDevice!!.address
                    } else {
                        currentDevice!!.name
                    }
                    this@BleServerActivity.blackboard.addWarnMsg("当前目标设备已设为：$deviceName")
                    DialogUtil.dismiss()
                }
            }
        }
    }

    private fun checkIsRunning(): Boolean {
        if (!server.isRunning()) {
            this.blackboard.addErrMsg("服务端未启动")
        }
        return server.isRunning()
    }
}
