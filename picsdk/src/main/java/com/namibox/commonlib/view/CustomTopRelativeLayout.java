package com.namibox.commonlib.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowInsets;
import android.widget.RelativeLayout;

/**
 * Create time: 2017/3/24.
 */

public class CustomTopRelativeLayout extends RelativeLayout {

  public CustomTopRelativeLayout(Context context) {
    super(context);
  }

  public CustomTopRelativeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomTopRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected boolean fitSystemWindows(Rect insets) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
      Log.d("CustomTopRelativeLayout", "fitSystemWindows top:" + insets.top);
      insets.top = 0;
    }

    return super.fitSystemWindows(insets);
  }

  @Override
  public WindowInsets onApplyWindowInsets(WindowInsets insets) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
      Log.d("CustomTopRelativeLayout",
          "onApplyWindowInsets top:" + insets.getSystemWindowInsetTop());
      return super.onApplyWindowInsets(
          insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0,
              insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom()));
    } else {
      return super.onApplyWindowInsets(insets);
    }
  }
}
