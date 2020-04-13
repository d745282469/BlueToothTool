package com.dong.easybluetooth.Classical

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/5 09:18
 * 消息监听器，监听来自客户端的消息以及客户端状态
 */
open class BluetoothMessageListener {
    /**
     * 接收到客户端传来的消息
     * @param msg
     */
    open fun onReceiveMsg(msg:ByteArray){}

    /**
     * 客户端发生异常
     * @param errorMsg 错误信息
     */
    open fun onFail(errorMsg:String){}
}