package com.namibox.hfx.utils;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

/**
 * @author zhangkx
 * @Date 2019/9/27 9:46
 */
public class TextStyleUtil {
  /**
   * 设置title 颜色 样式 默认缩放大小是1.42倍
   *
   * @param targetView 目标 控件
   * @param color title颜色
   */
  public static void setTextStytle(TextView targetView, String color, String targetStart, String targetEnd) {
   setTextStytle(targetView,color,targetStart,targetEnd,1.42f);
  }
  /**
   * 设置title 颜色 样式 縮放大小
   *
   * @param targetView 目标 控件
   * @param color title颜色
   */
  public static void setTextStytle(TextView targetView, String color, String targetStart,
      String targetEnd,float scale) {
    //数字部分粗体处理
    String titleContent = targetView.getText().toString().trim();
    SpannableString spannableString = new SpannableString(titleContent);
    //设置颜色
    int start = targetStart.length();
    int end = spannableString.length() - targetEnd.length();
    //防止服务器返回数据异常 数据正常 正常显示 异常 不展示
    if (end > start) {
      spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(color)),start, end,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      //设置粗体
      spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      //设置大小
      spannableString.setSpan(new RelativeSizeSpan(scale),start, end,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    targetView.setText(spannableString);
  }
}
