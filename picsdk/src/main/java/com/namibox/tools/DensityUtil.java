package com.namibox.tools;

import android.app.Application;
import android.util.DisplayMetrics;
import com.namibox.util.Logger;
import com.namibox.util.Utils;

/**
 * author : feng
 * creation time : 20-1-13下午6:10
 */
public class DensityUtil {

  private static final String TAG = "DensityUtil";

  private static float targetDensity;
  private static float targetScaleDensity;
  private static int targetDensityDpi;

  public static void init(Application application) {
    DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();
    float srcDensity = appDisplayMetrics.density;
    float srcScaleDensity = appDisplayMetrics.scaledDensity;
    Logger.d(TAG, "适配前density： " + srcDensity);

    int minPixels = Math.min(appDisplayMetrics.widthPixels, appDisplayMetrics.heightPixels);
    targetDensity = minPixels / (Utils.isTablet(application) ? 768f : 375f);
    targetScaleDensity = targetDensity * (srcScaleDensity / srcDensity);
    targetDensityDpi = (int) (160 * targetDensity);
    Logger.d(TAG, "适配后density： " + targetDensity);
  }

  public static void setCustomDensity(
      DisplayMetrics appDisplayMetrics, DisplayMetrics activityDisplayMetrics) {
    appDisplayMetrics.density = targetDensity;
    appDisplayMetrics.scaledDensity = targetScaleDensity;
    appDisplayMetrics.densityDpi = targetDensityDpi;
    activityDisplayMetrics.density = targetDensity;
    activityDisplayMetrics.scaledDensity = targetScaleDensity;
    activityDisplayMetrics.densityDpi = targetDensityDpi;
  }
}
