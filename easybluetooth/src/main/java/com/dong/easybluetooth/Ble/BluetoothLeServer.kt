package com.dong.easybluetooth.Ble

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/6 08:51
 * BLE服务端对象，作为服务端时使用
 */
class BluetoothLeServer(
    private val context: Context
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Job()

    private val gattCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            // 保存一下最近的一次请求，然后再回调给实际调用者
            lastRequest = CharacteristicRequest(
                device,
                requestId,
                characteristic,
                offset
            )
            listener?.onCharacteristicReadRequest(lastRequest!!)
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            // 保存一下最近的一次请求，然后再回调给实际调用者
            val writeRequest = CharacteristicWriteRequest(
                device, requestId, characteristic, offset, preparedWrite, responseNeeded, value
            )
            lastRequest = writeRequest
            listener?.onCharacteristicWriteRequest(writeRequest)
        }

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            val isSuccess = newState == BluetoothProfile.STATE_CONNECTED
            listener?.onDeviceConnectionChange(device, status, isSuccess)
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            val isSuccess = status == 0
            listener?.onServiceAdded(service, isSuccess)
        }
    }

    // 最近的一次请求
    var lastRequest: CharacteristicRequest? = null
        private set

    var listener: BleServerListener? = null

    var gattServer: BluetoothGattServer? = null
        private set

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    var isAdving = false
        private set

    /**
     * 开启BLE服务端
     * @return true表示服务端成功开启/false表示开启失败
     */
    fun start(): Boolean {
        gattServer = bluetoothManager.openGattServer(context, gattCallback)
        gattServer?.clearServices()
        return gattServer != null
    }

    /**
     * 开启广播才能被BLE扫描模式搜索到该设备
     * @param advertiseSettings 广播设置
     * @param advertiseData 广播内容
     * @param scanResponse 广播被扫描后回复的内容
     * @param callback 回调
     */
    fun startAdv(
        advertiseSettings: AdvertiseSettings,
        advertiseData: AdvertiseData,
        scanResponse: AdvertiseData? = null,
        callback: AdvertiseCallback
    ) {
        bluetoothAdapter.bluetoothLeAdvertiser.startAdvertising(
            advertiseSettings,
            advertiseData,
            callback
        )
        isAdving = true
    }

    /**
     * 关闭广播，可能导致设备断开连接
     * @param callback 开启广播的时候设置的回调
     */
    fun stopAdv(callback: AdvertiseCallback) {
        bluetoothAdapter.bluetoothLeAdvertiser.stopAdvertising(callback)
        isAdving = false
    }

    /**
     * 关闭BLE服务端
     */
    fun stop() {
        gattServer?.close()
        gattServer = null
    }

    fun isRunning(): Boolean {
        return gattServer != null
    }

    /**
     * 回复请求
     * @param response 回复对象
     * @return true表示回复成功，false表示回复失败
     */
    fun sendResponse(response: BleServerResponse): Boolean {
        val result = gattServer?.sendResponse(
            response.request.device,
            response.request.requestId, response.status, response.offset, response.data
        ) ?: false
        // 如果回复成功的话，那么清除这次请求
        if (result) lastRequest = null
        return result
    }

    /**
     * 下一次addService必须要等上一次addService回调了onServiceAdded()
     * 之后才能再调用
     * @param uuid 服务uuid
     */
    fun addService(uuid: UUID) {
        val service = BluetoothGattService(uuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        addService(service)
    }

    fun addService(service: BluetoothGattService) {
        // 防止重复添加相同UUID的服务
        val existService = gattServer?.getService(service.uuid)
        if (existService != null) {
            listener?.onServiceAdded(service, false)
        } else {
            gattServer?.addService(service)
        }

    }

    /**
     * 查询当前服务端的所有服务
     * @return 可能为null
     */
    fun getServiceList(): List<BluetoothGattService>? {
        return gattServer?.services
    }

    /**
     * 向已存在的服务添加特性
     * @param serviceUUID 服务的UUID
     * @param characteristic 要添加的特性
     * @return true表示添加成功，false表示失败
     */
    fun addCharacteristicToService(
        serviceUUID: UUID,
        characteristic: BluetoothGattCharacteristic
    ): Boolean {
        val service = gattServer?.getService(serviceUUID) ?: return false
        gattServer?.removeService(service)
        service.addCharacteristic(characteristic)
        gattServer?.addService(service)
        return true
    }

    /**
     * 服务端主动通知客户端特性内容有变化
     * @param characteristic 内容变化的特性
     * @param device 客户端设备
     * @param confirm 默认为false
     */
    fun notifyCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        device: BluetoothDevice,
        confirm: Boolean = false
    ): Boolean {
        return gattServer?.notifyCharacteristicChanged(device, characteristic, confirm) ?: false
    }

    fun getService(uuid: UUID): BluetoothGattService? {
        return gattServer?.getService(uuid)
    }

    /**
     * 创建特性
     * @param characteristicUUID 特性UUID
     * @param readable 特性是否可读，默认否
     * @param writable 特性是否可写，默认否
     * @param writeNoResponse 特性是否支持不用回复的写入，默认否
     * @param notify 特性是否可通知，默认为否
     */
    fun buildCharacteristic(
        characteristicUUID: UUID,
        readable: Boolean = false,
        writable: Boolean = false,
        writeNoResponse: Boolean = false,
        notify: Boolean = false
    ): BluetoothGattCharacteristic {

        var permission = 0x00
        if (readable) permission = permission or BluetoothGattCharacteristic.PERMISSION_READ
        if (writable) permission = permission or BluetoothGattCharacteristic.PERMISSION_WRITE

        var property = 0x00
        if (readable) property = property or BluetoothGattCharacteristic.PROPERTY_READ
        if (writable) property = property or BluetoothGattCharacteristic.PROPERTY_WRITE
        if (writeNoResponse) property =
            property or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
        if (notify) property = property or BluetoothGattCharacteristic.PROPERTY_NOTIFY

        return BluetoothGattCharacteristic(characteristicUUID, property, permission)
    }

    fun getConnectedDevices(): ArrayList<BluetoothDevice> {
        val list = ArrayList<BluetoothDevice>()
        list.addAll(bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER))
        return list
    }
}