package com.namibox.hfx.utils;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;

public class MyPageTransformer implements ViewPager.PageTransformer {
  /**
   * Y方向最小缩放值
   */
  private static final float MIN_SCALE_Y = 0.8f;

  @Override
  public void transformPage(@NonNull View page, float position) {
    if (position >= 1 || position <= -1) {
      page.setScaleY(MIN_SCALE_Y);
    } else if (position < 0) {
      //  -1 < position < 0
      //View 在再从中间往左边移动，或者从左边往中间移动
      float scaleY = MIN_SCALE_Y + (1 + position) * (1 - MIN_SCALE_Y);
      page.setScaleY(scaleY);
    } else {
      // 0 <= positin < 1
      //View 在从中间往右边移动 或者从右边往中间移动
      float scaleY = (1 - MIN_SCALE_Y) * (1 - position) + MIN_SCALE_Y;
      page.setScaleY(scaleY);
    }
  }
}
