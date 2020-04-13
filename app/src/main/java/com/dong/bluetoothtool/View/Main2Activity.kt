package com.dong.bluetoothtool.View

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dong.bluetoothtool.R
import com.dong.easybluetooth.BtManager
import com.dong.easypermission2.EasyPermission
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        getPermission()

        this.main_tv_name.text = BtManager.getName()
        this.main_tv_status.text = if (BtManager.isOn()) {
            "开启"
        } else {
            this.main_tv_status.setTextColor(getColor(R.color.txt_error))
            "关闭"
        }

        this.main_btn_classical_server.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ClassicalServerActivity::class.java
                )
            )
        }

        this.main_btn_classical_client.setOnClickListener {
            ScanActivity.start(this, false)
        }

        this.main_btn_ble_server.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    BleServerActivity::class.java
                )
            )
        }

        this.main_btn_ble_client.setOnClickListener {
            ScanActivity.start(this, true)
        }
    }

    fun getPermission(): Boolean {
        val permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN
        )
        return EasyPermission.inActivity(this)
            .permissions(permissions)
            .granted()
    }
}
