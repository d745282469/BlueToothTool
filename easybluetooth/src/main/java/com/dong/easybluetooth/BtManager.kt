package com.dong.easybluetooth

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/2 08:23
 */
object BtManager : CoroutineScope {
    private val tag = this::class.java.simpleName

    // 用于提供context
    private var application: Application? = null

    // 广播接收器
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothDevice.ACTION_FOUND == intent?.action) {
                // 发现设备，这里的设备包括经典蓝牙和BLE设备
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) addDevice(device)
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED == intent?.action) {
                // 蓝牙状态改变
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                    BluetoothAdapter.STATE_ON -> Log.d(tag, "蓝牙状态：已开启")
                    BluetoothAdapter.STATE_TURNING_ON -> Log.d(tag, "蓝牙状态：正在开启")
                    BluetoothAdapter.STATE_OFF -> Log.d(tag, "蓝牙状态：已关闭")
                    BluetoothAdapter.STATE_TURNING_OFF -> Log.d(tag, "蓝牙状态：正在关闭")
                }
            }
        }
    }

    //BLE设备扫描回调
    private val bleCallBack = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            Log.e(tag, "扫描BLE设备失败，错误码：$errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val device = result?.device
            if (device != null) addDevice(device)
        }
    }

    // 记录扫描到的设备
    private val deviceList = ArrayList<BluetoothDevice>()

    // 蓝牙管理器
    private val blAdapter = BluetoothAdapter.getDefaultAdapter()

    // 是否正在扫描
    var isScanning = false
        private set

    private var scanListener: ScanListener? = null

    // 用于关闭扫描的定时任务
    private var stopJob: Job? = null

    /**
     * 因为需要注册广播，所以在application中初始化一下
     * 如果不需要经典蓝牙的话不调用也不影响
     */
    fun init(application: Application) {
        this.application = application
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        this.application!!.registerReceiver(receiver, filter)
    }

    /**
     * 发现新设备
     * @param device 扫描到的设备
     */
    private fun addDevice(device: BluetoothDevice) {
        // 和现在的设备列表对比mac地址，找出不重复的设备
        var isExisted = false
        for (existDevice in deviceList) {
            if (device.address == existDevice.address) {
                isExisted = true
                break
            }
        }
        if (!isExisted) {
            deviceList.add(0, device)
            scanListener?.foundDevice(device)
            Log.d(
                tag,
                "新发现设备：${device.name}，MAC：${device.address}，类型：${device.bluetoothClass.majorDeviceClass}"
            )
        }
    }

    /**
     * 扫描设备
     * @param listener 扫描监听
     * @param timeout 扫描超时时间，最低默认为10秒
     * @param type 扫描类型
     * @see BluetoothType
     */
    fun scanDevice(
        listener: ScanListener? = null,
        timeout: Int = 10,
        type: BluetoothType = BluetoothType.CLASSICAL
    ) {
        stopJob?.cancel()
        val realTimeout = when {
            timeout < 10 -> 10 * 1000
            timeout > 60 -> 60 * 1000
            else -> timeout * 1000
        }
        stopJob = launch(Dispatchers.Default) {
            delay(realTimeout.toLong())
            stopScan()
            stopJob = null
        }
        stopScan()
        deviceList.clear()
        isScanning = true
        this.scanListener = listener
        when (type) {
            BluetoothType.CLASSICAL -> scanClassicalDevice()
            BluetoothType.LOW_ENG -> scanBleDevice()
        }
    }

    /**
     * 扫描经典蓝牙设备，通过广播的方式获取到扫描结果
     */
    private fun scanClassicalDevice() {
        blAdapter.startDiscovery()
    }

    /**
     * 扫描BLE设备
     */
    private fun scanBleDevice() {
        blAdapter.bluetoothLeScanner.startScan(bleCallBack)
    }

    /**
     * 结束设备扫描
     */
    fun stopScan() {
        blAdapter.cancelDiscovery()
        blAdapter.bluetoothLeScanner.stopScan(bleCallBack)
        isScanning = false
        scanListener = null
        stopJob?.cancel()
        stopJob = null
    }

    /**
     * 查找当前已绑定的设备，经典蓝牙
     */
    fun getBondedDevice(): ArrayList<BluetoothDevice> {
        val list = ArrayList<BluetoothDevice>()
        list.addAll(blAdapter.bondedDevices)
        return list
    }

    /**
     * 蓝牙是否打开
     */
    fun isOn(): Boolean {
        return blAdapter.isEnabled
    }

    /**
     * 获取蓝牙名称
     */
    fun getName(): String {
        return blAdapter.name
    }

    /**
     * 当前设备是否支持蓝牙
     */
    fun isSupport(): Boolean {
        return blAdapter != null
    }

    enum class BluetoothType {
        CLASSICAL,// 经典蓝牙
        LOW_ENG,// 低功耗蓝牙,BLE
    }

    override val coroutineContext: CoroutineContext
        get() = Job()
}