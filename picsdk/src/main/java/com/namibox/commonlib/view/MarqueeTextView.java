package com.namibox.commonlib.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * Create time: 2016/9/7.
 */

public class MarqueeTextView extends android.support.v7.widget.AppCompatTextView {

  public MarqueeTextView(Context con) {
    super(con);
  }

  public MarqueeTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean isFocused() {
    return true;
  }

  @Override
  protected void onFocusChanged(boolean focused, int direction,
      Rect previouslyFocusedRect) {
    // TODO Auto-generated method stub
    super.onFocusChanged(true, direction, previouslyFocusedRect);// 重点
  }
}
