package com.namibox.hfx.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Roy.chen on 2017/6/29.
 */

public class WordScrollView extends RecyclerView {

  public WordScrollView(Context context) {
    this(context, null);
  }

  public WordScrollView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

//  @Override
//  public boolean onTouchEvent(MotionEvent e) {
//    int action = e.getAction();
//    if (action == MotionEvent.ACTION_DOWN) {
//      getParent().requestDisallowInterceptTouchEvent(true);
//    }
//    return super.onTouchEvent(e);
//  }

}
