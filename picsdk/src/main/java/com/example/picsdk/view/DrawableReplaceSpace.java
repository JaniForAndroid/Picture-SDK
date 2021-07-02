package com.example.picsdk.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ReplacementSpan;

import com.namibox.util.Utils;

public class DrawableReplaceSpace extends ReplacementSpan {

  private int mWidth;
  private int mTextWidth;
  private Bitmap mBitmap;
  private Context mContext;

  public DrawableReplaceSpace(Context context, Bitmap bitmap) {
    mContext = context;
    mBitmap = bitmap;
  }

  @Override

  public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
    String currenttext = text.toString().substring(start, end);
    //将返回相对于Paint画笔的文本
    mTextWidth = (int) paint.measureText(text, start, end);
    mWidth = mTextWidth;
    return mTextWidth;
  }

  @Override
  public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {

    paint.setColor(0xFF333333);
    paint.setStrokeWidth(Utils.dp2px(mContext, 1.5f));
    canvas.drawLine(x, y + Utils.dp2px(mContext, 2), x + mWidth, y + Utils.dp2px(mContext, 2), paint);
    Rect rect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
    RectF rectF = new RectF(x, y - mBitmap.getHeight(), x + mBitmap.getWidth(), y);
    canvas.drawBitmap(mBitmap, rect, rectF, paint);
  }
}
