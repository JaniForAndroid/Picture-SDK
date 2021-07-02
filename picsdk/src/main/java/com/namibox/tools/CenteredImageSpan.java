package com.namibox.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ImageSpan;

/**
 * @author: zbd
 * @time: 2018/12/7
 */
public class CenteredImageSpan extends ImageSpan {

  public CenteredImageSpan(Drawable drawable) {
    super(drawable);

  }

  public CenteredImageSpan(Context context, Bitmap b) {
    super(context, b);
  }

  @Override
  public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom,
      @NonNull Paint paint) {
    Drawable b = getDrawable();
    canvas.save();
    int transY;
    transY = ((bottom - top) - b.getBounds().bottom) / 2 + top;
    canvas.translate(x, transY);
    b.draw(canvas);
    canvas.restore();
  }

  @Override
  public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
    Drawable d = getDrawable();
    Rect rect = d.getBounds();
    if (fm != null) {
      FontMetricsInt fmPaint = paint.getFontMetricsInt();
      int fontHeight = fmPaint.bottom - fmPaint.top;
      int drHeight = rect.bottom - rect.top;

      int top = drHeight / 2 - fontHeight / 4;
      int bottom = drHeight / 2 + fontHeight / 4;

      fm.ascent = -bottom;
      fm.top = -bottom;
      fm.bottom = top;
      fm.descent = top;
    }
    return rect.right;
  }
}
