package com.dong.bluetoothtool.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2019/7/11 09:34
 * 通用的PopUpWindow工具类
 * 不用再去写那些重复的代码
 */
public class PopWindowUtil {
    private static final String TAG = "PopWindowUtil";

    /**
     * 根据锚点View进行偏移
     *
     * @param anchorView 锚点View
     * @param window     要显示的PopUpWindow
     * @param location   方向位置
     * @param gravity    重力位置
     */
    public static void showBaseOnAnchor(View anchorView, PopupWindow window,
                                        HorizontalLocation location, HorizontalGravity gravity) {
        int anchorHeight = anchorView.getHeight();//锚点View的高度
        int anchorWidth = anchorView.getWidth();//锚点View的宽度
        //获取popupWindow的宽高
        int windowHeight = window.getHeight();
        int windowWidth = window.getWidth();
        if (windowHeight <= 0 || windowWidth <= 0) {
            window.getContentView().measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);//强制计算布局
            windowHeight = window.getContentView().getMeasuredHeight();
            windowWidth = window.getContentView().getMeasuredWidth();
        }
        int xOff = 0, yOff = 0;

        if (location == HorizontalLocation.LEFT) {
            xOff = -windowWidth;
        } else if (location == HorizontalLocation.RIGHT) {
            xOff = anchorWidth;
        }

        switch (gravity) {
            case TOP: {
                yOff = -anchorHeight;
                break;
            }
            case BOTTOM: {
                yOff = -windowHeight;
                break;
            }
            case CENTER: {
                yOff = -(windowHeight / 2 + anchorHeight / 2);
                break;
            }
        }
        Log.d(TAG, String.format("xOff=%s,yOff=%s,viewH=%s,viewW=%s,winH=%s,winW=%s",
                xOff, yOff, anchorHeight, anchorWidth, windowHeight, windowWidth));
        window.showAsDropDown(anchorView, xOff, yOff);
    }

    /**
     * 根据锚点View进行偏移
     *
     * @param anchorView 锚点View
     * @param window     要显示的PopUpWindow
     * @param location   方向位置
     * @param gravity    重力位置
     */
    public static void showBaseOnAnchor(View anchorView, PopupWindow window,
                                        VerticalLocation location, VerticalGravity gravity) {
        int anchorHeight = anchorView.getHeight();//锚点View的高度
        int anchorWidth = anchorView.getWidth();//锚点View的宽度
        //获取popupWindow的宽高
        int windowHeight = window.getHeight();
        int windowWidth = window.getWidth();
        if (windowHeight <= 0 || windowWidth <= 0) {
            window.getContentView().measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);//强制计算布局
            windowHeight = window.getContentView().getMeasuredHeight();
            windowWidth = window.getContentView().getMeasuredWidth();
        }
        int xOff = 0, yOff = 0;

        if (location == VerticalLocation.TOP) {
            yOff = -(windowHeight + anchorHeight);
        } else if (location == VerticalLocation.BOTTOM) {
            yOff = 0;
        }

        switch (gravity) {
            case LEFT: {
                xOff = 0;
                break;
            }
            case CENTER: {
                xOff = anchorWidth / 2 - windowWidth / 2;
                break;
            }
            case RIGHT: {
                xOff = anchorWidth - windowWidth;
                break;
            }
        }
        Log.d(TAG, String.format("xOff=%s,yOff=%s,viewH=%s,viewW=%s,winH=%s,winW=%s",
                xOff, yOff, anchorHeight, anchorWidth, windowHeight, windowWidth));
        window.showAsDropDown(anchorView, xOff, yOff);
    }

    /**
     * PopupWindow的建造者模式
     * 可以很快的创建一个PopupWindow
     */
    public static class Builder {
        private View contentView;
        private Drawable backgroundDrawable;//只有设置了background，才能响应事件
        private boolean touchable;//是否响应点击事件
        private boolean outSideTouchable;//是否响应外部点击事件
        private boolean focusable;//是否能获取焦点
        private int animationStyle;
        private int width;
        private int height;

        public Builder() {
            this.contentView = null;
            this.backgroundDrawable = new ColorDrawable(Color.TRANSPARENT);//默认透明背景
            this.touchable = true;
            this.outSideTouchable = true;
            this.focusable = true;
            this.animationStyle = -1;
            this.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            this.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        public Builder setContentView(View view) {
            this.contentView = view;
            return this;
        }

        public Builder setBackgroundDrawable(Drawable backgroundDrawable) {
            this.backgroundDrawable = backgroundDrawable;
            return this;
        }

        public Builder setTouchable(boolean touchable) {
            this.touchable = touchable;
            return this;
        }

        public Builder setOutSideTouchable(boolean outSideTouchable) {
            this.outSideTouchable = outSideTouchable;
            return this;
        }

        public Builder setFocusable(boolean focusable) {
            this.focusable = focusable;
            return this;
        }

        public Builder setAnimationStyle(int animationStyle) {
            this.animationStyle = animationStyle;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public PopupWindow build() {
            PopupWindow popupWindow = new PopupWindow(contentView, width, height, focusable);
            popupWindow.setBackgroundDrawable(backgroundDrawable);
            if (animationStyle != -1) {
                popupWindow.setAnimationStyle(animationStyle);
            }
            popupWindow.setTouchable(touchable);
            popupWindow.setOutsideTouchable(outSideTouchable);
            return popupWindow;
        }
    }

    /**
     * 水平方向，左右
     */
    public enum HorizontalLocation {
        LEFT, RIGHT
    }

    /**
     * 重力位置
     */
    public enum HorizontalGravity {
        TOP,
        CENTER,
        BOTTOM
    }

    /**
     * 垂直方向，上下
     */
    public enum VerticalLocation {
        TOP, BOTTOM
    }

    /**
     * 重力位置
     */
    public enum VerticalGravity {
        LEFT,
        CENTER,
        RIGHT
    }
}
