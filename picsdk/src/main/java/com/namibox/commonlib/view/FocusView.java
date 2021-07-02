package com.namibox.commonlib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * Create time: 2017/8/17.
 */

public class FocusView extends View implements OnGestureListener {

  static final int BG_COLOR = 0xb2000000;
  static final int FOCUS_COLOR = 0x00000000;
  Paint paint;
  Path path;
  private float cx, cy, cr;
  private int bgColor;
  private GestureDetector gestureDetector;
  private OnClickListener onClickListener;
  private int scrollState = SCROLL_NONE;

  private static final int SCROLL_NONE = 0;
  private static final int SCROLL_LEFT = 1;
  private static final int SCROLL_RIGHT = 2;
  private OnSwipeListener swipeListener;

  public interface OnSwipeListener {
    void onSwipe(boolean swipteLeft);
  }

  public void setOnSwipeListener(OnSwipeListener swipeListener) {
    this.swipeListener = swipeListener;
  }

  public FocusView(Context context) {
    super(context);
    init(context);
  }

  public FocusView(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public FocusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    paint = new Paint();
    paint.setAntiAlias(true);
    path = new Path();
    bgColor = BG_COLOR;
    gestureDetector = new GestureDetector(context, this);
  }

  public void setBgColor(int bgColor) {
    this.bgColor = bgColor;
    invalidate();
  }

  public void addRect(float left, float top, float right, float bottom, float rx, float ry) {
    RectF rectF = new RectF(left, top, right, bottom);
    path.addRoundRect(rectF, rx, ry, Direction.CW);
    invalidate();
  }

  public void addRectF(RectF rectF, float rx, float ry) {
    path.addRoundRect(rectF, rx, ry, Direction.CW);
    invalidate();
  }

  public void addCircle(float x, float y, float radius) {
    this.cx = x;
    this.cy = y;
    this.cr = radius * 2f;
    path.addCircle(x, y, radius, Direction.CW);
    invalidate();
  }

  public void reset() {
    path.reset();
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.save();
    canvas.clipRect(0, 0, getWidth(), getHeight());
    canvas.clipPath(path, Region.Op.DIFFERENCE);
    paint.setColor(bgColor);
    canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    paint.setColor(FOCUS_COLOR);
    canvas.drawPath(path, paint);
    canvas.restore();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
      stopScroll();
    }
    return gestureDetector.onTouchEvent(event);
  }

  @Override
  public boolean onDown(MotionEvent e) {
    return true;
  }

  @Override
  public void onShowPress(MotionEvent e) {

  }

  @Override
  public boolean onSingleTapUp(MotionEvent e) {
    if (onClickListener != null) {
      onClickListener.onClick(this);
    }
    return true;
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    if (scrollState == SCROLL_NONE) {
      if (Math.abs(distanceX) > Math.abs(distanceY)) {
        if (distanceX > 0) {
          scrollState = SCROLL_LEFT;
        } else {
          scrollState = SCROLL_RIGHT;
        }
      }
      startScroll();
    } else {
      doScroll(e1, distanceX);
    }
    return true;
  }

  private void startScroll() {

  }

  private void doScroll(MotionEvent e, float distance) {
  }

  private void stopScroll() {
    if (scrollState == SCROLL_LEFT) {
      if (swipeListener != null) {
        swipeListener.onSwipe(true);
      }
    } else if (scrollState == SCROLL_RIGHT) {
      if (swipeListener != null) {
        swipeListener.onSwipe(false);
      }
    }
    scrollState = SCROLL_NONE;
  }

  @Override
  public void onLongPress(MotionEvent e) {

  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    return false;
  }

  @Override
  public void setOnClickListener(@Nullable View.OnClickListener l) {
    onClickListener = l;
  }
}
