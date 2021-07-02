package com.namibox.tools;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;

/**
 * Create time: 2018/11/6.
 */
public class AnimUtil {

  public static void startGuideAnimator(final View view) {
    ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 1.05f, 1f);
    valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        float scale = (float) animation.getAnimatedValue();
        view.setScaleX(scale);
        view.setScaleY(scale);
      }
    });
    valueAnimator.setRepeatCount(2);
    valueAnimator.setDuration(1500);
    valueAnimator.start();
  }
}
