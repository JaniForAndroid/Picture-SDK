package com.namibox.commonlib.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

/**
 * Create time: 2017/3/24.
 */

public class CustomTopRelativeLayoutV19 extends RelativeLayout {

  public CustomTopRelativeLayoutV19(Context context) {
    super(context);
  }

  public CustomTopRelativeLayoutV19(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomTopRelativeLayoutV19(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected boolean fitSystemWindows(Rect insets) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
      Log.d("CustomTopRelativeLayout","fitSystemWindows top:" + insets.top);
      insets.top = 0;
    }

    return super.fitSystemWindows(insets);
  }

}

