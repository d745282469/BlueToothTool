package com.dong.bluetoothtool.View

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dong.bluetoothtool.R
import com.dong.bluetoothtool.ScanAdapter
import com.dong.easybluetooth.BtManager
import com.dong.easybluetooth.ScanListener
import kotlinx.android.synthetic.main.activity_scan.*

class ScanActivity : AppCompatActivity() {
    private val tag = this::class.java.simpleName

    private val bondedDevices = ArrayList<BluetoothDevice>()
    private val bondedAdapter = ScanAdapter(bondedDevices)

    private val scanDevices = ArrayList<BluetoothDevice>()
    private val scanAdapter = ScanAdapter(scanDevices)

    private var isBle = false

    companion object {
        @JvmStatic
        fun start(activity: Activity, isBle: Boolean) {
            val intent = Intent(activity, ScanActivity::class.java)
            intent.putExtra("isBle", isBle)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        isBle = intent.getBooleanExtra("isBle", false)

        this.scan_rl_bonded_device.adapter = bondedAdapter
        this.scan_rl_bonded_device.layoutManager = LinearLayoutManager(this)

        this.scan_rl_device.adapter = scanAdapter
        this.scan_rl_device.layoutManager = LinearLayoutManager(this)

        if (!isBle) {
            // 获取到的已绑定的设备是经典蓝牙的
            bondedDevices.addAll(BtManager.getBondedDevice())
        }

        val animation = ObjectAnimator.ofFloat(
            this.scan_iv_search,
            "rotation",
            this.scan_iv_search.rotation,
            360f
        )
        animation.duration = 3000
        animation.repeatCount = -1
        animation.repeatMode = ValueAnimator.RESTART

        this.scan_ll_search.setOnClickListener {
            if (BtManager.isScanning) {
                BtManager.stopScan()
                animation.cancel()
            } else {
                scanDevices.clear()
                scanAdapter.notifyDataSetChanged()
                val type = if (isBle) {
                    BtManager.BluetoothType.LOW_ENG
                } else {
                    BtManager.BluetoothType.CLASSICAL
                }
                BtManager.scanDevice(object : ScanListener {
                    override fun foundDevice(device: BluetoothDevice) {
                        scanDevices.add(0, device)
                        scanAdapter.notifyDataSetChanged()
                    }
                }, type = type)
                animation.start()
            }
        }

        scanAdapter.setOnItemClickListener { _, _, position ->
            gotoClient(scanDevices[position])
        }
        bondedAdapter.setOnItemClickListener { _, _, position ->
            gotoClient(bondedDevices[position])
        }
    }

    private fun gotoClient(device: BluetoothDevice) {
        if (isBle) {
            BleClientActivity.start(this, device)
        } else {
            ClassicalClientActivity.start(this, device)
        }
    }

    override fun onPause() {
        BtManager.stopScan()
        super.onPause()
    }
}
