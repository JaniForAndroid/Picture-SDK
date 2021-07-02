package com.example.picsdk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ReplacementSpan;

import com.namibox.util.Utils;

public class LineReplaceSpace extends ReplacementSpan {

  private int mWidth;
  private boolean mIsSelect;
  private boolean mDrawText;
  private Context mContext;

  public LineReplaceSpace(Context context, boolean isSelect, boolean drawText) {
    mIsSelect = isSelect;
    mDrawText = drawText;
    mContext = context;
  }

  @Override

  public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
    //将返回相对于Paint画笔的文本
    mWidth = (int) paint.measureText(text, start, end);
    return mWidth;
  }

  @Override
  public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
    if (mIsSelect) {
      paint.setColor(0xFF00B9FF);
    } else {
      paint.setColor(0xFF333333);
    }
//    paint.setColor(0xffffff);
    paint.setStrokeWidth(Utils.dp2px(mContext, 1.5f));

//    canvas.drawRect(x, top + 8, x + mWidth, bottom - 15, paint);
    canvas.drawLine(x, y + Utils.dp2px(mContext, 2), x + mWidth, y + Utils.dp2px(mContext, 2), paint);
    if (mDrawText) {
      canvas.drawText(text.toString().substring(start, end), x, y, paint);
    }
  }
}
