package com.dong.easybluetooth.Ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import com.dong.easybluetooth.Ble.CharacteristicRequest

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/6 10:20
 */
class CharacteristicWriteRequest(
    device: BluetoothDevice?,
    requestId: Int,
    characteristic: BluetoothGattCharacteristic?,
    offset: Int,
    val preparedWrite: Boolean,
    val responseNeeded: Boolean,
    val value: ByteArray?
) : CharacteristicRequest(device, requestId, characteristic, offset) {

}