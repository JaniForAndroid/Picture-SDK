package com.namibox.commonlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import com.example.picsdk.R;

/**
 * author : feng
 * description ：无线滚动长图img
 * creation time : 19-9-10下午7:07
 */
public class RollImageView extends View {

  private int speed;
  private int resourceId;
  private int width, height;
  //right = 0 left 1
  private int direction;
  private Bitmap bitmap;
  private int x = 0;

  public RollImageView(Context context) {
    this(context, null);
  }

  public RollImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RollImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RollImageView, 0, 0);
    for (int i = 0; i < a.getIndexCount(); i++) {
      int attr = a.getIndex(i);
      if (attr == R.styleable.RollImageView_speed) {
        speed = a.getInteger(R.styleable.RollImageView_speed, 10);
      } else if (attr == R.styleable.RollImageView_src) {
        resourceId = a.getResourceId(R.styleable.RollImageView_src, 0);
      } else if (attr == R.styleable.RollImageView_direction) {
        direction = a.getInteger(R.styleable.RollImageView_direction, 0);
      }
    }
    a.recycle();
  }

  /**
   * 滚动速度
   *
   * @param speed 单位像素
   */
  public void setSpeed(int speed) {
    this.speed = speed;
  }

  /**
   * 设置资源图片
   *
   * @param resourceId 资源id
   */
  public void setImageResourceId(int resourceId) {
    this.resourceId = resourceId;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    width = w;
    height = h;
    if (bitmap == null) {
      bitmap = decodeBitmap();
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    drawBitmap(bitmap, canvas);
  }

  private Bitmap decodeBitmap() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), resourceId, options);
    float ratio = height * 1.0f / bitmap.getHeight();
    return scaleBitmap(bitmap, ratio);
  }

  private Bitmap scaleBitmap(Bitmap origin, float ratio) {
    if (origin == null) {
      return null;
    }
    int width = origin.getWidth();
    int height = origin.getHeight();
    Matrix matrix = new Matrix();
    matrix.preScale(ratio, ratio);
    Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
    if (direction == 0) {
      x = newBM.getWidth();
    } else {
      x = 0;
    }
    if (newBM.equals(origin)) {
      return newBM;
    }
    origin.recycle();
    return newBM;
  }

  private void drawBitmap(Bitmap bitmap, Canvas canvas) {
    int bitmapWidth = bitmap.getWidth();
    int bitmapHeight = bitmap.getHeight();
    if (direction == 0) {
      if (x >= width && x <= bitmapWidth){
        Rect srcRect = new Rect(x - width, 0, x, bitmapHeight);
        Rect destRect = new Rect(0, 0, width, height);
        canvas.drawBitmap(bitmap, srcRect, destRect, null);
        x -= speed;
        invalidate();
      } else if (x > 0 && x < width) {
        Rect srcRect = new Rect(bitmapWidth - width + x, 0, bitmapWidth, bitmapHeight);
        Rect destRect = new Rect(0, 0, width-x, height);
        canvas.drawBitmap(bitmap, srcRect, destRect, null);
        Rect srcRect2 = new Rect(0, 0, x, bitmapHeight);
        Rect destRect2 = new Rect(width-x, 0, width, height);
        canvas.drawBitmap(bitmap, srcRect2, destRect2, null);
        x -= speed;
        invalidate();
      } else {
        x = bitmapWidth;
        Rect srcRect = new Rect(x - width, 0, x, bitmapHeight);
        Rect destRect = new Rect(0, 0, width, height);
        canvas.drawBitmap(bitmap, srcRect, destRect, null);
        x -= speed;
        invalidate();
      }
    } else {
      if (x >= 0 && x <= bitmapWidth - width) {
        Rect srcRect = new Rect(x, 0, x + width, bitmapHeight);
        Rect destRect = new Rect(0, 0, width, height);
        canvas.drawBitmap(bitmap, srcRect, destRect, null);
        x += speed;
        invalidate();
      } else if (x > bitmapWidth - width && x <= bitmapWidth) {
        Rect srcRect = new Rect(x, 0, bitmapWidth, bitmapHeight);
        Rect destRect = new Rect(0, 0, bitmapWidth - x, height);
        canvas.drawBitmap(bitmap, srcRect, destRect, null);
        Rect srcRect2 = new Rect(0, 0, x + width - bitmapWidth, bitmapHeight);
        Rect destRect2 = new Rect(destRect.right, 0, width, height);
        canvas.drawBitmap(bitmap, srcRect2, destRect2, null);
        x += speed;
        invalidate();
      } else {
        x -= bitmapWidth;
        Rect srcRect = new Rect(x, 0, x + width, bitmapHeight);
        Rect destRect = new Rect(0, 0, width, height);
        canvas.drawBitmap(bitmap, srcRect, destRect, null);
        x += speed;
        invalidate();
      }
    }
  }
}
