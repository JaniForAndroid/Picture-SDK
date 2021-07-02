package com.namibox.imageselector.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SizePickerView extends View {

  private Paint mPaint;
  private Paint mSelectPaint;
  private float[] sizeArray;
  private float selected;
  private float padding;
  private float strokeWidth;
  private GestureDetector gestureDetector;
  private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      float x = e.getX();
      int index = getTouchedIndex(x);
      setSelected(sizeArray[index]);
      return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }
  };

  public SizePickerView(Context context) {
    super(context);
    init();
  }

  public SizePickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public SizePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public SizePickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setColor(Color.WHITE);
    mPaint.setStyle(Paint.Style.FILL);

    float density = getContext().getResources().getDisplayMetrics().density;
    padding = 4 * density;
    strokeWidth = 2 * density;
    mSelectPaint = new Paint();
    mSelectPaint.setAntiAlias(false);
    mSelectPaint.setStrokeWidth(strokeWidth);
    mSelectPaint.setColor(Color.RED);
    mSelectPaint.setStyle(Paint.Style.STROKE);
    gestureDetector = new GestureDetector(getContext(), simpleOnGestureListener);
  }

  public void setSizeArray(float[] sizeArray, float selected) {
    this.sizeArray = sizeArray;
    this.selected = selected;
  }

  public float getSelectedSize() {
    return selected;
  }

  private void setSelected(float selected) {
    this.selected = selected;
    invalidate();
    if (callback != null) {
      callback.onSizeSelected(selected);
    }
  }

  private Callback callback;

  public interface Callback {

    void onSizeSelected(float size);
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  private int getTouchedIndex(float x) {
    float section_w = getWidth() / sizeArray.length;
    int index = (int) (x / section_w);
    return index;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (sizeArray == null) {
      return;
    }
    float w = getWidth();
    float h = getHeight();
    float section_w = w / sizeArray.length;
    int i = 0;
    for (float size : sizeArray) {
      float cx = i * section_w + section_w / 2;
      float cy = h / 2f;
      canvas.drawCircle(cx, cy, size / 2, mPaint);
      if (size == selected) {
        float left = i * section_w + padding - strokeWidth / 2;
        float top = padding - strokeWidth / 2;
        float right = left + section_w - 2 * padding + strokeWidth / 2;
        float bottom = h - padding + strokeWidth / 2;
        canvas.drawRect(left, top, right, bottom, mSelectPaint);
      }
      i++;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
  }
}
