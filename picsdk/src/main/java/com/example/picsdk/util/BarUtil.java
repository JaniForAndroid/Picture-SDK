package com.example.picsdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class BarUtil {

  private static final String TAG_COLOR = "TAG_COLOR";

  public static int getStatusBarHeight(Context context) {
    Resources resources = context.getResources();
    int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
    return resources.getDimensionPixelSize(resourceId);
  }

  public static void setStatusBarLightMode(Activity activity, boolean isLightMode) {
    setStatusBarLightMode(activity.getWindow(), isLightMode);
  }

  private static void setStatusBarLightMode(Window window, boolean isLightMode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      View decorView = window.getDecorView();
      if (decorView != null) {
        int vis = decorView.getSystemUiVisibility();
        if (isLightMode) {
          window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
          vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
          vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(vis);
      }
    }
  }

  public static void setStatusBarColor(Activity activity, @ColorInt int color) {
    setStatusBarColor(activity, color, false);
  }

  public static void setStatusBarColor(Activity activity, @ColorInt int color, boolean isDecor) {
    transparentStatusBar(activity);
    addStatusBarColor(activity, color, isDecor);
  }

  private static void addStatusBarColor(Activity activity, int color, boolean isDecor) {
    ViewGroup parent = isDecor ?
        (ViewGroup) activity.getWindow().getDecorView() :
        (ViewGroup) activity.findViewById(android.R.id.content);
    View fakeStatusBarView = parent.findViewWithTag(TAG_COLOR);
    if (fakeStatusBarView != null) {
      if (fakeStatusBarView.getVisibility() == View.GONE) {
        fakeStatusBarView.setVisibility(View.VISIBLE);
      }
      fakeStatusBarView.setBackgroundColor(color);
    } else {
      parent.addView(createColorStatusBarView(parent.getContext(), color));
    }
  }

  private static View createColorStatusBarView(Context context, int color) {
    View statusBarView = new View(context);
    statusBarView.setLayoutParams(new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(context)));
    statusBarView.setBackgroundColor(color);
    statusBarView.setTag(TAG_COLOR);
    return statusBarView;
  }

  public static void transparentStatusBar(Activity activity) {
    Window window = activity.getWindow();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
      window.getDecorView().setSystemUiVisibility(option);
      window.setStatusBarColor(Color.TRANSPARENT);
    } else {
      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }
  }
}
