package com.dong.bluetoothtool

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.dong.bluetoothtool.Bean.CmdMsg
import com.dong.bluetoothtool.Bean.CmdMsgType
import java.util.*
import kotlin.collections.ArrayList

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/2 16:15
 */
class BlackBoardAdapter(dataList: ArrayList<CmdMsg>,recyclerView: RecyclerView) :
    BaseQuickAdapter<CmdMsg, BaseViewHolder>(R.layout.item_backboard, dataList) {

    private val calendar = Calendar.getInstance()

    override fun convert(helper: BaseViewHolder?, item: CmdMsg?) {
        helper?.let {
            val h = if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 10){
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
            }else{
                "0"+Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
            }
            val m = if(Calendar.getInstance().get(Calendar.MINUTE) >= 10){
                Calendar.getInstance().get(Calendar.MINUTE).toString()
            }else{
                "0"+Calendar.getInstance().get(Calendar.MINUTE).toString()
            }
            val s = if(Calendar.getInstance().get(Calendar.SECOND) >= 10){
                Calendar.getInstance().get(Calendar.SECOND).toString()
            }else{
                "0"+Calendar.getInstance().get(Calendar.SECOND).toString()
            }
            it.setText(R.id.item_backboard_tv_msg, item?.msg)

            it.setText(R.id.item_backboard_tv_time, "$h:$m:$s")

            val tv = it.getView<TextView>(R.id.item_backboard_tv_msg)
            val context = tv.context
            var color = context.getColor(R.color.txt_backboard_normal)
            when (item?.type) {
                CmdMsgType.ERROR -> color = context.getColor(R.color.txt_error)
                CmdMsgType.NORMAL -> color = context.getColor(R.color.txt_backboard_normal)
                CmdMsgType.WARNING -> color = context.getColor(R.color.txt_warn)
            }
            tv.setTextColor(color)
        }
    }

    init {
        bindToRecyclerView(recyclerView)
        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                if (this@BlackBoardAdapter.recyclerView != null) {
                    this@BlackBoardAdapter.recyclerView.scrollToPosition(dataList.size - 1)
                }
            }
        })
    }
}