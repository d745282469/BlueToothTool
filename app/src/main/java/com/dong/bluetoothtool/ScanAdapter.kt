package com.dong.bluetoothtool

import android.bluetooth.BluetoothDevice
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/3/30 16:57
 */
class ScanAdapter(dataList: ArrayList<BluetoothDevice>) :
    BaseQuickAdapter<BluetoothDevice, BaseViewHolder>(R.layout.item_device, dataList) {
    override fun convert(helper: BaseViewHolder?, item: BluetoothDevice?) {
        helper?.let {
            val name = if (item?.name == null){
                item?.address
            }else{
                "${item.name}(${item.address})"
            }
            it.setText(R.id.item_device_tv_name,name)

            val iv = it.getView<ImageView>(R.id.item_device_iv_icon)
            val context = iv.context
            var img = context.getDrawable(R.mipmap.icon_bluetooth)
            when (item?.bluetoothClass?.majorDeviceClass){
                256 -> img = context.getDrawable(R.mipmap.icon_computer)
                512 -> img = context.getDrawable(R.mipmap.icon_phone)
                1024 -> img = context.getDrawable(R.mipmap.icon_audio)
            }
            iv.setImageDrawable(img)
        }
    }
}