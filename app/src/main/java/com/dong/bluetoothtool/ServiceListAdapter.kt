package com.dong.bluetoothtool

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import java.lang.StringBuilder

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/7 09:07
 */
class ServiceListAdapter(private val serviceList: List<BluetoothGattService>) :
    BaseExpandableListAdapter() {
    override fun getGroup(groupPosition: Int): Any {
        return serviceList[groupPosition]
    }

    var onChildClickListener: OnChildClickListener? = null

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val service = serviceList[groupPosition]
        val context = parent!!.context
        val viewHolder = if (convertView == null) {
            val rootView = LayoutInflater.from(context).inflate(R.layout.item_service, null)
            GroupViewHolder(rootView)
        } else {
            convertView.tag as GroupViewHolder
        }

        viewHolder.tvCharaCount.text = service.characteristics.size.toString()
        viewHolder.tvServiceUUID.text = service.uuid.toString()
        return viewHolder.rootView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return serviceList[groupPosition].characteristics.size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return serviceList[groupPosition].characteristics[childPosition]
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val characteristic = serviceList[groupPosition].characteristics[childPosition]
        val context = parent!!.context
        val viewHolder = if (convertView == null) {
            val rootView = LayoutInflater.from(context).inflate(R.layout.item_characteristic, null)
            rootView.setOnClickListener {
                onChildClickListener?.onChildClick(groupPosition, childPosition)
            }
            ChildViewHolder(rootView)
        } else {
            convertView.tag as ChildViewHolder
        }
        val attr = StringBuilder()
        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) attr.append(
            "Write"
        ).append(",")
        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) attr.append(
            "Write No Response"
        ).append(",")
        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) attr.append(
            "Read"
        ).append(",")
        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) attr.append(
            "Notify"
        ).append(",")

        viewHolder.tvCharaUUID.text = characteristic.uuid.toString()
        viewHolder.tvAttr.text = if (attr.isNotEmpty()) {
            attr.removeRange(attr.length - 1, attr.length)
        } else {
            "无属性"
        }
        return viewHolder.rootView
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return serviceList.size
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return (groupPosition * childPosition).toLong()
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    class ChildViewHolder(val rootView: View) {
        init {
            rootView.tag = this
        }

        val tvCharaUUID = rootView.findViewById<TextView>(R.id.tv_characteristic_uuid)
        val tvAttr = rootView.findViewById<TextView>(R.id.tv_characteristic_attr)
    }

    class GroupViewHolder(val rootView: View) {
        init {
            rootView.tag = this
        }

        val tvCharaCount = rootView.findViewById<TextView>(R.id.tv_characteristic_count)
        val tvServiceUUID = rootView.findViewById<TextView>(R.id.tv_service_uuid)
    }

    interface OnChildClickListener {
        fun onChildClick(parentPosition: Int, childPosition: Int)
    }
}