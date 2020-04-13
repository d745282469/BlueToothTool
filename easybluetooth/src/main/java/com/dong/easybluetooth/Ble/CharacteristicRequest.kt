package com.dong.easybluetooth.Ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/6 10:04
 * 由客户端发给服务端的请求
 */
open class CharacteristicRequest(
    val device: BluetoothDevice?,
    val requestId: Int,
    val characteristic: BluetoothGattCharacteristic?,
    val offset:Int
) {
}