package com.namibox.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.style.ReplacementSpan;

/**
 * author : feng
 * description ：TextView不同字体大小、颜色居中显示
 * creation time : 18-11-28上午10:06
 */
public class VerticalCenterSpan extends ReplacementSpan {

  private int color;
  private boolean colorEnable;

  public VerticalCenterSpan() {
  }

  public VerticalCenterSpan(@ColorInt int color) {
    this.color = color;
    colorEnable = true;
  }

  @Override
  public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
    text = text.subSequence(start, end);
    return (int) paint.measureText(text.toString());
  }

  @Override
  public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom,
      @NonNull Paint paint) {
    text = text.subSequence(start, end);
    Paint.FontMetricsInt fm = paint.getFontMetricsInt();
    if (colorEnable) {
      paint.setColor(color);
    }
    canvas.drawText(text.toString(), x, y - ((y + fm.descent + y + fm.ascent) / 2 - (bottom + top) / 2), paint);
  }
}
