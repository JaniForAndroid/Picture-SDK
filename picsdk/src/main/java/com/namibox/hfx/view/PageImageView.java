package com.namibox.hfx.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ryan on 2015/1/31.
 */
public class PageImageView extends View {

  private Paint paint;
  private Bitmap mBitmap;
  private RectF rectF;
  private int mBitmapHeight, mBitmapWidth;
  private Callback callback;
  private int color = 0xffffffff;

  public interface Callback {

    void onImageLoaded(boolean isPortrait, int position);
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public PageImageView(Context context) {
    super(context);
    init(context);
  }

  public PageImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public PageImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public PageImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context);
  }

  private void init(Context context) {
    paint = new Paint();
    paint.setAntiAlias(true);
    rectF = new RectF();
  }

  /**
   * 设置画布背景色
   */
  public void setCanvasBackgroundColor(int color) {
    this.color = color;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    rectF.left = 0;
    rectF.top = 0;
    rectF.right = w;
    rectF.bottom = h;
  }

  public void setImageBitmap(Bitmap bm, int position) {
    mBitmap = bm;
    if (mBitmap != null) {
      mBitmapHeight = mBitmap.getHeight();
      mBitmapWidth = mBitmap.getWidth();
    } else {
      mBitmapHeight = 0;
      mBitmapWidth = 0;
    }
    if (callback != null) {
      callback.onImageLoaded(isPortrait(), position);
    }
    postInvalidate();
  }

  public Bitmap getBitmap() {
    return mBitmap;
  }

  public boolean isPortrait() {
    return (mBitmapHeight == 0 && mBitmapWidth == 0) || mBitmapHeight > mBitmapWidth;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.drawColor(color);
    if (mBitmap != null) {
      canvas.save();
      float r = 1f * mBitmapWidth / mBitmapHeight;
      float rb = 1f * getWidth() / getHeight();
      if (r < rb) {
        float w = r * getHeight();
        rectF.left = (getWidth() - w) / 2;
        rectF.right = getWidth() - rectF.left;
        rectF.top = 0;
        rectF.bottom = getHeight();
      } else {
        float h = getWidth() / r;
        rectF.left = 0;
        rectF.right = getWidth();
        rectF.top = (getHeight() - h) / 2;
        rectF.bottom = getHeight() - rectF.top;
      }
      canvas.drawBitmap(mBitmap, null, rectF, paint);
      canvas.restore();
    }
  }

}
