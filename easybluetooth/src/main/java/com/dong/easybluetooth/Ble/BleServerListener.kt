package com.dong.easybluetooth.Ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import com.dong.easybluetooth.Ble.CharacteristicRequest
import com.dong.easybluetooth.Ble.CharacteristicWriteRequest

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/6 10:02
 */
open class BleServerListener {
    open fun onDeviceConnectionChange(device: BluetoothDevice?, status: Int, isConnected: Boolean) {}

    open fun onCharacteristicReadRequest(request: CharacteristicRequest) {}

    open fun onCharacteristicWriteRequest(request: CharacteristicWriteRequest) {}

    open fun onServiceAdded(service: BluetoothGattService?, isSuccess: Boolean) {}
}