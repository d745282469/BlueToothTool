package com.dong.easybluetooth

import android.bluetooth.BluetoothDevice

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/3 09:27
 */
interface ScanListener {
    fun foundDevice(device: BluetoothDevice)
}