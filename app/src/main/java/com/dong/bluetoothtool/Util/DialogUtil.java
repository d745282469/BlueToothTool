package com.dong.bluetoothtool.Util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.dong.bluetoothtool.R;
import com.dong.bluetoothtool.ScanAdapter;
import com.dong.bluetoothtool.ServiceListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2019/7/11 08:34
 * 通用的dialog工具类
 */
public class DialogUtil {
    private static final String TAG = "DialogUtil";
    private static Dialog dialog;
    private static final int NORMAL_WIDTH = 300;//常用宽度，dp

    public static boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public static void dismiss() {
        if (isShowing()) {
            dialog.dismiss();
        }
    }

    public static void show() {
        if (dialog != null) {
            dismiss();
            dialog.show();
        } else {
            Log.e(TAG, "dialog is null!");
        }
    }

    /**
     * 加载中Dialog
     * 使用系统自带的ProgressDialog
     *
     * @param context 上下文
     * @param msg     内容
     */
    public static void loading(Context context, String msg) {
        dialog = new ProgressDialog(context);
        ((ProgressDialog) dialog).setMessage(msg);
        dialog.setCanceledOnTouchOutside(false);//点击外部不消失
        show();
    }

    /**
     * 服务列表Dialog
     *
     * @param serviceList 服务列表
     * @param context     上下文
     * @param listener    点击监听器
     */
    public static void showServiceDialog(
            List<BluetoothGattService> serviceList,
            Context context,
            @Nullable ServiceListAdapter.OnChildClickListener listener) {
        Log.d(TAG, "服务列表：" + Arrays.toString(serviceList.toArray()));
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_service_list, null);
        ExpandableListView listView = contentView.findViewById(R.id.el_service_list);
        ServiceListAdapter adapter = new ServiceListAdapter(serviceList);
        adapter.setOnChildClickListener(listener);
        listView.setAdapter(adapter);
        initNormalDialog(context, contentView, true);
        show();
    }

    /**
     * 设备列表Dialog
     *
     * @param deviceList 设备列表
     * @param context    上下文
     * @param listener   点击监听器
     */
    public static void showDeviceDialog(
            ArrayList<BluetoothDevice> deviceList,
            Context context,
            @Nullable BaseQuickAdapter.OnItemClickListener listener) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_device_list, null);
        RecyclerView recyclerView = contentView.findViewById(R.id.rl_device);
        ScanAdapter adapter = new ScanAdapter(deviceList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter.setOnItemClickListener(listener);
        initNormalDialog(context, contentView, true);
        show();
    }

    /**
     * 常见的Dialog初始化
     *
     * @param context              上下文
     * @param contentView          要显示的View
     * @param canceledTouchOutside 是否允许点击外部消失
     */
    private static void initNormalDialog(Context context, View contentView, boolean canceledTouchOutside) {
        dismiss();
        dialog = new Dialog(context, R.style.Dialog_Normal);
        dialog.setContentView(contentView);
        dialog.setCanceledOnTouchOutside(canceledTouchOutside);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = UnitUtil.dip2px(context, NORMAL_WIDTH);
        window.setAttributes(layoutParams);
    }

    /**
     * 可自定义Dialog的Window属性
     *
     * @param context      上下文
     * @param contentView  要显示的View
     * @param layoutParams 属性参数
     */
    private void initCustomDialog(Context context, View contentView, WindowManager.LayoutParams layoutParams) {
        dialog = new Dialog(context, R.style.Dialog_Normal);
        dialog.setContentView(contentView);
        Window window = dialog.getWindow();
        window.setAttributes(layoutParams);
    }

    /*---------------------监听器开始---------------------*/

    /**
     * 确认监听器
     */
    public interface onConfirmListener {
        void onConfirm();
    }
}
