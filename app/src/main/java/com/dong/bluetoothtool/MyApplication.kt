package com.dong.bluetoothtool

import android.app.Application
import com.dong.easybluetooth.BtManager

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/2 10:50
 */
class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        BtManager.init(this)
    }
}