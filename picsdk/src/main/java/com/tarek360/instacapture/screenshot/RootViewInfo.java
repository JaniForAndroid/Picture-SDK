package com.tarek360.instacapture.screenshot;

import android.view.View;
import android.view.WindowManager.LayoutParams;

/**
 * Created by tarek on 5/18/16.
 */
public class RootViewInfo {

  private int top;
  private int left;
  private View view;
  private LayoutParams layoutParams;

  public RootViewInfo(View view, LayoutParams layoutParams) {
    this.view = view;
    this.layoutParams = layoutParams;
    int[] onScreenPosition = new int[2];
    view.getLocationOnScreen(onScreenPosition);
    left = onScreenPosition[0];
    top = onScreenPosition[1];
  }


  public View getView() {
    return view;
  }

  public LayoutParams getLayoutParams() {
    return layoutParams;
  }

  public int getTop() {
    return top;
  }

  public int getLeft() {
    return left;
  }
}