package com.namibox.tools;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;

/**
 * author : feng
 * description ：控件使用帮助工具类
 * creation time : 18-12-12下午4:17
 */
public class ViewUtil {

  /**
   * 扩大控件触摸区域
   *
   * @param view 控件
   * @param size 四周扩大大小，单位:px
   */
  public static void expandTouchArea(final View view, final int size) {
    final View parentView = (View) view.getParent();
    parentView.post(new Runnable() {
      @Override
      public void run() {
        Rect rect = new Rect();
        view.getHitRect(rect);
        rect.top -= size;
        rect.bottom += size;
        rect.left -= size;
        rect.right += size;
        parentView.setTouchDelegate(new TouchDelegate(rect, view));
      }
    });
  }
}
