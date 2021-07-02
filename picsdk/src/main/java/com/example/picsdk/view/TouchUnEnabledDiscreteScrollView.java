package com.example.picsdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.yarolegovich.discretescrollview.DiscreteScrollView;

public class TouchUnEnabledDiscreteScrollView extends DiscreteScrollView {

  public TouchUnEnabledDiscreteScrollView(Context context) {
    super(context);
  }

  public TouchUnEnabledDiscreteScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent e) {
    return false;
  }

  @Override
  public boolean onTouchEvent(MotionEvent e) {
    return true;
  }
}
