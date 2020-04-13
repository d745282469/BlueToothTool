package com.dong.easybluetooth.Classical

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/5 09:09
 */
class BluetoothClassicalClient(
    val serverDevice: BluetoothDevice,
    private val uuid: UUID
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Job()

    var clientListener: ClassicalClientListener? = null

    var messageListener: BluetoothMessageListener? = null

    // 与服务端通信的socket
    private var serverSocket: BluetoothSocket? = null

    /**
     * 开始连接服务端
     */
    fun connectServer() {
        launch(Dispatchers.IO) {
            val socket = serverDevice.createInsecureRfcommSocketToServiceRecord(uuid)
            try {
                socket.connect()// 阻塞直到连接成功或者出现异常
                notifyListenerConnected()
                serverSocket = socket
                // 连接成功后开启消息轮询
                startListenMsg()
            } catch (e: Exception) {
                // 可能在连接的时候，服务端关闭了
                notifyListenerError("发生异常：${e.message}")
                socket.close()
            }
        }
    }

    /**
     * 向服务端发送消息
     * @param data
     */
    fun send(data: ByteArray) {
        launch(Dispatchers.IO) {// 切换到IO线程
            if (serverSocket != null) {
                if (serverSocket!!.isConnected) {
                    try {
                        serverSocket!!.outputStream.write(data)
                    } catch (e: Exception) {
                        notifyMessageFail("发生异常：${e.message}")
                        serverSocket!!.close()
                    }
                }
            }
        }
    }

    private fun startListenMsg(){
        launch(Dispatchers.IO) {
            if (serverSocket != null) {
                if (serverSocket!!.isConnected) {
                    while (serverSocket!!.isConnected) {
                        try {
                            val data = ByteArray(serverSocket!!.inputStream.available())
                            serverSocket!!.inputStream.read(data)
                            if (data.isNotEmpty()) {
                                notifyMessageReceive(data)
                            }
                            delay(500)// 500ms轮询
                        } catch (e: Exception) {
                            notifyMessageFail("发生异常：${e.message}")
                            serverSocket!!.close()
                        }
                    }

                }
            }
        }
    }

    private fun notifyMessageReceive(data: ByteArray) {
        launch(Dispatchers.Main) {
            messageListener?.onReceiveMsg(data)
        }
    }

    private fun notifyMessageFail(msg: String) {
        launch(Dispatchers.Main) {
            messageListener?.onFail(msg)
        }
    }

    /**
     * 关闭与服务端的连接
     */
    fun disConnect() {
        serverSocket?.close()
        messageListener = null
    }

    fun isConnected():Boolean{
        if (serverSocket == null) return false
        return serverSocket!!.isConnected
    }

    private fun notifyListenerError(msg: String) {
        launch(Dispatchers.Main) {
            clientListener?.onFail(msg)
        }
    }

    private fun notifyListenerConnected() {
        launch(Dispatchers.Main) {
            clientListener?.onConnected()
        }
    }
}