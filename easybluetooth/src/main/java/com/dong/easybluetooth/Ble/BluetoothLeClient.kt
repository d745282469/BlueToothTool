package com.dong.easybluetooth.Ble

import android.bluetooth.*
import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/6 08:51
 * BLE客户端对象，作为客户端时实例化使用
 * 用于和BLE服务端进行交互
 */
class BluetoothLeClient(
    val serverDevice: BluetoothDevice,
    private val context: Context
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Job()

    var server: BluetoothGatt? = null
        private set

    var isConnected = false
        private set

    private var callback: BluetoothGattCallback = object : BluetoothGattCallback() {}

    private val callbackPoxy: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            callback.onReadRemoteRssi(gatt, rssi, status)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            callback.onCharacteristicRead(gatt, characteristic, status)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            callback.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            callback.onServicesDiscovered(gatt, status)
        }

        override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                callback.onPhyUpdate(gatt, txPhy, rxPhy, status)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            callback.onMtuChanged(gatt, mtu, status)
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            callback.onReliableWriteCompleted(gatt, status)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            callback.onDescriptorWrite(gatt, descriptor, status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            callback.onCharacteristicChanged(gatt, characteristic)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            callback.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                callback.onPhyRead(gatt, txPhy, rxPhy, status)
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState != BluetoothProfile.STATE_CONNECTED) isConnected = false
            callback.onConnectionStateChange(gatt, status, newState)
        }
    }

    /**
     * 连接服务端
     * @return false表示连接失败，可能当前设备不支持BLE，不是服务端不支持
     */
    fun connectServer(gattCallback: BluetoothGattCallback? = null): Boolean {
        if (gattCallback != null) callback = gattCallback
        server = serverDevice.connectGatt(context, false, callbackPoxy)
        server?.discoverServices()
        if (server != null) isConnected = true
        return server != null
    }

    /**
     * 查询服务端支持的服务列表，结果在回调onServicesDiscovered
     */
    fun checkService() {
        if (server == null) {
            callback.onServicesDiscovered(server, -1)
        } else {
            server?.discoverServices()
        }
    }

    /**
     * 断开和服务端的连接
     */
    fun disconnectServer() {
        server?.disconnect()
        isConnected = false
    }

    /**
     * 根据uuid获取对应的服务
     * @param uuid 服务的uuid
     */
    fun getServiceByUUID(uuid: UUID): BluetoothGattService? {
        return server?.getService(uuid)
    }

    /**
     * 发送写特性请求
     * @param characteristic 要写入的特性
     * @param data 要写入的内容
     * @return true表示发送请求成功，false表示发送请求失败
     */
    fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray
    ): Boolean {
        characteristic.value = data
        return server?.writeCharacteristic(characteristic) ?: false
    }

    /**
     * 发送读特性请求
     * @param characteristic 要读取的特性
     * @return true表示发送请求成功，false表示发送请求失败
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean {
        return server?.readCharacteristic(characteristic) ?: false
    }

    /**
     * 注册特性通知
     * @param characteristic 希望接收通知的特性
     * @return true表示注册成功，false表示注册失败
     */
    fun regCharacteristicNotify(characteristic: BluetoothGattCharacteristic): Boolean {
        return server?.setCharacteristicNotification(characteristic, true) ?: false
    }
}
