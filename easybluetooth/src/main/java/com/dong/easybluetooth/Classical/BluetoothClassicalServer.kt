package com.dong.easybluetooth.Classical

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/5 08:34
 * 经典蓝牙服务端
 */
class BluetoothClassicalServer(
    val serverName: String,
    val uuid: UUID
) : CoroutineScope {
    var serverSocket: BluetoothServerSocket? = null

    // 是否正在监听设备连接
    var isRunning = false
        private set

    // 已经连接上来的客户端
    val clientList = ArrayList<Client>()

    var listener: ClassicalServerListener? = null

    val adapter = BluetoothAdapter.getDefaultAdapter()

    /**
     * 开启服务端
     * @param timeout 超时时间，-1则表示无限
     */
    fun start(timeout: Int = -1) {
        if (isRunning) return

        serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(serverName, uuid)

        // 开启一个IO线程的协程，去监听客户端设备连接
        launch(Dispatchers.IO) {
            try {
                isRunning = true
                // serverSocket.accept()是阻塞方法，会一直阻塞直到有客户端连接上来或超时
                val clientSocket = serverSocket!!.accept(timeout)
                addClient(clientSocket)
                start()// 继续监听连接
                Log.d("d","监听服务端")
            } catch (e: Exception) {
                // 这里的异常可能是超时、也可能是手动关闭服务端导致的
                if (isRunning) {
                    // 超时等系统异常
                    notifyListenerError("发生异常：${e.message}")
                }
                isRunning = false
                serverSocket?.close()
            }
        }
    }

    private fun addClient(clientSocket:BluetoothSocket){
        // 排除已经在列表中的设备
        val client = Client(clientSocket,this)
        var b = false
        for (existClient in clientList) {
            if (existClient.remoteDevice.address == client.remoteDevice.address) {
                b = true
                break
            }
        }
        if (!b) clientList.add(client)
        notifyListenerConnected(client)
    }

    private fun notifyListenerError(msg: String) {
        launch(Dispatchers.Main) {
            listener?.onFail(msg)
        }
    }

    private fun notifyListenerConnected(client: Client) {
        launch(Dispatchers.Main) {
            listener?.onDeviceConnected(client)
        }
    }

    /**
     * 关闭服务端，但是已经连接的客户端依然允许通信
     */
    fun stop() {
        isRunning = false
        serverSocket?.close()
    }

    /**
     * 从设备列表移除
     * @param client 客户端
     */
    private fun removeClient(client: Client){
        clientList.remove(client)
    }

    class Client(
        private val socket: BluetoothSocket,
        private val server:BluetoothClassicalServer
    ) : CoroutineScope {
        private val ous = socket.outputStream
        private val ins = socket.inputStream
        val remoteDevice = socket.remoteDevice

        var messageListener: BluetoothMessageListener? = null

        init {
            Log.d("d", "服务端的客户端初始化")
            listenMsg()
        }

        /**
         * 轮询监听消息，500ms
         */
        private fun listenMsg() {
            launch(Dispatchers.IO) {
                if (socket.isConnected) {
                    while (socket.isConnected) {
                        try {
                            val data = ByteArray(ins.available())
                            ins.read(data)
                            if (data.isNotEmpty()) {
                                notifyListenerReceive(data)
                            }
                            delay(500) // 500ms轮询
                        } catch (e: Exception) {
                            notifyListenerError("发生异常：${e.message}")
                            disConnect()
                        }
                    }
                } else {
                    notifyListenerError("初始化客户端失败")
                }
            }
        }

        private fun notifyListenerError(msg: String) {
            launch(Dispatchers.Main) {
                messageListener?.onFail(msg)
            }
        }

        private fun notifyListenerReceive(msg: ByteArray) {
            launch(Dispatchers.Main) {
                messageListener?.onReceiveMsg(msg)
            }
        }

        /**
         * 关闭与客户端的连接
         */
        fun disConnect() {
            socket.close()
            messageListener = null
            // 从服务端设备列表中移除
            server.removeClient(this)
        }

        /**
         * 向该客户端发送消息
         * @param msg 要发送的消息
         */
        fun send(msg: ByteArray) {
            if (!socket.isConnected) {
                notifyListenerError("连接已断开")
            } else {
                launch(Dispatchers.IO) {
                    try {
                        ous.write(msg)
                    } catch (e: Exception) {
                        // 可能在发送信息的时候，与客户端断开连接
                        notifyListenerError("发生异常：${e.message}")
                        disConnect()
                    }
                }
            }
        }

        /**
         * 当前客户端是否与当前服务端连接
         * @return true表示已连接/false表示未连接
         */
        fun isConnected(): Boolean {
            return socket.isConnected
        }

        override val coroutineContext: CoroutineContext
            get() = Job()
    }

    override val coroutineContext: CoroutineContext
        get() = Job()
}