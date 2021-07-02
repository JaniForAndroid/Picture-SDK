package com.namibox.tools;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import com.namibox.util.Logger;

public class DensityUtils {

  private static float sNoncompatDensity;
  private static float sNoncompatScaleDensity;
  private static final int PHONE_STANDARD_INCH_DP = 147;
  private static final int PAD_STANDARD_INCH_DP = 160;
  private static final float PHONE_STANDARD_INCH = 2.45f;
  private static final float PAD_STANDARD_INCH = 4.2f;
  private static final float RANGE_VALUE = 0.4f;
  private static boolean sEnable = false;
  private static boolean isLogger;
  private static float targetDensity;
  private static float targetScaleDensity;
  private static int targetDensityDpi;

  public static void setEnable(boolean enable) {
    sEnable = enable;
  }

  public static void setCustomDensity(final Application application,
      DisplayMetrics appDisplayMetrics, DisplayMetrics activityDisplayMetrics, boolean isTablet) {
    if (!sEnable) {
      return;
    }
    if (sNoncompatDensity == 0 || targetDensity == 0) {
      sNoncompatDensity = appDisplayMetrics.density;
      sNoncompatScaleDensity = appDisplayMetrics.scaledDensity;
      application.registerComponentCallbacks(new ComponentCallbacks() {
        @Override
        public void onConfigurationChanged(Configuration newConfig) {
          if (newConfig != null && newConfig.fontScale > 0) {
            sNoncompatScaleDensity = application.getResources().getDisplayMetrics().scaledDensity;
          }
        }

        @Override
        public void onLowMemory() {

        }
      });
      float standardInch = PHONE_STANDARD_INCH;
      float standardInchDp = PHONE_STANDARD_INCH_DP;
      if (isTablet) {
        standardInch = PAD_STANDARD_INCH;
        standardInchDp = PAD_STANDARD_INCH_DP;
      }
      int width = appDisplayMetrics.widthPixels < appDisplayMetrics.heightPixels
          ? appDisplayMetrics.widthPixels : appDisplayMetrics.heightPixels;
      float inch = width / appDisplayMetrics.xdpi;
      float totalDp = width / appDisplayMetrics.density;
      float inchDp = totalDp / (width / appDisplayMetrics.xdpi);
      float scale =
          inchDp / (inch > standardInch ? standardInchDp : standardInchDp * standardInch / inch);
      targetDensity = appDisplayMetrics.density * scale;
      if (Math.abs(targetDensity - sNoncompatDensity) > 0.8f) {
        sEnable = false;
        Logger.d("缩放太大, 不进行适配， density " + appDisplayMetrics.density);
        return;
      } else if (isTablet && sNoncompatDensity < 2f){
        sEnable = false;
        Logger.d("平板 density过低, 不进行适配， density " + appDisplayMetrics.density);
        return;
      }
      if (targetDensity - sNoncompatDensity > RANGE_VALUE) {
        targetDensity = sNoncompatDensity + RANGE_VALUE;
      } else if (targetDensity - sNoncompatDensity < -RANGE_VALUE) {
        targetDensity = sNoncompatDensity - RANGE_VALUE;
      }
      targetScaleDensity = targetDensity * (sNoncompatScaleDensity / sNoncompatDensity);
      targetDensityDpi = (int) (160 * targetDensity);
      if (!isLogger) {
        isLogger = true;
        Logger.d("设备信息: " + inch + "英寸" + (isTablet ? "平板" : "手机")
            + "\nwidthPixels " + appDisplayMetrics.widthPixels + " heightPixels " + appDisplayMetrics.heightPixels
            + "\nxdpi " + appDisplayMetrics.xdpi + " ydpi " + appDisplayMetrics.ydpi);
        Logger.d("调整之前: density " + appDisplayMetrics.density + " scaledDensity " + appDisplayMetrics.scaledDensity + " densityDpi " + appDisplayMetrics.densityDpi);
        Logger.d("调整之后: density " + targetDensity + " scaledDensity " + targetScaleDensity + " densityDpi " + targetDensityDpi);
      }
    }

    appDisplayMetrics.density = targetDensity;
    appDisplayMetrics.scaledDensity = targetScaleDensity;
    appDisplayMetrics.densityDpi = targetDensityDpi;

    activityDisplayMetrics.density = targetDensity;
    activityDisplayMetrics.scaledDensity = targetScaleDensity;
    activityDisplayMetrics.densityDpi = targetDensityDpi;
  }
}
