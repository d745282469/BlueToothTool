package com.dong.bluetoothtool.Widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.CheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dong.bluetoothtool.Bean.CmdMsg
import com.dong.bluetoothtool.Bean.CmdMsgType
import com.dong.bluetoothtool.BlackBoardAdapter
import com.dong.bluetoothtool.R
import com.dong.bluetoothtool.Util.PopWindowUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/4/3 10:50
 */
class BlackBoardView(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs), CoroutineScope {
    private val tag = this::class.java.simpleName
    private val msgList = ArrayList<CmdMsg>()
    private val layoutManager = LinearLayoutManager(context)
    private val adapter = BlackBoardAdapter(msgList, this)
    private val popView = LayoutInflater.from(context).inflate(R.layout.pop_data_type, null)
    private val checkBox = (popView as ViewGroup).findViewById<CheckBox>(R.id.cb_data_type)

    private val longTouchListener =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent?) {
//                showPopMenu(e!!)
            }
        })

    /**
     * 弹出窗口选择字节数组的方式
     */
    private fun showPopMenu(e: MotionEvent) {
        val pop = PopWindowUtil.Builder()
            .setContentView(popView)
            .build()

        pop.showAtLocation(this, Gravity.START or Gravity.TOP, e.x.toInt(), e.y.toInt())
    }

    fun isByteData():Boolean{
        return checkBox.isChecked
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        if (longTouchListener.onTouchEvent(e)) return true
        return super.onTouchEvent(e)
    }

    init {
        setAdapter(adapter)
        setLayoutManager(layoutManager)
    }

    fun addCmsMsg(msg: CmdMsg) {
        msgList.add(msg)
        launch(Dispatchers.Main) {
            adapter.notifyItemChanged(msgList.size - 1)
        }
//        Log.d(tag,"显示信息：${msg.msg}")
    }

    fun addMsg(msg: String) {
        addCmsMsg(CmdMsg(CmdMsgType.NORMAL, msg))
    }

    fun addErrMsg(msg: String) {
        addCmsMsg(CmdMsg(CmdMsgType.ERROR, msg))
    }

    fun addWarnMsg(msg: String) {
        addCmsMsg(CmdMsg(CmdMsgType.WARNING, msg))
    }

    override val coroutineContext: CoroutineContext
        get() = Job()
}