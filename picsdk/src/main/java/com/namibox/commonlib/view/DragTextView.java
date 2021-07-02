package com.namibox.commonlib.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class DragTextView extends AppCompatTextView {

  private int mParentHeight;
  private int mParentWidth;
  private int lastX;
  private int lastY;
  private boolean mIsTouch;
  private int mVisibilityLater;

  public DragTextView(Context context) {
    this(context, null);
  }

  public DragTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int rawX = (int) event.getRawX();
    int rawY = (int) event.getRawY();
    mIsTouch = true;
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        lastX = rawX;
        lastY = rawY;
        if (getParent() != null) {
          mParentWidth = ((ViewGroup) getParent()).getWidth();
          mParentHeight = ((ViewGroup) getParent()).getHeight();
        }
        break;
      case MotionEvent.ACTION_MOVE:

        int dx = rawX - lastX;
        int dy = rawY - lastY;
        float x = getX() + dx;
        float y = getY() + dy;
        //检测是否到达边缘 左上右下
        x = x < 0 ? 0 : x > mParentWidth - getWidth() ? mParentWidth - getWidth() : x;
        y = y < 0 ? 0 : y > mParentHeight - getHeight() ? mParentHeight - getHeight() : y;
        setX(x);
        setY(y);
        lastX = rawX;
        lastY = rawY;
        break;
      case MotionEvent.ACTION_UP:
        mIsTouch = false;
        setVisibility(mVisibilityLater);
        break;
    }
    return true;
  }

  @Override
  public void setVisibility(int visibility) {
    if (!mIsTouch) {
      super.setVisibility(visibility);
    }
    mVisibilityLater = visibility;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    if (oldh == 0 || oldw == 0) {
      return;
    }
    if (getParent() != null) {
      mParentWidth = ((ViewGroup) getParent()).getWidth();
      mParentHeight = ((ViewGroup) getParent()).getHeight();
    } else {
      return;
    }
    if (getX() < 0) {
      setX(0);
    }
    if (getX() > mParentWidth - w) {
      setX(mParentWidth - w);
    }
    if (getY() < 0) {
      setY(0);
    }
    if (getY() > mParentHeight - h) {
      setY(mParentHeight - h);
    }
  }

}
