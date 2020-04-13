package com.dong.easybluetooth.Classical

import com.dong.easybluetooth.Classical.BluetoothClassicalServer

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/2 17:20
 */
open class ClassicalServerListener {
    /**
     * 客户端设备已连接，可能会多次被调用
     * @param client 客户端
     */
    open fun onDeviceConnected(client: BluetoothClassicalServer.Client) {}

    /**
     * 服务端发生异常
     * @param errorMsg 错误信息
     */
    open fun onFail(errorMsg: String) {}
}