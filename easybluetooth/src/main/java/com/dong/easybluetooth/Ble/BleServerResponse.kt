package com.dong.easybluetooth.Ble

import com.dong.easybluetooth.Ble.CharacteristicRequest

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/6 10:08
 * 服务端发送给客户端的响应
 */
class BleServerResponse(
    val request: CharacteristicRequest,
    val status: Int,
    val data: ByteArray,
    val offset: Int = 0
) {
}